cmake_minimum_required(VERSION 3.22.1)
project("redu_vlm")

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "llama: build tests" FORCE)
set(LLAMA_BUILD_COMMON ON CACHE BOOL "llama: build common utils library" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "llama: build examples" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "llama: build tools" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "llama: build server" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "ggml: use OpenMP" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "ggml: use llamafile kernels" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "ggml: optimize for build host" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "build shared libs" FORCE)

add_subdirectory(llama.cpp)
find_package(Threads REQUIRED)
# tools/mtmd reads LLAMA_INSTALL_VERSION, but llama.cpp only defines it inside
# its own subdirectory scope. Without a value here the vendored
# set_target_properties(mtmd PROPERTIES VERSION ...) call receives an odd
# argument list and the release native configure fails.
set(LLAMA_INSTALL_VERSION 0.0.0)
add_subdirectory(llama.cpp/tools/mtmd)

add_library(redu_llama_jni SHARED redu_llama_jni.cpp)

target_include_directories(redu_llama_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/common
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/tools/mtmd
)

target_link_libraries(redu_llama_jni
    PRIVATE
    llama
    ggml
    llama-common
    mtmd
    log
    android
)
