#ifndef MOBILE360_SENSINGCOMPONENT_DETECTORTYPES_H
#define MOBILE360_SENSINGCOMPONENT_DETECTORTYPES_H

namespace via {
namespace sensing {

enum class DetectorTypes : unsigned int {
    NONE        = 0,
    FCW         = 1,
    FCW_DL      = 256,
    LDW         = 2,
    BSD_L       = 4,
    BSD_R       = 8,
    BSD_LR      = ((unsigned int)DetectorTypes::BSD_L | (unsigned int)DetectorTypes::BSD_R),
    SLD         = 16,
    BCW         = 32,
    Weather     = 64,
    PED         = 128,
    TLD         = 512
};


enum class RuntimeLoadDataTypes : unsigned char {
    NONE                    = 0,
    FCWS_CascadeModel       = 1,
    FCWS_DL_Model           = 2,
    FCWS_DL_Label           = 3,
    FCWS_DL_DSPLib          = 4,
    SLD_Model               = 5,
    SLD_Proto               = 6,
    SLD_Label               = 7,
    Weather_ClassifyModel   = 8,
    SLD_NightModel          = 9,
    SLD_PrefetchModel       = 10,
    TLD_Pattern_1           = 11,
    TLD_Pattern_2           = 12,
    TLD_Pattern_3           = 13,
};


} // namespace sensing
}   // namespace via

#endif
