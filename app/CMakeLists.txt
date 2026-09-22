cmake_minimum_required(VERSION 3.22)

project("tinyai")

add_library(
        tinyai_native
        SHARED
        src/main/cpp/native-lib.cpp
)
target_link_options(
        tinyai_native
        PRIVATE
        "-Wl,-z,max-page-size=16384"
)

find_library(
        log-lib
        log
)

target_link_libraries(
        tinyai_native
        ${log-lib}
)