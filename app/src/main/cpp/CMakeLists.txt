cmake_minimum_required(VERSION 3.22.1)

project("binary_analyzer")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add compiler flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -O2")

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

# Add the native library
add_library(
    binary_analyzer
    SHARED
    binary_analyzer.cpp
    disassembler.cpp
    function_detector.cpp
    jni_bridge.cpp
)

# Link libraries
target_link_libraries(
    binary_analyzer
    ${log-lib}
    ${android-lib}
)

# Set library properties
set_target_properties(
    binary_analyzer
    PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)

# Sets the minimum version of CMake required to build the native library.
# This ensures that this project is built with a toolchain that supports
# a wide array of CMake features.
cmake_minimum_required(VERSION 3.18.1)

# Sets the project name.
project(mobilearmdisassembler)

# Specifies the C++ standard to use.
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

# Configure link-time optimization (LTO) and profile-guided optimization (PGO).
# LTO: Reduces code size and potentially improves runtime performance.
# PGO: Requires a profiling step first, typically not enabled by default for simple builds.
# We'll enable LTO. PGO is more complex and usually for release builds after profiling.
if (CMAKE_BUILD_TYPE MATCHES "Release")
    # Link-Time Optimization (LTO)
    set(CMAKE_INTERPROCEDURAL_OPTIMIZATION TRUE)
    # You can add PGO flags here if you have profile data:
    # target_compile_options(mobilearmdisassembler PRIVATE -fprofile-use -fprofile-correction)
    # target_link_options(mobilearmdisassembler PRIVATE -fprofile-use -fprofile-correction)
endif()

# Adds the include directory for header files.
include_directories(include)

# Defines a library named 'native-lib'.
# 'SHARED' means that it is a shared library that can be loaded by other components.
# The source files are listed after 'SHARED'.
add_library(
    mobilearmdisassembler
    SHARED
    src/native_lib.cpp
    src/elf_parser.cpp
    src/arm_disassembler.cpp
    src/utils.cpp
)

# Searches for a prebuilt static library called 'log'
# (part of the Android NDK) to use for logging.
find_library(log-lib log)

# Specifies libraries to link to the target library.
target_link_libraries(
    mobilearmdisassembler
    ${log-lib}
)

# Optional: Add Capstone as a third-party dependency
# If you decide to use Capstone (highly recommended for a robust disassembler),
# you would typically add it as a prebuilt library or build it from source.
# For simplicity in this example, we'll assume a custom disassembler or a
# Capstone integration that's managed outside this basic CMake,
# or you'd add_subdirectory(path/to/capstone) here.
# For now, we'll implement a simplified ARM disassembler.
