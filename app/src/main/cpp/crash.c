#include <jni.h>

//
// Created by ZhaoLinlin on 2021/7/12.
//
void test() {
    int* p = NULL;
    int a = *p;
}

JNIEXPORT void JNICALL
Java_com_mxtech_MainActivity_testCrash(JNIEnv
* env,
jclass clazz
) {
    test();
}
