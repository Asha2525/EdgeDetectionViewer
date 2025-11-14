#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ashasuresh_edgedetectionviewer_NativeBridge_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from native C++";
    return env->NewStringUTF(hello.c_str());
}
