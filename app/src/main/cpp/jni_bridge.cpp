#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "binary_analyzer.h"

#define LOG_TAG "JNI_Bridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global analyzer instance
static BinaryAnalyzer* g_analyzer = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_loadBinary(
    JNIEnv *env, jobject /* this */, jstring file_path) {
    
    const char *path = env->GetStringUTFChars(file_path, nullptr);
    if (!path) {
        LOGE("Failed to get file path string");
        return JNI_FALSE;
    }
    
    // Clean up previous analyzer
    if (g_analyzer) {
        delete g_analyzer;
    }
    
    g_analyzer = new BinaryAnalyzer();
    bool result = g_analyzer->loadBinary(std::string(path));
    
    env->ReleaseStringUTFChars(file_path, path);
    
    LOGI("Binary load result: %s", result ? "success" : "failed");
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobjectArray JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_getFileInfo(
    JNIEnv *env, jobject /* this */, jstring file_path) {
    
    const char *path = env->GetStringUTFChars(file_path, nullptr);
    if (!path) {
        LOGE("Failed to get file path string");
        return nullptr;
    }
    
    // Create temporary analyzer for file info
    BinaryAnalyzer analyzer;
    if (!analyzer.loadBinary(std::string(path))) {
        env->ReleaseStringUTFChars(file_path, path);
        LOGE("Failed to load binary for file info");
        return nullptr;
    }
    
    std::string architecture, file_type;
    uint64_t entry_point;
    
    if (!analyzer.getFileInfo(architecture, file_type, entry_point)) {
        env->ReleaseStringUTFChars(file_path, path);
        LOGE("Failed to get file info");
        return nullptr;
    }
    
    env->ReleaseStringUTFChars(file_path, path);
    
    // Create Java string array
    jobjectArray result = env->NewObjectArray(3, env->FindClass("java/lang/String"), nullptr);
    if (!result) {
        LOGE("Failed to create result array");
        return nullptr;
    }
    
    jstring jarch = env->NewStringUTF(architecture.c_str());
    jstring jtype = env->NewStringUTF(file_type.c_str());
    jstring jentry = env->NewStringUTF(std::to_string(entry_point).c_str());
    
    env->SetObjectArrayElement(result, 0, jarch);
    env->SetObjectArrayElement(result, 1, jtype);
    env->SetObjectArrayElement(result, 2, jentry);
    
    env->DeleteLocalRef(jarch);
    env->DeleteLocalRef(jtype);
    env->DeleteLocalRef(jentry);
    
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_disassemble(
    JNIEnv *env, jobject /* this */, jlong start_address, jint count) {
    
    if (!g_analyzer) {
        LOGE("No binary loaded");
        return nullptr;
    }
    
    std::vector<Instruction> instructions;
    if (!g_analyzer->disassemble(start_address, count, instructions)) {
        LOGE("Disassembly failed");
        return nullptr;
    }
    
    // Find DisassemblyData class
    jclass dataClass = env->FindClass("com/idapro/mobile/native/DisassemblyData");
    if (!dataClass) {
        LOGE("Failed to find DisassemblyData class");
        return nullptr;
    }
    
    // Get constructor
    jmethodID constructor = env->GetMethodID(dataClass, "<init>", 
        "(J[BLjava/lang/String;Ljava/lang/String;ZZJ)V");
    if (!constructor) {
        LOGE("Failed to find DisassemblyData constructor");
        return nullptr;
    }
    
    // Create array
    jobjectArray result = env->NewObjectArray(instructions.size(), dataClass, nullptr);
    if (!result) {
        LOGE("Failed to create instruction array");
        return nullptr;
    }
    
    // Fill array
    for (size_t i = 0; i < instructions.size(); i++) {
        const Instruction& inst = instructions[i];
        
        // Create byte array
        jbyteArray bytes = env->NewByteArray(inst.bytes.size());
        env->SetByteArrayRegion(bytes, 0, inst.bytes.size(), 
                               reinterpret_cast<const jbyte*>(inst.bytes.data()));
        
        // Create strings
        jstring mnemonic = env->NewStringUTF(inst.mnemonic.c_str());
        
        // Join operands
        std::string operands_str;
        for (size_t j = 0; j < inst.operands.size(); j++) {
            if (j > 0) operands_str += ", ";
            operands_str += inst.operands[j];
        }
        jstring operands = env->NewStringUTF(operands_str.c_str());
        
        // Create DisassemblyData object
        jobject dataObj = env->NewObject(dataClass, constructor,
            (jlong)inst.address,
            bytes,
            mnemonic,
            operands,
            (jboolean)inst.is_function,
            (jboolean)inst.is_jump,
            (jlong)inst.jump_target
        );
        
        env->SetObjectArrayElement(result, i, dataObj);
        
        // Clean up local refs
        env->DeleteLocalRef(bytes);
        env->DeleteLocalRef(mnemonic);
        env->DeleteLocalRef(operands);
        env->DeleteLocalRef(dataObj);
    }
    
    env->DeleteLocalRef(dataClass);
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_detectFunctions(
    JNIEnv *env, jobject /* this */) {
    
    if (!g_analyzer) {
        LOGE("No binary loaded");
        return nullptr;
    }
    
    std::vector<Function> functions;
    if (!g_analyzer->detectFunctions(functions)) {
        LOGE("Function detection failed");
        return nullptr;
    }
    
    // Find FunctionData class
    jclass dataClass = env->FindClass("com/idapro/mobile/native/FunctionData");
    if (!dataClass) {
        LOGE("Failed to find FunctionData class");
        return nullptr;
    }
    
    // Get constructor
    jmethodID constructor = env->GetMethodID(dataClass, "<init>", 
        "(Ljava/lang/String;JILjava/lang/String;ZZ)V");
    if (!constructor) {
        LOGE("Failed to find FunctionData constructor");
        return nullptr;
    }
    
    // Create array
    jobjectArray result = env->NewObjectArray(functions.size(), dataClass, nullptr);
    if (!result) {
        LOGE("Failed to create function array");
        return nullptr;
    }
    
    // Fill array
    for (size_t i = 0; i < functions.size(); i++) {
        const Function& func = functions[i];
        
        jstring name = env->NewStringUTF(func.name.c_str());
        jstring signature = env->NewStringUTF(func.signature.c_str());
        
        jobject dataObj = env->NewObject(dataClass, constructor,
            name,
            (jlong)func.address,
            (jint)func.size,
            signature,
            (jboolean)func.is_exported,
            (jboolean)func.is_imported
        );
        
        env->SetObjectArrayElement(result, i, dataObj);
        
        env->DeleteLocalRef(name);
        env->DeleteLocalRef(signature);
        env->DeleteLocalRef(dataObj);
    }
    
    env->DeleteLocalRef(dataClass);
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_readBytes(
    JNIEnv *env, jobject /* this */, jlong address, jint size) {
    
    if (!g_analyzer) {
        LOGE("No binary loaded");
        return nullptr;
    }
    
    std::vector<uint8_t> data;
    if (!g_analyzer->readBytes(address, size, data)) {
        LOGE("Failed to read bytes at address 0x%lx", address);
        return nullptr;
    }
    
    jbyteArray result = env->NewByteArray(data.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, data.size(), 
                               reinterpret_cast<const jbyte*>(data.data()));
    }
    
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_getBinaryData(
    JNIEnv *env, jobject /* this */) {
    
    if (!g_analyzer) {
        LOGE("No binary loaded");
        return nullptr;
    }
    
    std::vector<uint8_t> data;
    if (!g_analyzer->getBinaryData(data)) {
        LOGE("Failed to get binary data");
        return nullptr;
    }
    
    jbyteArray result = env->NewByteArray(data.size());
    if (result) {
        env->SetByteArrayRegion(result, 0, data.size(), 
                               reinterpret_cast<const jbyte*>(data.data()));
    }
    
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_calculateChecksum(
    JNIEnv *env, jobject /* this */, jstring file_path) {
    
    const char *path = env->GetStringUTFChars(file_path, nullptr);
    if (!path) {
        LOGE("Failed to get file path string");
        return nullptr;
    }
    
    // Create temporary analyzer for checksum
    BinaryAnalyzer analyzer;
    if (!analyzer.loadBinary(std::string(path))) {
        env->ReleaseStringUTFChars(file_path, path);
        LOGE("Failed to load binary for checksum");
        return nullptr;
    }
    
    std::string checksum = analyzer.calculateChecksum();
    env->ReleaseStringUTFChars(file_path, path);
    
    if (checksum.empty()) {
        LOGE("Failed to calculate checksum");
        return nullptr;
    }
    
    return env->NewStringUTF(checksum.c_str());
}

JNIEXPORT void JNICALL
Java_com_idapro_mobile_native_NativeBinaryAnalyzer_cleanup(
    JNIEnv *env, jobject /* this */) {
    
    if (g_analyzer) {
        g_analyzer->cleanup();
        delete g_analyzer;
        g_analyzer = nullptr;
        LOGI("Binary analyzer cleaned up");
    }
}

// JNI_OnLoad function
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    
    LOGI("Native binary analyzer library loaded");
    return JNI_VERSION_1_6;
}

// JNI_OnUnload function
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (g_analyzer) {
        delete g_analyzer;
        g_analyzer = nullptr;
    }
    LOGI("Native binary analyzer library unloaded");
}

}
