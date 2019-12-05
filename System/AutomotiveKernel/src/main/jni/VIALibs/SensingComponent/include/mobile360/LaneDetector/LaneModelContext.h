#ifndef VIA_ADAS_LANEMODELCONTEXT_H
#define VIA_ADAS_LANEMODELCONTEXT_H

//#include <sensing/ldws/LaneMarkingContext.h>
#include <mutex>
#include "mobile360/LaneDetector/LaneMarkingContext.h"
namespace via {
namespace sensing {
namespace lane {

class LaneModelContext {
public:
    LaneModelContext();
    void copyTo(LaneModelContext &dst);
    void reset();

    LaneMarkingContext parabola;
    double curvature;
    float width;
    float prob_L;
    float prob_R;
    float driftRatio_L;
    float driftRatio_R;
private:
    std::mutex ctxMutex;
};


}   //namespace lane
}   //namespace sensing
}   //namespace via

#endif //VIA_ADAS_LANEMODELCONTEXT_H
