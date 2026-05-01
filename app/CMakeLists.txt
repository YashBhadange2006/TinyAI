cmake_minimum_required(VERSION 3.22)

add_library(
        llm
        SHARED
        src/main/cpp/native-lib.cpp
)