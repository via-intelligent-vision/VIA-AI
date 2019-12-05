cmake_minimum_required(VERSION 2.8)

set(PROJECT_NAME VIA-AI)
project(${PROJECT_NAME})

# Add local include
set(ADAS_INCLUDE "./")
set(3rd_Path "${CMAKE_CURRENT_LIST_DIR}/3rdparty")
set(VALUE_PRODUCT_NAME                      "ADAS SDK")
set(VALUE_RELEASE_DATE                      ${TODAY})
set(VALUE_RELEASE_VERSION                   "v2.4")
string(TIMESTAMP TODAY "%Y%m%d")

include_directories(${ADAS_INCLUDE})

#---------------------------------------------------------------------------------------------------
# Setup value to cmake
#---------------------------------------------------------------------------------------------------

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fexceptions -frtti -Werror=return-type")

if(ENABLE_RUNTIME_LIMITATION)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DENABLE_RUNTIME_LIMITATION")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DTIME_LIMITATION_SECOND=${TIME_LIMITATION_SECOND}")
endif()

#---------------------------------------------------------------------------------------------------
# [3rd library] [opencv]
#---------------------------------------------------------------------------------------------------
if(NOT OpenCV_FOUND)
    set(OpenCV_DIR "${3rd_Path}/opencv_3.4.5/sdk/native/jni/")
    find_package(OpenCV REQUIRED)
endif()


message("OpenCV library status:")
message("    - version: ${OpenCV_VERSION}")
message("    - libraries: ${OpenCV_LIBS}")
message("    - include path: ${OpenCV_INCLUDE_DIRS}")
include_directories(${OpenCV_INCLUDE_DIRS})

#---------------------------------------------------------------------------------------------------
# [3rd library] [glm]
#---------------------------------------------------------------------------------------------------
if(NOT glm_FOUND)
    set(glm_DIR "${3rd_Path}/glm-0.9.9.3")
    find_package(glm REQUIRED)
    include_directories(${glm_INCLUDE_DIR})
endif()

#---------------------------------------------------------------------------------------------------
# [3rd library] [fastcv]
#---------------------------------------------------------------------------------------------------
#if (${PRODUCT_FLAVOR}  MATCHES "SNPE")
#    set(INCLUDE_fastcv "${3rd_Path}/fastcv/include")
#    set(LIB_fastcv "${3rd_Path}/fastcv/libs/${ANDROID_ABI}/libfastcv.a")
#
#    include_directories(${INCLUDE_fastcv})
#    add_library(libfastcv SHARED IMPORTED)
#    #add_definitions(-DWITH_FASTCV)
#    set_target_properties(libfastcv PROPERTIES IMPORTED_LOCATION ${LIB_fastcv})
#endif()

#---------------------------------------------------------------------------------------------------
# [VIS SensingKernel]
#---------------------------------------------------------------------------------------------------
set (VIA_SENSING_COMPONENT_PATH "${CMAKE_CURRENT_LIST_DIR}/VIALibs/SensingComponent")

include_directories(${VIA_SENSING_COMPONENT_PATH}/include)
get_filename_component(SensingComponent_PATH "${VIA_SENSING_COMPONENT_PATH}/libs/${ANDROID_ABI}" ABSOLUTE)
link_directories(${SensingComponent_PATH})
add_library(SensingComponent SHARED IMPORTED)
set_target_properties(SensingComponent PROPERTIES IMPORTED_LOCATION ${SensingComponent_PATH}/libSensingComponent.so)



#string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE_LC)
#get_filename_component(SensingComponent_PATH "../../../../SensingKernel/build/intermediates/cmake/${BUILD_TYPE_LC}/obj/${ANDROID_ABI}" ABSOLUTE)
#link_directories(${SensingComponent_PATH})
#
#add_library(SensingComponent SHARED IMPORTED)
#
#if(CMAKE_BUILD_TYPE MATCHES Debug)
#    set_target_properties(SensingComponent PROPERTIES IMPORTED_LOCATION ${SensingComponent_PATH}/libSensingComponentd.so)
#else()
#    set_target_properties(SensingComponent PROPERTIES IMPORTED_LOCATION ${SensingComponent_PATH}/libSensingComponent.so)
#endif()


#---------------------------------------------------------------------------------------------------
# Process subdirectories
#---------------------------------------------------------------------------------------------------
set (3RDPARTY_PATH ${3rd_Path})
set (SNPE_PATH ${3RDPARTY_PATH}/SNPE)

# [ Tools ]
add_subdirectory(tools)

# [ Automotive Control ]
add_subdirectory(automotiveControl)

# [ vBus ]
add_subdirectory(vBus)

# [ Car ]
add_subdirectory(car)

# [ gl ]
add_subdirectory(glModel)

#---------------------------------------------------------------------------------------------------
# Build library
#---------------------------------------------------------------------------------------------------
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-Bsymbolic")
set(LIB_3rd "${LIB_3rd};${LIB_libusb_1_0};${LIB_fastcv}")
message("LIB_3rd :  ${LIB_3rd}")

file(GLOB SOURCES_CUR "*.cpp")
message("LIB_3rd : ${SOURCES_CUR}")
add_library(${PROJECT_NAME} SHARED ${SOURCES_CUR})

target_link_libraries(${PROJECT_NAME} PUBLIC ${LIB_3rd} ${LIB_AutomotiveControl} ${LIB_car}
                    vBus SensingComponent tools glModel
                     ${OpenCV_LIBS} ${LIB_fastcv} log jnigraphics EGL GLESv2 android)
