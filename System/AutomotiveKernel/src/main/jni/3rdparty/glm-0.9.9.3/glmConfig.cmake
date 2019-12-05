# ===================================================================================
#  The glm CMake configuration file
#
#  Usage from an external project:
#    In your CMakeLists.txt, add these lines:
#
#    find_package(glm REQUIRED)
#    include_directories(${GLM_INCLUDE_DIRS}) # Not needed for CMake >= 2.8.11
#    target_link_libraries(MY_TARGET_NAME ${GLM_LIBS})
#
#    This file will define the following variables:
#      - glm_LIBS_DIR                 : The glm library directories.
#      - glm_LIBS                     : The list of all imported targets for glm modules.
#      - glm_INCLUDE_DIR              : The glm include directories.
#      - glm_VERSION                  : The version of this glm build: "0.9.9.3"
#
# ===================================================================================

# Extract directory name from full path of the file currently being processed.
# Note that CMake 2.8.3 introduced CMAKE_CURRENT_LIST_DIR. We reimplement it
# for older versions of CMake to support these as well.
if(CMAKE_VERSION VERSION_LESS "2.8.3")
    get_filename_component(CMAKE_CURRENT_LIST_DIR "${CMAKE_CURRENT_LIST_FILE}" PATH)
endif()

if(NOT DEFINED GLM_CONFIG_SUBDIR)
    set(GLM_CONFIG_SUBDIR "/abi-${ANDROID_NDK_ABI_NAME}")
endif()

set(GLM_CONFIG_PATH "${CMAKE_CURRENT_LIST_DIR}${GLM_CONFIG_SUBDIR}")
if(EXISTS "${GLM_CONFIG_PATH}/glmConfig.cmake")
    include("${GLM_CONFIG_PATH}/glmConfig.cmake")
else()
    if(NOT glm_FIND_QUIETLY)
        message(WARNING "Found glm Android Pack but it has no binaries compatible with your ABI (can't find: ${GLM_CONFIG_SUBDIR})")
    endif()
        message(Error "glm not found")
        set(glm_FOUND FALSE)
endif()
