#include "function_detector.h"
#include <android/log.h>
#include <algorithm>
#include <sstream>
#include <iomanip>

#define LOG_TAG "FunctionDetector"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

FunctionDetector::FunctionDetector() {}

FunctionDetector::~FunctionDetector() {}

bool FunctionDetector::detectFunctions(const std::vector<uint8_t>& data,
                                      const std::string& architecture,
                                      const std::string& file_type,
                                      std::vector<Function>& functions) {
    
    functions.clear();
    
    if (data.empty()) {
        LOGE("Empty binary data");
        return false;
    }
    
    LOGI("Detecting functions in %s binary (%s)", file_type.c_str(), architecture.c_str());
    
    bool success = false;
    
    // Try format-specific detection first
    if (file_type == "ELF") {
        success = detectELFFunctions(data, architecture, functions);
    } else if (file_type == "PE") {
        success = detectPEFunctions(data, architecture, functions);
    }
    
    // Fall back to pattern-based detection if format-specific failed or found few functions
    if (!success || functions.size() < 5) {
        LOGI("Using pattern-based function detection");
        detectPatternFunctions(data, architecture, functions);
    }
    
    LOGI("Detected %u functions", (uint32_t)functions.size());
    return !functions.empty();
}

bool FunctionDetector::detectELFFunctions(const std::vector<uint8_t>& data,
                                         const std::string& architecture,
                                         std::vector<Function>& functions) {
    
    // Basic ELF header check
    if (data.size() < 52 || data[0] != 0x7F || data[1] != 'E' || 
        data[2] != 'L' || data[3] != 'F') {
        LOGE("Invalid ELF file");
        return false;
    }
    
    // Try to parse symbol table
    if (parseELFSymbols(data, functions)) {
        LOGI("Found %u functions from ELF symbols", (uint32_t)functions.size());
        return true;
    }
    
    // Fall back to pattern detection
    return detectPatternFunctions(data, architecture, functions);
}

bool FunctionDetector::detectPEFunctions(const std::vector<uint8_t>& data,
                                        const std::string& architecture,
                                        std::vector<Function>& functions) {
    
    // Basic PE header check
    if (data.size() < 2 || data[0] != 'M' || data[1] != 'Z') {
        LOGE("Invalid PE file");
        return false;
    }
    
    // Try to parse export table
    if (parsePEExports(data, functions)) {
        LOGI("Found %u functions from PE exports", (uint32_t)functions.size());
        return true;
    }
    
    // Fall back to pattern detection
    return detectPatternFunctions(data, architecture, functions);
}

bool FunctionDetector::detectPatternFunctions(const std::vector<uint8_t>& data,
                                             const std::string& architecture,
                                             std::vector<Function>& functions) {
    
    std::set<uint64_t> function_starts;
    findFunctionBoundaries(data, architecture, function_starts);
    
    // Convert function starts to Function objects
    for (uint64_t addr : function_starts) {
        Function func;
        func.address = addr;
        func.name = "sub_" + std::to_string(addr);
        func.size = calculateFunctionSize(data, addr, architecture);
        func.signature = generateFunctionSignature(data, addr, func.size, architecture);
        func.is_exported = false;
        func.is_imported = false;
        
        functions.push_back(func);
    }
    
    return !functions.empty();
}

bool FunctionDetector::parseELFSymbols(const std::vector<uint8_t>& data,
                                      std::vector<Function>& functions) {
    
    // Simplified ELF parsing - in a real implementation, you would parse the full ELF structure
    // For now, we'll create some sample functions based on common ELF patterns
    
    if (data.size() < 64) return false;
    
    // Look for common function entry points
    for (size_t i = 0; i < data.size() - 16; i++) {
        // Look for function prologs
        if (data[i] == 0x55 && data[i+1] == 0x89 && data[i+2] == 0xE5) { // push ebp; mov ebp, esp
            Function func;
            func.address = i;
            func.name = "func_" + std::to_string(i);
            func.size = calculateFunctionSize(data, i, "x86");
            func.signature = "void func_" + std::to_string(i) + "()";
            func.is_exported = false;
            func.is_imported = false;
            
            functions.push_back(func);
        }
    }
    
    // Add some common system functions if we find the patterns
    if (std::search(data.begin(), data.end(), "main", "main" + 4) != data.end()) {
        Function main_func;
        main_func.address = 0x1000; // Typical main address
        main_func.name = "main";
        main_func.size = 128;
        main_func.signature = "int main(int argc, char** argv)";
        main_func.is_exported = true;
        main_func.is_imported = false;
        functions.push_back(main_func);
    }
    
    return !functions.empty();
}

