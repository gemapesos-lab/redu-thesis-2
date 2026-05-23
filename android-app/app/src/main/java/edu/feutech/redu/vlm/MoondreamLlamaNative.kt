package edu.feutech.redu.vlm

object MoondreamLlamaNative {
    init {
        System.loadLibrary("redu_llama_jni")
    }

    /**
     * Initializes the Moondream2 model with the given file paths.
     * Must be called before inference.
     */
    external fun initModels(modelPath: String, mmprojPath: String): Boolean

    /**
     * Frees the Moondream2 model from memory.
     */
    external fun freeModels()

    /**
     * Processes an image (encoded as JPEG/PNG bytes) and returns the VQA sentiment label.
     * The prompt used is the predefined constrained VQA prompt for visual sentiment.
     */
    external fun inferenceImage(imageBytes: ByteArray): String
}
