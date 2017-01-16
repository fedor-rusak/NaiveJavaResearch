# JNI

[JNI](https://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/design.html) — is the best official source I have found that describes binding of native java methods and function implemented in shared libraries (DLLs).

It is not that important when we implement native method calling using java. Yet there are some interesting things to know because native methods are used all over the standard library.

## Creating java object with C++

Thank to this [answer](http://stackoverflow.com/questions/13877543/how-to-instantiate-a-class-in-jni) we have some code:

```java
jobject Java_com_example_hellojni_HelloJni_createObject(JNIEnv *env, jobject this ) {
    jclass cls = (*env)->FindClass(env, "java/lang/Integer");
    jmethodID methodID = (*env)->GetMethodID(env, cls, "<init>", "(I)V");
    return (*env)->NewObject(env,cls, methodID, 5);
}