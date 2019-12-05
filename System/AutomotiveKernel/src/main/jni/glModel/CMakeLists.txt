cmake_minimum_required(VERSION 2.8)

set(PROJECT_NAME "glModel")
project(${PROJECT_NAME})

message("---------------------------------------------------------------------------------")
#---------------------------------------------------------------------------------------------------
# [user define variable]
#---------------------------------------------------------------------------------------------------
option(BUILD_MODULE_STATIC      "build module as static library" ON)

set(AdasCore_DIR "" CACHE PATH "")
set(libusb_DIR "" CACHE PATH "")

# apply default value
if(NOT EXISTS ${CMAKE_BINARY_DIR}/CMakeCache.txt)
    if(NOT BUILD_SHARED_LIBS)
        set(BUILD_SHARED_LIBS "OFF" CACHE STRING "" FORCE)
    endif()
    if(NOT CMAKE_BUILD_TYPE)
        set(CMAKE_BUILD_TYPE "Release" CACHE STRING "" FORCE)
    endif()
endif()


if(CMAKE_BUILD_TYPE MATCHES Debug)
    if(NOT CMAKE_DEBUG_POSTFIX)
        set(CMAKE_DEBUG_POSTFIX d)
    endif()
endif()

set(PROJECT_INCLUDE_PATH ${CMAKE_CURRENT_LIST_DIR}/include)
set(CMAKE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Werror -O3 -fPIC")

#---------------------------------------------------------------------------------------------------
# [3rd library] [glm]
#---------------------------------------------------------------------------------------------------
if(NOT glm_FOUND)
    set(glm_DIR "${3RDPARTY_PATH}/glm-0.9.9.3")
    find_package(glm REQUIRED)
    include_directories(${glm_INCLUDE_DIR})
endif()

#---------------------------------------------------------------------------------------------------
# [get module sources]
#---------------------------------------------------------------------------------------------------
# [ Lane model ]
add_subdirectory(lane)
set(MODULES ${MODULES} gl-lane)

# [ Image model ]
add_subdirectory(image)
set(MODULES ${MODULES} gl-image)

# [ core model ]
add_subdirectory(core)
set(MODULES ${MODULES} gl-core)

set(SRC_CUR "")
file(GLOB SRC_CUR "*.cpp")

#---------------------------------------------------------------------------------------------------
# [add library]
#---------------------------------------------------------------------------------------------------
include_directories("${PROJECT_INCLUDE_PATH}/")

if(NOT ${BUILD_MODULE_STATIC})
    add_library(${PROJECT_NAME} SHARED ${SRC_CUR})
    #target_link_libraries(${PROJECT_NAME} PUBLIC ${MODULES})
    target_link_libraries(${PROJECT_NAME} ${LIBS}  "-Wl,--whole-archive" ${MODULES} "-Wl,--no-whole-archive")
else()
    add_library(${PROJECT_NAME} STATIC ${SRC_CUR})
    target_link_libraries(${PROJECT_NAME} PUBLIC ${MODULES})
endif()
