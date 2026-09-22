#include <jni.h>
#include "llama.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_yashbhadange_tinyai_LlamaCppEngine_stringFromJNI(
        JNIEnv* env,
        jobject /*this*/){
    return env->NewStringUTF("Hello from TinyAI C++!");
}

// initModel -> Takes .gguf,initialized llama_backend, config context paras,thread count and loads model
// freeModel -> unload context,model and frees backend memory
// generateResult (actual text gen) -> takes user text, tokenizes and generates tokens
// sendTokenCallBack -> Token streaming

extern "C"
JNIEXPORT void JNICALL
Java_com_yashbhadange_tinyai_LlamaCppEngine_initMain(
        JNIEnv *env,
        jobject /*this*/,
        jstring nativeLibDir) {
    llama_log_
}