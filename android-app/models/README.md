# Moondream 0.5B VLM Model Files

## Overview

This directory contains the **Moondream 2 (0.5B)** Vision-Language Model files used by the REDU app's no-text visual sentiment fallback path.

## Model Details

| Property | Value |
|---|---|
| **Model** | Moondream 2 (0.5B parameters) |
| **Source** | [ggml-org/moondream2-20250414-GGUF](https://huggingface.co/ggml-org/moondream2-20250414-GGUF) |
| **Base Model** | [vikhyatk/moondream2](https://huggingface.co/vikhyatk/moondream2) |
| **Architecture** | phi2 |
| **Context Length** | 2048 tokens |
| **License** | Apache 2.0 |
| **Format** | GGUF (llama.cpp compatible) |

## Files

### Deployment Pair (recommended for mobile)

| File | Quantization | Size | Purpose |
|---|---|---|---|
| `moondream2-text-model-Q4_K_M.gguf` | **Q4_K_M** | **877 MB** | Text/LLM decoder |
| `moondream2-mmproj-f16-20250414.gguf` | F16 | **868 MB** | Vision encoder / multimodal projector |
| **Total** | | **1.7 GB** | |

### Original F16 (for reference / re-quantization)

| File | Quantization | Size |
|---|---|---|
| `moondream2-text-model-f16_ct-vicuna.gguf` | F16 | 2.6 GB |

> The F16 text model is kept for reference. Only the Q4_K_M version is needed for deployment.

## Quantization

The text model was quantized from F16 to **Q4_K_M** using `llama-quantize` (llama.cpp):

```bash
llama-quantize moondream2-text-model-f16_ct-vicuna.gguf \
               moondream2-text-model-Q4_K_M.gguf Q4_K_M
```

**Results:**
- Original: 2706.27 MiB (16.01 BPW)
- Quantized: 875.18 MiB (5.18 BPW)
- **67.7% size reduction** with minimal quality loss

The vision projector (`mmproj`) is kept at F16 because vision encoders are sensitive to quantization.

## In-App Download

The REDU app includes a **"Download Model"** button in **Settings > VLM Model** that:
1. Downloads both GGUF files to the app's internal storage
2. Shows a progress bar during download
3. Allows deleting the model to reclaim storage

The download URLs are configured in `ModelDownloadManager.kt`.

### Hosting

To host the quantized model for in-app download:
1. Create a HuggingFace repo (e.g., `your-username/redu-moondream2-q4km-gguf`)
2. Upload `moondream2-text-model-Q4_K_M.gguf` and `moondream2-mmproj-f16-20250414.gguf`
3. Update the `BASE_URL` constant in `ModelDownloadManager.kt` to point to your repo

## Usage in REDU

These model files are used by the `VisualSentimentResolver` component to perform on-device Visual Question Answering (VQA) when a social media content item has no usable caption or visible comments.

### VQA Labels

The model is prompted to classify each keyframe into one of five sentiment labels:
- `SEVERE_NEG` — Severe negative sentiment
- `MILD_NEG` — Mild negative sentiment
- `NEUTRAL` — Neutral sentiment
- `MILD_POS` — Mild positive sentiment
- `SEVERE_POS` — Severe positive sentiment

### Inference Pipeline

1. Android `AccessibilityService.takeScreenshot` captures an on-demand frame for no-text items
2. The frame is held in volatile RAM only (never persisted to storage)
3. The frame is processed by Moondream 0.5B via constrained VQA
4. The item's label is applied only if the same session and item transition are still active
5. If `SEVERE_NEG` or `MILD_NEG` wins, the item contributes negative exposure to NSD
