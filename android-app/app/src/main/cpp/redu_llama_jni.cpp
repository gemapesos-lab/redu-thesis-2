#include <jni.h>
#include <string>
#include <vector>
#include <atomic>
#include <chrono>
#include <android/log.h>

#include "llama.h"
#include "common.h"
#include "sampling.h"
#include "mtmd.h"
#include "mtmd-helper.h"

#define TAG "ReduLlamaJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static struct llama_model * model = nullptr;
static struct mtmd_context * ctx_mtmd = nullptr;
static struct llama_context * ctx_llama = nullptr;
static struct common_sampler * smpl = nullptr;
static struct llama_batch generation_batch;
static bool generation_batch_ready = false;
static std::atomic<bool> abort_inference(false);
static std::chrono::steady_clock::time_point inference_deadline;
static constexpr int kInferenceThreads = 4;
static constexpr int kContextSize = 768;
static constexpr int kBatchSize = 256;

static long long elapsed_ms(std::chrono::steady_clock::time_point start) {
    return std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::steady_clock::now() - start
    ).count();
}

static bool should_abort_inference(void *) {
    return abort_inference.load() || std::chrono::steady_clock::now() > inference_deadline;
}

static bool should_abort_mtmd_eval(struct ggml_tensor *, bool, void *) {
    return should_abort_inference(nullptr);
}

static void native_log_callback(enum ggml_log_level level, const char * text, void *) {
    if (!text) return;
    const int priority = level == GGML_LOG_LEVEL_ERROR ? ANDROID_LOG_ERROR :
                         level == GGML_LOG_LEVEL_WARN  ? ANDROID_LOG_WARN :
                         ANDROID_LOG_INFO;
    __android_log_print(priority, TAG, "%s", text);
}

