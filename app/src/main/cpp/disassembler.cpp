#include "disassembler.h"
#include <android/log.h>
#include <algorithm>
#include <sstream>
#include <iomanip>

#define LOG_TAG "Disassembler"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

Disassembler::Disassembler() {}

Disassembler::~Disassembler() {}

bool Disassembler::disassemble(const std::vector<uint8_t>& data, 
                              const std::string& architecture,
                              uint64_t start_address, 
                              uint32_t count, 
                              std::vector<Instruction>& instructions) {
    
    instructions.clear();
    
    if (data.empty() || count == 0) {
        LOGE("Invalid input data or count");
        return false;
    }
    
    // Convert address to offset (simplified approach)
    uint64_t start_offset = std::min(start_address, (uint64_t)data.size());
    
    LOGI("Disassembling %u instructions from offset 0x%lx (%s)", 
         count, start_offset, architecture.c_str());
    
    if (architecture == "x86") {
        return disassembleX86(data, start_offset, count, instructions);
    } else if (architecture == "x86-64") {
        return disassembleX86_64(data, start_offset, count, instructions);
    } else if (architecture == "ARM" || architecture == "arm") {
        return disassembleARM(data, start_offset, count, instructions);
    } else {
        LOGE("Unsupported architecture: %s", architecture.c_str());
        return false;
    }
}

bool Disassembler::disassembleX86(const std::vector<uint8_t>& data, 
                                 uint64_t start_offset, 
                                 uint32_t count, 
                                 std::vector<Instruction>& instructions) {
    
    uint64_t offset = start_offset;
    uint32_t decoded = 0;
    
    while (offset < data.size() && decoded < count) {
        Instruction instr;
        
        if (decodeX86Instruction(&data[offset], data.size() - offset, 
                                start_offset + offset, instr)) {
            instructions.push_back(instr);
            offset += instr.bytes.size();
            decoded++;
        } else {
            // Skip invalid byte
            offset++;
        }
        
        // Safety check to avoid infinite loops
        if (offset >= data.size()) break;
    }
    
    LOGI("Successfully decoded %u x86 instructions", (uint32_t)instructions.size());
    return !instructions.empty();
}

bool Disassembler::disassembleX86_64(const std::vector<uint8_t>& data, 
                                    uint64_t start_offset, 
                                    uint32_t count, 
                                    std::vector<Instruction>& instructions) {
    // For simplicity, use same logic as x86 with different addressing
    return disassembleX86(data, start_offset, count, instructions);
}

bool Disassembler::disassembleARM(const std::vector<uint8_t>& data, 
                                 uint64_t start_offset, 
                                 uint32_t count, 
                                 std::vector<Instruction>& instructions) {
    
    uint64_t offset = start_offset;
    uint32_t decoded = 0;
    
    // Simple ARM disassembly - assume ARM32 with 4-byte instructions
    while (offset + 4 <= data.size() && decoded < count) {
        Instruction instr;
        instr.address = start_offset + offset;
        instr.bytes.assign(data.begin() + offset, data.begin() + offset + 4);
        
        // Basic ARM instruction decoding
        uint32_t instruction = *reinterpret_cast<const uint32_t*>(&data[offset]);
        
        // Decode based on ARM instruction format
        uint8_t cond = (instruction >> 28) & 0xF;
        uint8_t opcode = (instruction >> 21) & 0xF;
        
        switch ((instruction >> 24) & 0xF) {
            case 0x0:
            case 0x1:
                instr.mnemonic = "mov";
                instr.operands = "r0, r1"; // Simplified
                break;
            case 0x2:
            case 0x3:
                instr.mnemonic = "add";
                instr.operands = "r0, r1, r2"; // Simplified
                break;
            case 0xA:
            case 0xB:
                instr.mnemonic = "b";
                instr.operands = "0x" + std::to_string(instruction & 0xFFFFFF);
                instr.is_jump = true;
                instr.jump_target = instr.address + ((int32_t)(instruction << 8) >> 6);
                break;
            default:
                instr.mnemonic = "unknown";
                instr.operands = "";
                break;
        }
        
        instructions.push_back(instr);
        offset += 4;
        decoded++;
    }
    
    LOGI("Successfully decoded %u ARM instructions", (uint32_t)instructions.size());
    return !instructions.empty();
}

