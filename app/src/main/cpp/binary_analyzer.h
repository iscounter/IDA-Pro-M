#ifndef BINARY_ANALYZER_H
#define BINARY_ANALYZER_H

#include <string>
#include <vector>
#include <memory>
#include <cstdint>

/**
 * Structure to represent a binary instruction
 */
struct Instruction {
    uint64_t address;
    std::vector<uint8_t> bytes;
    std::string mnemonic;
    std::string operands;
    bool is_function;
    bool is_jump;
    uint64_t jump_target;
    
    Instruction() : address(0), is_function(false), is_jump(false), jump_target(0) {}
};

/**
 * Structure to represent a detected function
 */
struct Function {
    std::string name;
    uint64_t address;
    uint32_t size;
    std::string signature;
    bool is_exported;
    bool is_imported;
    
    Function() : address(0), size(0), is_exported(false), is_imported(false) {}
};

/**
 * Main binary analyzer class
 */
class BinaryAnalyzer {
public:
    BinaryAnalyzer();
    ~BinaryAnalyzer();
    
    /**
     * Load and analyze a binary file
     * @param file_path Path to the binary file
     * @return true if successful, false otherwise
     */
    bool loadBinary(const std::string& file_path);
    
    /**
     * Get basic file information
     * @param architecture Output parameter for architecture
     * @param file_type Output parameter for file type
     * @param entry_point Output parameter for entry point address
     * @return true if successful
     */
    bool getFileInfo(std::string& architecture, std::string& file_type, uint64_t& entry_point);
    
    /**
     * Disassemble instructions starting from given address
     * @param start_address Starting address
     * @param count Number of instructions to disassemble
     * @param instructions Output vector of instructions
     * @return true if successful
     */
    bool disassemble(uint64_t start_address, uint32_t count, std::vector<Instruction>& instructions);
    
    /**
     * Detect functions in the binary
     * @param functions Output vector of detected functions
     * @return true if successful
     */
    bool detectFunctions(std::vector<Function>& functions);
    
    /**
     * Read raw bytes from the binary
     * @param address Starting address
     * @param size Number of bytes to read
     * @param data Output vector for the data
     * @return true if successful
     */
    bool readBytes(uint64_t address, uint32_t size, std::vector<uint8_t>& data);
    
    /**
     * Get the entire binary data
     * @param data Output vector for the binary data
     * @return true if successful
     */
    bool getBinaryData(std::vector<uint8_t>& data);
    
    /**
     * Calculate SHA-256 checksum of the file
     * @return Hex string of the checksum
     */
    std::string calculateChecksum();
    
    /**
     * Clean up resources
     */
    void cleanup();

private:
    class Impl;
    std::unique_ptr<Impl> pImpl;
};

/**
 * Utility functions
 */
namespace BinaryUtils {
    /**
     * Detect architecture from binary header
     */
    std::string detectArchitecture(const std::vector<uint8_t>& data);
    
    /**
     * Detect file type from binary header
     */
    std::string detectFileType(const std::vector<uint8_t>& data);
    
    /**
     * Calculate SHA-256 hash
     */
    std::string sha256(const std::vector<uint8_t>& data);
    
    /**
     * Convert bytes to hex string
     */
    std::string bytesToHex(const std::vector<uint8_t>& bytes);
}

#endif // BINARY_ANALYZER_H
