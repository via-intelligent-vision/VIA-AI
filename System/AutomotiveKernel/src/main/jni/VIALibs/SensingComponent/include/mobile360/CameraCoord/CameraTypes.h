#ifndef VIA_ADAS_CAMERATYPES_H
#define VIA_ADAS_CAMERATYPES_H
namespace via {
namespace camera {
/**
@brief Define camera types.
*/
enum class CameraTypes : unsigned int {
    /**
    @brief Unknown camera type.
    */
    Camera_Unknown = 0,
    /**
    @brief Custom the instrinsic.
    */
    Camera_Custom = 1,
    /**
    @brief format : Camera_Vendor, ModuleName, Sensor, FOV
    */
    Camera_Sharp_Module_OV10640_Fov50 = 2,
    Camera_HTC_U11plus_Unknown_Unknown = 3,
    Camera_HTC_U11eyes_Unknown_Unknown = 4,
    Camera_HTC_U12plus_Unknown_Unknown = 8,
    Camera_GooglePixel2_Unknown_Unknown = 5,
    Camera_FIC_Imx6Pad_Unknown_Fov78 = 6,
    Camera_REC_SonyISX016_FOV40 = 7,
};

}
}   // namespace via
#endif //VIA_ADAS_CAMERATYPES_H
