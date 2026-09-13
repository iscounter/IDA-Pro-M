#ifndef DISASSEMBLER_H
#define DISASSEMBLER_H

#include "binary_analyzer.h"
#include <vector>
#include <string>
#include <cstdint>

/**
 * Simple disassembler for basic instruction decoding
 */
class Disassembler {
public:
    Disassembler();
    ~Disassembler();
    
    /**
     * Disassemble instructions from binary data
     * @param data Binary data
     * @param architecture Target architecture (x86, x86-64, ARM, etc.)
     * @param start_address Starting address
     * @param count Number of instructions to disassemble
     * @param instructions Output vector of instructions
     * @return true if successful
     */
    bool disassemble(const std::vector<uint8_t>& data, 
                    const std::string& architecture,
                    uint64_t start_address, 
                    uint32_t count, 
                    std::vector<Instruction>& instructions);

private:
    /**
     * Disassemble x86 instructions
     */
    bool disassembleX86(const std::vector<uint8_t>& data, 
                       uint64_t start_offset, 
                       uint32_t count, 
                       std::vector<Instruction>& instructions);
    
    /**
     * Disassemble x86-64 instructions
     */
    bool disassembleX86_64(const std::vector<uint8_t>& data, 
                          uint64_t start_offset, 
                          uint32_t count, 
                          std::vector<Instruction>& instructions);
    
    /**
     * Disassemble ARM instructions
     */
    bool disassembleARM(const std::vector<uint8_t>& data, 
                       uint64_t start_offset, 
                       uint32_t count, 
                       std::vector<Instruction>& instructions);
    
    /**
     * Decode a single x86 instruction
     */
    bool decodeX86Instruction(const uint8_t* bytes, 
                             size_t max_len, 
                             uint64_t address, 
                             Instruction& instruction);
    
    /**
     * Get instruction mnemonic from opcode
     */
    std::string getX86Mnemonic(uint8_t opcode, uint8_t modrm = 0);
    
    /**
     * Parse operands for x86 instruction
     */
    std::string parseX86Operands(const uint8_t* bytes, 
                                size_t len, 
                                uint8_t opcode, 
                                uint8_t modrm);
    
    /**
     * Check if instruction is a function start
     */
    bool isFunctionStart(const uint8_t* bytes, size_t len);
    
    /**
     * Check if instruction is a jump/branch
     */
    bool isJumpInstruction(const uint8_t* bytes, size_t len, uint64_t& target);
};

#endif // DISASSEMBLER_H
