cmake_minimum_required(VERSION 2.8)

set(PROJECT_NAME "gl-image")
project(${PROJECT_NAME})

#---------------------------------------------------------------------------------------------------
# [user define variable]
#---------------------------------------------------------------------------------------------------
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
set(SRC_CUR "")
file(GLOB SRC_CUR "*.cpp")

#---------------------------------------------------------------------------------------------------
# [add library]
#---------------------------------------------------------------------------------------------------
add_library(${PROJECT_NAME} STATIC ${SRC_CUR})
target_link_libraries(${PROJECT_NAME} PUBLIC ${LIBS})