bool Disassembler::decodeX86Instruction(const uint8_t* bytes, 
                                       size_t max_len, 
                                       uint64_t address, 
                                       Instruction& instruction) {
    if (max_len == 0) return false;
    
    instruction.address = address;
    instruction.is_function = isFunctionStart(bytes, max_len);
    
    uint8_t opcode = bytes[0];
    instruction.mnemonic = getX86Mnemonic(opcode);
    
    // Determine instruction length (simplified)
    size_t inst_len = 1;
    
    // Handle common instruction patterns
    switch (opcode) {
        case 0x50: case 0x51: case 0x52: case 0x53: // PUSH reg
        case 0x54: case 0x55: case 0x56: case 0x57:
            inst_len = 1;
            instruction.operands = "r" + std::to_string(opcode & 0x7);
            break;
            
        case 0x58: case 0x59: case 0x5A: case 0x5B: // POP reg
        case 0x5C: case 0x5D: case 0x5E: case 0x5F:
            inst_len = 1;
            instruction.operands = "r" + std::to_string(opcode & 0x7);
            break;
            
        case 0x90: // NOP
            inst_len = 1;
            instruction.operands = "";
            break;
            
        case 0xC3: // RET
            inst_len = 1;
            instruction.operands = "";
            break;
            
        case 0xE8: // CALL rel32
            if (max_len >= 5) {
                inst_len = 5;
                int32_t offset = *reinterpret_cast<const int32_t*>(&bytes[1]);
                uint64_t target = address + inst_len + offset;
                instruction.operands = "0x" + std::to_string(target);
                instruction.is_jump = true;
                instruction.jump_target = target;
            } else {
                return false;
            }
            break;
            
        case 0xE9: // JMP rel32
            if (max_len >= 5) {
                inst_len = 5;
                int32_t offset = *reinterpret_cast<const int32_t*>(&bytes[1]);
                uint64_t target = address + inst_len + offset;
                instruction.operands = "0x" + std::to_string(target);
                instruction.is_jump = true;
                instruction.jump_target = target;
            } else {
                return false;
            }
            break;
            
        case 0x74: case 0x75: // JZ/JNZ rel8
        case 0x76: case 0x77: // JBE/JA rel8
        case 0x78: case 0x79: // JS/JNS rel8
            if (max_len >= 2) {
                inst_len = 2;
                int8_t offset = static_cast<int8_t>(bytes[1]);
                uint64_t target = address + inst_len + offset;
                instruction.operands = "0x" + std::to_string(target);
                instruction.is_jump = true;
                instruction.jump_target = target;
            } else {
                return false;
            }
            break;
            
        case 0x89: // MOV r/m32, r32
            if (max_len >= 2) {
                inst_len = 2;
                uint8_t modrm = bytes[1];
                instruction.operands = parseX86Operands(bytes, max_len, opcode, modrm);
            } else {
                return false;
            }
            break;
            
        default:
            // Unknown instruction - try to skip safely
            inst_len = 1;
            instruction.operands = "";
            break;
    }
    
    if (inst_len > max_len) {
        return false;
    }
    
    // Store instruction bytes
    instruction.bytes.assign(bytes, bytes + inst_len);
    
    return true;
}

std::string Disassembler::getX86Mnemonic(uint8_t opcode, uint8_t modrm) {
    switch (opcode) {
        case 0x50: case 0x51: case 0x52: case 0x53:
        case 0x54: case 0x55: case 0x56: case 0x57:
            return "push";
            
        case 0x58: case 0x59: case 0x5A: case 0x5B:
        case 0x5C: case 0x5D: case 0x5E: case 0x5F:
            return "pop";
            
        case 0x89: return "mov";
        case 0x90: return "nop";
        case 0xC3: return "ret";
        case 0xE8: return "call";
        case 0xE9: return "jmp";
        case 0xEB: return "jmp";
        
        case 0x74: return "jz";
        case 0x75: return "jnz";
        case 0x76: return "jbe";
        case 0x77: return "ja";
        case 0x78: return "js";
        case 0x79: return "jns";
        case 0x7A: return "jp";
        case 0x7B: return "jnp";
        case 0x7C: return "jl";
        case 0x7D: return "jge";
        case 0x7E: return "jle";
        case 0x7F: return "jg";
        
        default: return "db";
    }
}

std::string Disassembler::parseX86Operands(const uint8_t* bytes, 
                                          size_t len, 
                                          uint8_t opcode, 
                                          uint8_t modrm) {
    // Simplified operand parsing
    uint8_t mod = (modrm >> 6) & 0x3;
    uint8_t reg = (modrm >> 3) & 0x7;
    uint8_t rm = modrm & 0x7;
    
    std::string reg_names[] = {"eax", "ecx", "edx", "ebx", "esp", "ebp", "esi", "edi"};
    
    if (mod == 0x3) { // Register to register
        return reg_names[rm] + ", " + reg_names[reg];
    } else {
        // Memory operand (simplified)
        return "[" + reg_names[rm] + "], " + reg_names[reg];
    }
}

bool Disassembler::isFunctionStart(const uint8_t* bytes, size_t len) {
    if (len < 3) return false;
    
    // Common function prologs
    // push ebp; mov ebp, esp
    if (bytes[0] == 0x55 && bytes[1] == 0x89 && bytes[2] == 0xE5) {
        return true;
    }
    
    // push ebp
    if (bytes[0] == 0x55) {
        return true;
    }
    
    return false;
}

bool Disassembler::isJumpInstruction(const uint8_t* bytes, size_t len, uint64_t& target) {
    if (len == 0) return false;
    
    uint8_t opcode = bytes[0];
    
    // Conditional jumps (rel8)
    if ((opcode >= 0x70 && opcode <= 0x7F) && len >= 2) {
        int8_t offset = static_cast<int8_t>(bytes[1]);
        target = offset; // Relative offset
        return true;
    }
    
    // JMP rel8
    if (opcode == 0xEB && len >= 2) {
        int8_t offset = static_cast<int8_t>(bytes[1]);
        target = offset;
        return true;
    }
    
    // JMP rel32
    if (opcode == 0xE9 && len >= 5) {
        int32_t offset = *reinterpret_cast<const int32_t*>(&bytes[1]);
        target = offset;
        return true;
    }
    
    // CALL rel32
    if (opcode == 0xE8 && len >= 5) {
        int32_t offset = *reinterpret_cast<const int32_t*>(&bytes[1]);
        target = offset;
        return true;
    }
    
    return false;
}
