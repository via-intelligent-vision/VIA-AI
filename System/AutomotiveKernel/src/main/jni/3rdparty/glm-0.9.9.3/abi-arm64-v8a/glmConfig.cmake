
get_filename_component(GLM_INSTALL_PATH "${GLM_CONFIG_PATH}/../" REALPATH)

set(glm_LIBS_DIR "${GLM_INSTALL_PATH}/libs/${ANDROID_NDK_ABI_NAME}")
set(glm_LIBS "")
set(glm_INCLUDE_DIR "${GLM_INSTALL_PATH}/include")
set(glm_VERSION "v0.9.9.3")