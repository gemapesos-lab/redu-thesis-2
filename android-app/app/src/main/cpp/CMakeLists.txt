cmake_minimum_required(VERSION 3.22.1)
project("redu_vlm")

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "llama: build tests" FORCE)
set(LLAMA_BUILD_COMMON ON CACHE BOOL "llama: build common utils library" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "llama: build examples" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "llama: build server" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "ggml: use OpenMP" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "build shared libs" FORCE)

add_subdirectory(llama.cpp)
find_package(Threads REQUIRED)
add_subdirectory(llama.cpp/examples/llava)

add_library(redu_llama_jni SHARED redu_llama_jni.cpp)

target_include_directories(redu_llama_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/common
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/examples/llava
)

target_link_libraries(redu_llama_jni
    PRIVATE
    llama
    ggml
    common
    llava_static
    log
    android
)
