#---------------------------------------------------------------------------------------------------
# Build library
#---------------------------------------------------------------------------------------------------
file(GLOB SOURCES_CUR "*.cpp")
set(CMAKE_CXX_FLAGS_RELEASE "-O3")

add_library(car STATIC ${SOURCES_CUR} ${SOURCES_CAR_SUB})
set(LIB_car car PARENT_SCOPE)

