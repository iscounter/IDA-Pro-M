#include "binary_analyzer.h"
#include "disassembler.h"
#include "function_detector.h"
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <algorithm>
#include <openssl/sha.h>

#define LOG_TAG "BinaryAnalyzer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class BinaryAnalyzer::Impl {
public:
    std::vector<uint8_t> binary_data;
    std::string file_path;
    std::string architecture;
    std::string file_type;
    uint64_t entry_point;
    bool is_loaded;
    
    Impl() : entry_point(0), is_loaded(false) {}
    
    bool loadFile(const std::string& path) {
        std::ifstream file(path, std::ios::binary | std::ios::ate);
        if (!file.is_open()) {
            LOGE("Failed to open file: %s", path.c_str());
            return false;
        }
        
        std::streamsize size = file.tellg();
        file.seekg(0, std::ios::beg);
        
        binary_data.resize(size);
        if (!file.read(reinterpret_cast<char*>(binary_data.data()), size)) {
            LOGE("Failed to read file: %s", path.c_str());
            return false;
        }
        
        file_path = path;
        
        // Analyze file header
        architecture = BinaryUtils::detectArchitecture(binary_data);
        file_type = BinaryUtils::detectFileType(binary_data);
        entry_point = extractEntryPoint();
        
        is_loaded = true;
        LOGI("Successfully loaded binary: %s (%s, %s)", path.c_str(), 
             architecture.c_str(), file_type.c_str());
        
        return true;
    }
    
private:
    uint64_t extractEntryPoint() {
        if (binary_data.size() < 4) return 0;
        
        // ELF files
        if (binary_data[0] == 0x7F && binary_data[1] == 'E' && 
            binary_data[2] == 'L' && binary_data[3] == 'F') {
            
            if (binary_data.size() < 32) return 0;
            
            // Check if 32-bit or 64-bit
            bool is64bit = (binary_data[4] == 2);
            
            if (is64bit && binary_data.size() >= 40) {
                // 64-bit ELF entry point at offset 24
                uint64_t entry = 0;
                for (int i = 0; i < 8; i++) {
                    entry |= (uint64_t)binary_data[24 + i] << (i * 8);
                }
                return entry;
            } else if (!is64bit && binary_data.size() >= 28) {
                // 32-bit ELF entry point at offset 24
                uint32_t entry = 0;
                for (int i = 0; i < 4; i++) {
                    entry |= (uint32_t)binary_data[24 + i] << (i * 8);
                }
                return entry;
            }
        }
        
        // PE files
        if (binary_data[0] == 'M' && binary_data[1] == 'Z') {
            // Simple PE parsing - return base address
            return 0x400000; // Default Windows base address
        }
        
        return 0;
    }
};

BinaryAnalyzer::BinaryAnalyzer() : pImpl(std::make_unique<Impl>()) {}

BinaryAnalyzer::~BinaryAnalyzer() = default;

bool BinaryAnalyzer::loadBinary(const std::string& file_path) {
    return pImpl->loadFile(file_path);
}

bool BinaryAnalyzer::getFileInfo(std::string& architecture, std::string& file_type, uint64_t& entry_point) {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded");
        return false;
    }
    
    architecture = pImpl->architecture;
    file_type = pImpl->file_type;
    entry_point = pImpl->entry_point;
    
    return true;
}

bool BinaryAnalyzer::disassemble(uint64_t start_address, uint32_t count, std::vector<Instruction>& instructions) {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded for disassembly");
        return false;
    }
    
    Disassembler disasm;
    return disasm.disassemble(pImpl->binary_data, pImpl->architecture, start_address, count, instructions);
}

bool BinaryAnalyzer::detectFunctions(std::vector<Function>& functions) {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded for function detection");
        return false;
    }
    
    FunctionDetector detector;
    return detector.detectFunctions(pImpl->binary_data, pImpl->architecture, pImpl->file_type, functions);
}

bool BinaryAnalyzer::readBytes(uint64_t address, uint32_t size, std::vector<uint8_t>& data) {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded");
        return false;
    }
    
    // For simplicity, treat address as offset into file
    if (address >= pImpl->binary_data.size()) {
        LOGE("Address out of bounds: 0x%lx", address);
        return false;
    }
    
    uint64_t end_address = std::min(address + size, (uint64_t)pImpl->binary_data.size());
    uint32_t actual_size = end_address - address;
    
    data.resize(actual_size);
    std::copy(pImpl->binary_data.begin() + address, 
              pImpl->binary_data.begin() + end_address, 
              data.begin());
    
    return true;
}

bool BinaryAnalyzer::getBinaryData(std::vector<uint8_t>& data) {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded");
        return false;
    }
    
    data = pImpl->binary_data;
    return true;
}

std::string BinaryAnalyzer::calculateChecksum() {
    if (!pImpl->is_loaded) {
        LOGE("No binary loaded for checksum calculation");
        return "";
    }
    
    return BinaryUtils::sha256(pImpl->binary_data);
}

void BinaryAnalyzer::cleanup() {
    pImpl->binary_data.clear();
    pImpl->file_path.clear();
    pImpl->is_loaded = false;
    LOGI("Binary analyzer cleaned up");
}

// Utility functions implementation
namespace BinaryUtils {
    
    std::string detectArchitecture(const std::vector<uint8_t>& data) {
        if (data.size() < 4) return "Unknown";
        
        // ELF files
        if (data[0] == 0x7F && data[1] == 'E' && data[2] == 'L' && data[3] == 'F') {
            if (data.size() > 4) {
                switch (data[4]) {
                    case 1: return "x86";
                    case 2: return "x86-64";
                    default: return "ELF";
                }
            }
            return "ELF";
        }
        
        // PE files
        if (data[0] == 'M' && data[1] == 'Z') {
            return "x86"; // Simplified - could parse further for x64
        }
        
        // Mach-O files
        if (data.size() >= 4) {
            uint32_t magic = *reinterpret_cast<const uint32_t*>(data.data());
            switch (magic) {
                case 0xfeedface: return "x86";
                case 0xfeedfacf: return "x86-64";
                case 0xcefaedfe: return "x86";
                case 0xcffaedfe: return "x86-64";
            }
        }
        
        return "Unknown";
    }
    
    std::string detectFileType(const std::vector<uint8_t>& data) {
        if (data.size() < 4) return "Unknown";
        
        // ELF
        if (data[0] == 0x7F && data[1] == 'E' && data[2] == 'L' && data[3] == 'F') {
            return "ELF";
        }
        
        // PE
        if (data[0] == 'M' && data[1] == 'Z') {
            return "PE";
        }
        
        // Mach-O
        if (data.size() >= 4) {
            uint32_t magic = *reinterpret_cast<const uint32_t*>(data.data());
            if (magic == 0xfeedface || magic == 0xfeedfacf || 
                magic == 0xcefaedfe || magic == 0xcffaedfe) {
                return "Mach-O";
            }
        }
        
        return "Binary";
    }
    
    std::string sha256(const std::vector<uint8_t>& data) {
        unsigned char hash[SHA256_DIGEST_LENGTH];
        SHA256_CTX sha256;
        SHA256_Init(&sha256);
        SHA256_Update(&sha256, data.data(), data.size());
        SHA256_Final(hash, &sha256);
        
        std::stringstream ss;
        for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
            ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
        }
        
        return ss.str();
    }
    
    std::string bytesToHex(const std::vector<uint8_t>& bytes) {
        std::stringstream ss;
        for (uint8_t byte : bytes) {
            ss << std::hex << std::setw(2) << std::setfill('0') << (int)byte;
        }
        return ss.str();
    }
}