static void free_models() {
    LOGI("free_models start");
    if (generation_batch_ready) {
        llama_batch_free(generation_batch);
        generation_batch_ready = false;
    }
    if (smpl) { common_sampler_free(smpl); smpl = nullptr; }
    if (ctx_llama) { llama_free(ctx_llama); ctx_llama = nullptr; }
    if (ctx_mtmd) { mtmd_free(ctx_mtmd); ctx_mtmd = nullptr; }
    if (model) { llama_model_free(model); model = nullptr; }
    llama_backend_free();
    LOGI("free_models done");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_initModels(JNIEnv* env, jobject /* this */, jstring model_path, jstring mmproj_path) {
    const auto start = std::chrono::steady_clock::now();
    LOGI("initModels start already_loaded=%d partial_loaded=%d",
         model != nullptr && ctx_mtmd != nullptr && ctx_llama != nullptr && smpl != nullptr,
         model != nullptr || ctx_mtmd != nullptr || ctx_llama != nullptr || smpl != nullptr);
    if (model != nullptr && ctx_mtmd != nullptr && ctx_llama != nullptr && smpl != nullptr) {
        LOGI("initModels reused elapsed_ms=%lld", elapsed_ms(start));
        return JNI_TRUE;
    }
    if (model != nullptr || ctx_mtmd != nullptr || ctx_llama != nullptr || smpl != nullptr) {
        LOGI("initModels freeing partial previous state elapsed_ms=%lld", elapsed_ms(start));
        free_models();
    }
    
    const char * model_c_str = env->GetStringUTFChars(model_path, 0);
    const char * mmproj_c_str = env->GetStringUTFChars(mmproj_path, 0);
    if (!model_c_str || !mmproj_c_str) {
        if (model_c_str) env->ReleaseStringUTFChars(model_path, model_c_str);
        if (mmproj_c_str) env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        LOGE("initModels failed to read model paths elapsed_ms=%lld", elapsed_ms(start));
        return JNI_FALSE;
    }

    LOGI("initModels backend init start elapsed_ms=%lld", elapsed_ms(start));
    llama_log_set(native_log_callback, nullptr);
    mtmd_helper_log_set(native_log_callback, nullptr);
    llama_backend_init();
    LOGI("initModels backend init done elapsed_ms=%lld", elapsed_ms(start));

    common_params params;
    params.model.path = model_c_str;
    params.mmproj.path = mmproj_c_str;
    params.n_ctx = kContextSize;
    params.n_batch = kBatchSize;
    params.n_ubatch = kBatchSize;
    params.cpuparams.n_threads = kInferenceThreads;
    params.cpuparams_batch.n_threads = kInferenceThreads;
    params.sampling.temp = 0.0f;
    params.sampling.top_k = 1;
    params.sampling.top_p = 1.0f;
    params.sampling.min_p = 0.0f;
    params.sampling.samplers = { COMMON_SAMPLER_TYPE_TOP_K, COMMON_SAMPLER_TYPE_TEMPERATURE };
    params.sampling.user_sampling_config =
            COMMON_PARAMS_SAMPLING_CONFIG_SAMPLERS |
            COMMON_PARAMS_SAMPLING_CONFIG_TOP_K |
            COMMON_PARAMS_SAMPLING_CONFIG_TOP_P |
            COMMON_PARAMS_SAMPLING_CONFIG_MIN_P |
            COMMON_PARAMS_SAMPLING_CONFIG_TEMP;

    llama_model_params mparams = common_model_params_to_llama(params);
    LOGI("initModels text model load start elapsed_ms=%lld path=%s", elapsed_ms(start), model_c_str);
    model = llama_model_load_from_file(model_c_str, mparams);
    LOGI("initModels text model load done elapsed_ms=%lld success=%d", elapsed_ms(start), model != nullptr);
    if (!model) {
        LOGE("Failed to load text model");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    llama_context_params cparams = common_context_params_to_llama(params);
    cparams.n_ctx = params.n_ctx;
    cparams.abort_callback = should_abort_inference;
    cparams.abort_callback_data = nullptr;
    LOGI("initModels llama context create start elapsed_ms=%lld n_ctx=%d", elapsed_ms(start), cparams.n_ctx);
    ctx_llama = llama_init_from_model(model, cparams);
    LOGI("initModels llama context create done elapsed_ms=%lld success=%d", elapsed_ms(start), ctx_llama != nullptr);
    if (!ctx_llama) {
        LOGE("Failed to create llama context");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    mtmd_context_params mtmd_params = mtmd_context_params_default();
    mtmd_params.use_gpu = false;
    mtmd_params.print_timings = true;
    mtmd_params.n_threads = kInferenceThreads;
    mtmd_params.warmup = false;
    mtmd_params.cb_eval = should_abort_mtmd_eval;
    mtmd_params.cb_eval_user_data = nullptr;
    LOGI("initModels mtmd/mmproj load start elapsed_ms=%lld path=%s", elapsed_ms(start), mmproj_c_str);
    ctx_mtmd = mtmd_init_from_file(mmproj_c_str, model, mtmd_params);
    LOGI("initModels mtmd/mmproj load done elapsed_ms=%lld success=%d vision=%d audio=%d",
         elapsed_ms(start),
         ctx_mtmd != nullptr,
         ctx_mtmd ? mtmd_support_vision(ctx_mtmd) : 0,
         ctx_mtmd ? mtmd_support_audio(ctx_mtmd) : 0);
    if (!ctx_mtmd || !mtmd_support_vision(ctx_mtmd)) {
        LOGE("Failed to load a vision-capable mtmd/mmproj model");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    LOGI("initModels sampler create start elapsed_ms=%lld", elapsed_ms(start));
    smpl = common_sampler_init(model, params.sampling);
    LOGI("initModels sampler create done elapsed_ms=%lld success=%d", elapsed_ms(start), smpl != nullptr);
    if (!smpl) {
        LOGE("Failed to create sampler");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    generation_batch = llama_batch_init(1, 0, 1);
    generation_batch_ready = true;

    env->ReleaseStringUTFChars(model_path, model_c_str);
    env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
    LOGI("initModels done elapsed_ms=%lld", elapsed_ms(start));
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_freeModels(JNIEnv* env, jobject /* this */) {
    LOGI("freeModels requested from JNI");
    abort_inference.store(true);
    free_models();
}

extern "C" JNIEXPORT void JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_cancelInference(JNIEnv* env, jobject /* this */) {
    LOGI("cancelInference requested from JNI");
    abort_inference.store(true);
}

extern "C" JNIEXPORT jstring JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_inferenceImage(JNIEnv* env, jobject /* this */, jbyteArray image_bytes) {
    const auto start = std::chrono::steady_clock::now();
    if (!model || !ctx_mtmd || !ctx_llama || !smpl || !generation_batch_ready) {
        LOGE("inferenceImage called before models ready");
        return env->NewStringUTF("UNRESOLVED");
    }
    abort_inference.store(false);
    inference_deadline = std::chrono::steady_clock::now() + std::chrono::seconds(20);
    llama_set_abort_callback(ctx_llama, should_abort_inference, nullptr);
    LOGI("inferenceImage start deadline_ms=20000");

    jsize len = env->GetArrayLength(image_bytes);
    LOGI("inferenceImage input bytes=%d elapsed_ms=%lld", len, elapsed_ms(start));
    jbyte* bytes = env->GetByteArrayElements(image_bytes, 0);

    LOGI("inferenceImage mtmd bitmap decode start elapsed_ms=%lld", elapsed_ms(start));
    mtmd::bitmap bitmap(mtmd_helper_bitmap_init_from_buf(
            ctx_mtmd,
            reinterpret_cast<const unsigned char *>(bytes),
            static_cast<size_t>(len)));
    env->ReleaseByteArrayElements(image_bytes, bytes, JNI_ABORT);
    LOGI("inferenceImage mtmd bitmap decode done elapsed_ms=%lld success=%d size=%ux%u bytes=%zu",
         elapsed_ms(start),
         bitmap.ptr != nullptr,
         bitmap.ptr ? bitmap.nx() : 0,
         bitmap.ptr ? bitmap.ny() : 0,
         bitmap.ptr ? bitmap.n_bytes() : 0);

    if (!bitmap.ptr) {
        LOGE("Failed to decode image buffer elapsed_ms=%lld", elapsed_ms(start));
        return env->NewStringUTF("UNRESOLVED");
    }

    llama_pos n_past = 0;
    LOGI("inferenceImage kv/sampler reset start elapsed_ms=%lld", elapsed_ms(start));
    llama_memory_clear(llama_get_memory(ctx_llama), true);
    common_sampler_reset(smpl);
    LOGI("inferenceImage kv/sampler reset done elapsed_ms=%lld", elapsed_ms(start));

    const mtmd_bitmap * bitmaps[] = { bitmap.ptr.get() };
    std::string prompt = "A chat between a curious human and an artificial intelligence assistant. The assistant gives helpful answers.\nUSER: ";
    prompt += mtmd_default_marker();
    prompt += "\nAnalyze the visual sentiment of this image. Respond with exactly one label and nothing else: SEVERE_NEG, MILD_NEG, NEUTRAL, MILD_POS, SEVERE_POS.\nASSISTANT:";

    mtmd_input_text text;
    text.text = prompt.c_str();
    text.add_special = true;
    text.parse_special = true;

    mtmd::input_chunks chunks(mtmd_input_chunks_init());
    LOGI("inferenceImage mtmd tokenize start elapsed_ms=%lld", elapsed_ms(start));
    const int32_t tokenize_result = mtmd_tokenize(ctx_mtmd, chunks.ptr.get(), &text, bitmaps, 1);
    LOGI("inferenceImage mtmd tokenize done elapsed_ms=%lld result=%d chunks=%zu total_tokens=%zu total_pos=%d",
         elapsed_ms(start),
         tokenize_result,
         chunks.ptr ? mtmd_input_chunks_size(chunks.ptr.get()) : 0,
         chunks.ptr ? mtmd_helper_get_n_tokens(chunks.ptr.get()) : 0,
         chunks.ptr ? static_cast<int>(mtmd_helper_get_n_pos(chunks.ptr.get())) : 0);
    if (tokenize_result != 0) {
        LOGE("Inference failed during mtmd tokenize result=%d elapsed_ms=%lld", tokenize_result, elapsed_ms(start));
        abort_inference.store(false);
        return env->NewStringUTF("UNRESOLVED");
    }

    for (size_t i = 0; i < mtmd_input_chunks_size(chunks.ptr.get()); ++i) {
        const mtmd_input_chunk * chunk = mtmd_input_chunks_get(chunks.ptr.get(), i);
        LOGI("inferenceImage mtmd chunk index=%zu type=%d tokens=%zu pos=%d id=%s",
             i,
             mtmd_input_chunk_get_type(chunk),
             mtmd_input_chunk_get_n_tokens(chunk),
             static_cast<int>(mtmd_input_chunk_get_n_pos(chunk)),
             mtmd_input_chunk_get_id(chunk) ? mtmd_input_chunk_get_id(chunk) : "");
    }

    LOGI("inferenceImage mtmd eval chunks start elapsed_ms=%lld n_past=%d", elapsed_ms(start), static_cast<int>(n_past));
    llama_pos new_n_past = n_past;
    const int32_t eval_result = mtmd_helper_eval_chunks(
            ctx_mtmd,
            ctx_llama,
            chunks.ptr.get(),
            n_past,
            0,
            kBatchSize,
            true,
            &new_n_past);
    n_past = new_n_past;
    LOGI("inferenceImage mtmd eval chunks done elapsed_ms=%lld result=%d n_past=%d",
         elapsed_ms(start), eval_result, static_cast<int>(n_past));
    if (eval_result != 0) {
        LOGE("Inference aborted or failed during mtmd prompt/image eval result=%d elapsed_ms=%lld", eval_result, elapsed_ms(start));
        abort_inference.store(false);
        return env->NewStringUTF("UNRESOLVED");
    }

    std::string response = "";
    LOGI("inferenceImage generation start elapsed_ms=%lld n_past=%d", elapsed_ms(start), n_past);
    for (int i = 0; i < 32; i++) {
        if (should_abort_inference(nullptr)) {
            LOGE("Inference aborted during generation token=%d elapsed_ms=%lld", i, elapsed_ms(start));
            break;
        }
        if (i == 0 || i % 4 == 0) {
            LOGI("inferenceImage generation sample token=%d elapsed_ms=%lld", i, elapsed_ms(start));
        }
        const llama_token id = common_sampler_sample(smpl, ctx_llama, -1);
        common_sampler_accept(smpl, id, true);
        if (llama_vocab_is_eog(llama_model_get_vocab(model), id)) {
            LOGI("inferenceImage generation eog token=%d elapsed_ms=%lld", i, elapsed_ms(start));
            break;
        }
        std::string piece = common_token_to_piece(ctx_llama, id);
        response += piece;
        
        common_batch_clear(generation_batch);
        common_batch_add(generation_batch, id, n_past++, {0}, true);
        if (llama_decode(ctx_llama, generation_batch)) {
            LOGE("Inference aborted or failed during token eval token=%d elapsed_ms=%lld n_past=%d", i, elapsed_ms(start), n_past);
            break;
        }
    }

    abort_inference.store(false);
    LOGI("inferenceImage done elapsed_ms=%lld response=%s", elapsed_ms(start), response.c_str());
    return env->NewStringUTF(response.c_str());
}
