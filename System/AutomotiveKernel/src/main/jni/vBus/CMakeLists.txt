cmake_minimum_required(VERSION 2.8)

set(PROJECT_NAME "vBus")
project(${PROJECT_NAME})

message("---------------------------------------------------------------------------------")
#---------------------------------------------------------------------------------------------------
# [user define variable]
#---------------------------------------------------------------------------------------------------
option(BUILD_ANDROID            "build library for Android" OFF)
option(BUILD_MODULE_STATIC      "build module as static library" ON)

set(vBus_VERSION        "1.0.0" CACHE STRING "")
set(vBus_VERSION_MAJOR  "1" CACHE STRING "")
set(vBus_VERSION_MINOR  "0" CACHE STRING "")
set(vBus_VERSION_PATCH  "0" CACHE STRING "")
set(vBus_VERSION_STATUS "Regular build" CACHE STRING "")
set(vBus_DIR "" CACHE PATH "" )
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
# [3rd library] [opencv]
#---------------------------------------------------------------------------------------------------
if(NOT OpenCV_FOUND)
    set(OpenCV_DIR ${PKG_OPENCV_CMAKE_PATH})
    include ("${CMAKE_PATH}/Findopencv.cmake")
    include_directories(${OpenCV_INCLUDE_DIRS})
    set (LIBS ${LIBS} ${OpenCV_LIBS})
else()
    include_directories(${OpenCV_INCLUDE_DIRS})
    set (LIBS ${LIBS} ${OpenCV_LIBS})
endif()

#---------------------------------------------------------------------------------------------------
# [3rd library] [libusb-1.0]
#---------------------------------------------------------------------------------------------------
#file(RELATIVE_PATH libusb_DIR ${CMAKE_CURRENT_LIST_DIR}/../3rdparty/libusb1.0)
set(libusb_DIR ${CMAKE_CURRENT_LIST_DIR}/../3rdparty/libusb1.0)
message("libusb_DIR ${libusb_DIR}")

set(libusb_1_0_INCLUDE_PATH ${libusb_DIR}/include)
set(libusb_1_0_LIB_PATH ${libusb_DIR}/lib/${ANDROID_ABI}/)

include_directories(${libusb_1_0_INCLUDE_PATH})
add_library(libusb1.0 SHARED IMPORTED)
set_target_properties(libusb1.0 PROPERTIES IMPORTED_LOCATION ${libusb_1_0_LIB_PATH}/libusb1.0.so)


#---------------------------------------------------------------------------------------------------
# [check dependency]
#---------------------------------------------------------------------------------------------------
#push flags


#---------------------------------------------------------------------------------------------------
# [get module sources]
#---------------------------------------------------------------------------------------------------
set(SRC_CUR "")
file(GLOB SRC_CUR "*.cpp")

add_subdirectory(CANDongles)

add_subdirectory(Honda)

#---------------------------------------------------------------------------------------------------
# [add library]
#---------------------------------------------------------------------------------------------------
include_directories("${PROJECT_INCLUDE_PATH}/")

if(${BUILD_SHARED_LIBS})
    add_library(${PROJECT_NAME} SHARED ${SRC_CUR})
    target_link_libraries(${PROJECT_NAME} PUBLIC ${LIBS} libusb1.0)
else()
    add_library(${PROJECT_NAME} STATIC ${SRC_CUR})
    target_link_libraries(${PROJECT_NAME} PUBLIC ${LIBS} libusb1.0)
endif()
