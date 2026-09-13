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
