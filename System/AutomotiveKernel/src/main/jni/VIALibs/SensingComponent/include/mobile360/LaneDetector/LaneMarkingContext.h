#ifndef VIA_ADAS_LANEMARKINGCONTEXT_H
#define VIA_ADAS_LANEMARKINGCONTEXT_H
namespace via {
namespace sensing {
namespace lane {

class LaneMarkingContext {
public:
    LaneMarkingContext();
    LaneMarkingContext(double a, double b, double c);
    void copyTo(LaneMarkingContext &dst);
    double value(double x);
    double getCurvature(double x = 20 * 100);
    double cA;   // x = cA * x^2 + cB * x + cC;
    double cB;
    double cC;
};

} // namespace lane
} // namespace sensing
}   //namespace via

#endif //VIA_ADAS_LANEMARKINGCONTEXT_H
