#---------------------------------------------------------------------------------------------------
# set adas kernel path
#---------------------------------------------------------------------------------------------------
set(PATH_ADASKernel "${CMAKE_CURRENT_LIST_DIR}/../../../../../libadaskernel/src/main/jni")
set(INCLUDE_ADASKernel "${PATH_ADASKernel}")
set(LOCAL_INCLUDE "${PATH_ADASKernel}./include")

#---------------------------------------------------------------------------------------------------
# set include & lib path
#---------------------------------------------------------------------------------------------------
message("INCLUDE_ADASKernel  ${INCLUDE_ADASKernel}")
include_directories(${INCLUDE_ADASKernel})
include_directories(${OpenCV_INCLUDE_DIRS} ${LOCAL_INCLUDE} ${ADAS_INCLUDE})

#---------------------------------------------------------------------------------------------------
# Process subdirectories
#---------------------------------------------------------------------------------------------------
set(SOURCES_AUTOCTL_SUB "")
add_subdirectory(LatitudePlanner)
add_subdirectory(LongitudePlanner)
#---------------------------------------------------------------------------------------------------
# Build library
#---------------------------------------------------------------------------------------------------
file(GLOB SOURCES_CUR "*.cpp")
set(CMAKE_CXX_FLAGS_RELEASE "-O3")

add_library(automotiveControl STATIC ${SOURCES_CUR} ${SOURCES_AUTOCTL_SUB})
set(LIB_AutomotiveControl automotiveControl PARENT_SCOPE)

