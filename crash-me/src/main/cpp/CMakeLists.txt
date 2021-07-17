# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("crash-me")

add_compile_options(-fPIC)
add_compile_options("-O0")

add_library( # Sets the name of the library.
             mx-crash-me

             # Sets the library as a shared library.
             SHARED

        crash_me.cpp
        )

INCLUDE_DIRECTORIES(
        ${CMAKE_SOURCE_DIR}
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

#string(TOLOWER "${CMAKE_BUILD_TYPE}" build_type)
#if(build_type MATCHES "^rel")
#    set_target_properties(native-lib PROPERTIES CXX_VISIBILITY_PRESET hidden)
#    set (CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--version-script=${CMAKE_SOURCE_DIR}/version_script")
#endif()

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       mx-crash-me
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
        )