#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <android/bitmap.h>

#include "llava.h"
#include "llama.h"
#include "clip.h"
#include "common.h"
#include "sampling.h"

#define TAG "ReduLlamaJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static struct llama_model * model = nullptr;
static struct clip_ctx * ctx_clip = nullptr;
static struct llama_context * ctx_llama = nullptr;
static struct common_sampler * smpl = nullptr;

static void free_models() {
    if (smpl) { common_sampler_free(smpl); smpl = nullptr; }
    if (ctx_llama) { llama_free(ctx_llama); ctx_llama = nullptr; }
    if (ctx_clip) { clip_free(ctx_clip); ctx_clip = nullptr; }
    if (model) { llama_free_model(model); model = nullptr; }
    llama_backend_free();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_initModels(JNIEnv* env, jobject /* this */, jstring model_path, jstring mmproj_path) {
    if (model != nullptr && ctx_clip != nullptr && ctx_llama != nullptr && smpl != nullptr) {
        return JNI_TRUE;
    }
    if (model != nullptr || ctx_clip != nullptr || ctx_llama != nullptr || smpl != nullptr) {
        free_models();
    }
    
    const char * model_c_str = env->GetStringUTFChars(model_path, 0);
    const char * mmproj_c_str = env->GetStringUTFChars(mmproj_path, 0);
    if (!model_c_str || !mmproj_c_str) {
        if (model_c_str) env->ReleaseStringUTFChars(model_path, model_c_str);
        if (mmproj_c_str) env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        return JNI_FALSE;
    }

    llama_backend_init();

    common_params params;
    params.model = model_c_str;
    params.mmproj = mmproj_c_str;
    params.n_ctx = 2048;

    llama_model_params mparams = common_model_params_to_llama(params);
    model = llama_load_model_from_file(model_c_str, mparams);
    if (!model) {
        LOGE("Failed to load text model");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    ctx_clip = clip_model_load(mmproj_c_str, 1);
    if (!ctx_clip) {
        LOGE("Failed to load mmproj model");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    llama_context_params cparams = common_context_params_to_llama(params);
    cparams.n_ctx = params.n_ctx;
    ctx_llama = llama_new_context_with_model(model, cparams);
    if (!ctx_llama) {
        LOGE("Failed to create llama context");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    smpl = common_sampler_init(model, params.sampling);
    if (!smpl) {
        LOGE("Failed to create sampler");
        env->ReleaseStringUTFChars(model_path, model_c_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
        free_models();
        return JNI_FALSE;
    }

    env->ReleaseStringUTFChars(model_path, model_c_str);
    env->ReleaseStringUTFChars(mmproj_path, mmproj_c_str);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_freeModels(JNIEnv* env, jobject /* this */) {
    free_models();
}

static bool eval_tokens(struct llama_context * ctx, std::vector<llama_token>& tokens, int * n_past) {
    int n_eval = (int) tokens.size();
    if (llama_decode(ctx, llama_batch_get_one(tokens.data(), n_eval))) {
        return false;
    }
    *n_past += n_eval;
    return true;
}

static bool eval_string(struct llama_context * ctx, const std::string& str, int * n_past, bool add_bos) {
    std::vector<llama_token> tokens = common_tokenize(ctx, str, add_bos, true);
    return eval_tokens(ctx, tokens, n_past);
}

extern "C" JNIEXPORT jstring JNICALL
Java_edu_feutech_redu_vlm_MoondreamLlamaNative_inferenceImage(JNIEnv* env, jobject /* this */, jbyteArray image_bytes) {
    if (!model || !ctx_clip || !ctx_llama) {
        return env->NewStringUTF("UNRESOLVED");
    }

    jsize len = env->GetArrayLength(image_bytes);
    jbyte* bytes = env->GetByteArrayElements(image_bytes, 0);

    // Make Image Embed
    llava_image_embed * image_embed = llava_image_embed_make_with_bytes(ctx_clip, 4, (const unsigned char*)bytes, len);
    env->ReleaseByteArrayElements(image_bytes, bytes, JNI_ABORT);

    if (!image_embed) {
        LOGE("Failed to create image embed");
        return env->NewStringUTF("UNRESOLVED");
    }

    int n_past = 0;
    llama_kv_cache_clear(ctx_llama);
    common_sampler_reset(smpl);

    std::string prompt = "Analyze the visual sentiment of this image. You must respond with exactly one of the following labels and nothing else: SEVERE_NEG, MILD_NEG, NEUTRAL, MILD_POS, SEVERE_POS.";
    std::string system_prompt = "A chat between a curious human and an artificial intelligence assistant. The assistant gives helpful, detailed, and polite answers to the human's questions.\nUSER:<image>";
    std::string user_prompt = prompt + "\nASSISTANT:";

    eval_string(ctx_llama, system_prompt, &n_past, true);
    llava_eval_image_embed(ctx_llama, image_embed, 512, &n_past);
    eval_string(ctx_llama, user_prompt, &n_past, false);

    std::string response = "";
    for (int i = 0; i < 32; i++) {
        const llama_token id = common_sampler_sample(smpl, ctx_llama, -1);
        common_sampler_accept(smpl, id, true);
        if (llama_token_is_eog(model, id)) break;
        std::string piece = common_token_to_piece(ctx_llama, id);
        response += piece;
        
        std::vector<llama_token> t;
        t.push_back(id);
        eval_tokens(ctx_llama, t, &n_past);
    }

    llava_image_embed_free(image_embed);
    return env->NewStringUTF(response.c_str());
}
