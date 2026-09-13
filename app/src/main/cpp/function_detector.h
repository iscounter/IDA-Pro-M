#ifndef FUNCTION_DETECTOR_H
#define FUNCTION_DETECTOR_H

#include "binary_analyzer.h"
#include <vector>
#include <string>
#include <cstdint>
#include <set>

/**
 * Function detection and analysis
 */
class FunctionDetector {
public:
    FunctionDetector();
    ~FunctionDetector();
    
    /**
     * Detect functions in binary data
     * @param data Binary data
     * @param architecture Target architecture
     * @param file_type Binary file type (ELF, PE, Mach-O)
     * @param functions Output vector of detected functions
     * @return true if successful
     */
    bool detectFunctions(const std::vector<uint8_t>& data,
                        const std::string& architecture,
                        const std::string& file_type,
                        std::vector<Function>& functions);

private:
    /**
     * Detect functions in ELF files
     */
    bool detectELFFunctions(const std::vector<uint8_t>& data,
                           const std::string& architecture,
                           std::vector<Function>& functions);
    
    /**
     * Detect functions in PE files
     */
    bool detectPEFunctions(const std::vector<uint8_t>& data,
                          const std::string& architecture,
                          std::vector<Function>& functions);
    
    /**
     * Detect functions using pattern analysis
     */
    bool detectPatternFunctions(const std::vector<uint8_t>& data,
                               const std::string& architecture,
                               std::vector<Function>& functions);
    
    /**
     * Parse ELF symbol table
     */
    bool parseELFSymbols(const std::vector<uint8_t>& data,
                        std::vector<Function>& functions);
    
    /**
     * Parse PE export table
     */
    bool parsePEExports(const std::vector<uint8_t>& data,
                       std::vector<Function>& functions);
    
    /**
     * Find function boundaries using heuristics
     */
    void findFunctionBoundaries(const std::vector<uint8_t>& data,
                               const std::string& architecture,
                               std::set<uint64_t>& function_starts);
    
    /**
     * Analyze code patterns for function detection
     */
    bool isLikelyFunctionStart(const uint8_t* bytes, size_t len,
                              const std::string& architecture);
    
    /**
     * Generate function signature
     */
    std::string generateFunctionSignature(const std::vector<uint8_t>& data,
                                         uint64_t address,
                                         uint32_t size,
                                         const std::string& architecture);
    
    /**
     * Calculate function size
     */
    uint32_t calculateFunctionSize(const std::vector<uint8_t>& data,
                                  uint64_t start_address,
                                  const std::string& architecture);
};

#endif // FUNCTION_DETECTOR_H
