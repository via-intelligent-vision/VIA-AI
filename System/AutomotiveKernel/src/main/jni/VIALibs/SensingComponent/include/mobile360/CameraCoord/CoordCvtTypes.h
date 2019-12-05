#ifndef CAMERA_COORDCVTTYPES_H
#define CAMERA_COORDCVTTYPES_H
namespace via {
namespace camera {

enum class CoordCvtTypes : unsigned int {
    Unknown = 0,
    ImgCoord_To_UndistCoord = 1,
    ImgCoord_To_ObjCoord_ZeroZ = 2,
    NormalizedImgCoord_To_ObjCoord_ZeroZ = 3,
    UndistCoord_To_ObjCoord_ZeroZ = 4,
    ObjCoord_To_NormalizedImgCoord = 5,
    NormalizedImgCoord_To_cmDistance = 6,
};

}
}
#endif //CAMERA_COORDCVTTYPES_H
