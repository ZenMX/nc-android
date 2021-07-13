//
// Created by ZhaoLinlin on 2021/7/9.
//

#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <malloc.h>
#include <time.h>
#include <thread>

#define CRASH_TYPE_NPE 1
#define CRASH_TYPE_STACK_OVER_FLOW 2

void test_stack_over_flow(int a) {
    test_stack_over_flow(a + 1);
}

void mx_test_crash(int type) {
    switch (type) {
        case CRASH_TYPE_NPE: {
            __android_log_print(6, "test", "test crash type %d", type);
            int* p = nullptr;
            int a = *p;
            break;
        }

        case CRASH_TYPE_STACK_OVER_FLOW: {
            __android_log_print(6, "test", "test crash type %d", type);
            test_stack_over_flow(0);
            break;
        }
    }
}


#ifdef __cplusplus
extern "C" {
#endif
    
    
JNIEXPORT void JNICALL
Java_com_mxtech_CrashMe_crash(
        JNIEnv *env,
        jclass clazz, jint type, jboolean thread) {

    if (thread) {
        std::thread* thread1 = new std::thread(mx_test_crash, type);
    } else {
        mx_test_crash(type);
    }

}
#ifdef __cplusplus
}
#endif