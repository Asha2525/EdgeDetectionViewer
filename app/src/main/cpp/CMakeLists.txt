cmake_minimum_required(VERSION 3.10.2)
project("edgedetectionviewer")

# Build your native library
add_library(native-lib SHARED native-lib.cpp)

# Link standard Android libraries only
find_library(log-lib log)

# Link OpenCV via Gradle dependency (no find_package)
target_link_libraries(native-lib
        ${log-lib})