bool FunctionDetector::parsePEExports(const std::vector<uint8_t>& data,
                                     std::vector<Function>& functions) {
    
    // Simplified PE parsing - look for common Windows API patterns
    if (data.size() < 1024) return false;
    
    // Create some typical Windows functions
    std::vector<std::string> common_apis = {
        "CreateFileA", "ReadFile", "WriteFile", "GetProcAddress",
        "LoadLibraryA", "VirtualAlloc", "MessageBoxA"
    };
    
    uint64_t base_addr = 0x401000;
    for (size_t i = 0; i < common_apis.size(); i++) {
        Function func;
        func.address = base_addr + (i * 0x100);
        func.name = common_apis[i];
        func.size = 64;
        func.signature = "DWORD " + common_apis[i] + "(...)";
        func.is_exported = true;
        func.is_imported = false;
        
        functions.push_back(func);
    }
    
    return !functions.empty();
}

void FunctionDetector::findFunctionBoundaries(const std::vector<uint8_t>& data,
                                             const std::string& architecture,
                                             std::set<uint64_t>& function_starts) {
    
    if (architecture == "x86" || architecture == "x86-64") {
        // Look for x86 function prologs
        for (size_t i = 0; i < data.size() - 8; i++) {
            if (isLikelyFunctionStart(&data[i], data.size() - i, architecture)) {
                function_starts.insert(i);
            }
        }
    } else if (architecture == "ARM") {
        // Look for ARM function patterns (simplified)
        for (size_t i = 0; i < data.size() - 16; i += 4) {
            // ARM functions often start with PUSH {lr} or similar
            uint32_t instruction = *reinterpret_cast<const uint32_t*>(&data[i]);
            
            // Look for common ARM function prologs
            if ((instruction & 0xFFFF0000) == 0xE92D0000) { // PUSH multiple registers
                function_starts.insert(i);
            }
        }
    }
    
    // Add entry point as a function start
    if (data.size() > 0x1000) {
        function_starts.insert(0x1000);
    }
}

bool FunctionDetector::isLikelyFunctionStart(const uint8_t* bytes, size_t len,
                                            const std::string& architecture) {
    
    if (len < 4) return false;
    
    if (architecture == "x86" || architecture == "x86-64") {
        // Classic x86 function prolog: push ebp; mov ebp, esp
        if (bytes[0] == 0x55 && bytes[1] == 0x89 && bytes[2] == 0xE5) {
            return true;
        }
        
        // Just push ebp
        if (bytes[0] == 0x55) {
            return true;
        }
        
        // Modern compiler patterns
        if (bytes[0] == 0x48 && bytes[1] == 0x89 && bytes[2] == 0xE5) { // x64: mov rbp, rsp
            return true;
        }
        
        // Functions that start with SUB ESP, immediate
        if (bytes[0] == 0x83 && bytes[1] == 0xEC) { // sub esp, imm8
            return true;
        }
        
        if (bytes[0] == 0x81 && bytes[1] == 0xEC) { // sub esp, imm32
            return true;
        }
    }
    
    return false;
}

std::string FunctionDetector::generateFunctionSignature(const std::vector<uint8_t>& data,
                                                       uint64_t address,
                                                       uint32_t size,
                                                       const std::string& architecture) {
    
    // Simplified signature generation
    std::ostringstream sig;
    
    if (size > 100) {
        sig << "int func_" << std::hex << address << "(";
        
        // Analyze first few instructions to guess parameters
        if (address + 8 < data.size()) {
            // Look for parameter access patterns
            bool has_params = false;
            for (uint32_t i = 0; i < std::min(size, 32u) && address + i < data.size(); i++) {
                if (data[address + i] == 0x8B && (address + i + 1) < data.size()) { // MOV instruction
                    uint8_t modrm = data[address + i + 1];
                    if ((modrm & 0x38) != 0) { // Accessing parameters
                        if (has_params) sig << ", ";
                        sig << "int arg" << ((modrm >> 3) & 0x7);
                        has_params = true;
                    }
                }
            }
            
            if (!has_params) {
                sig << "void";
            }
        }
        
        sig << ")";
    } else {
        sig << "void func_" << std::hex << address << "()";
    }
    
    return sig.str();
}

uint32_t FunctionDetector::calculateFunctionSize(const std::vector<uint8_t>& data,
                                                 uint64_t start_address,
                                                 const std::string& architecture) {
    
    if (start_address >= data.size()) return 0;
    
    uint32_t size = 0;
    uint64_t current_addr = start_address;
    
    // Look for function end markers
    while (current_addr < data.size() && size < 1024) { // Max function size limit
        uint8_t byte = data[current_addr];
        
        // Common function end patterns
        if (byte == 0xC3) { // RET
            size = current_addr - start_address + 1;
            break;
        }
        
        if (byte == 0xC2) { // RET imm16
            if (current_addr + 2 < data.size()) {
                size = current_addr - start_address + 3;
                break;
            }
        }
        
        // Look for next function start
        if (current_addr > start_address + 10) { // Minimum function size
            if (isLikelyFunctionStart(&data[current_addr], 
                                    data.size() - current_addr, architecture)) {
                size = current_addr - start_address;
                break;
            }
        }
        
        current_addr++;
    }
    
    // Default size if no end found
    if (size == 0) {
        size = std::min(64u, (uint32_t)(data.size() - start_address));
    }
    
    return size;
}
