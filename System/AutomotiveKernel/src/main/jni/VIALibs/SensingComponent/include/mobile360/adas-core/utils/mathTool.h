#pragma once

#include <opencv2/core.hpp>

namespace mathTool
{
    void CalcCrossProduct(double *pa, double *pb, double *C);

    float CalcLineValueX(cv::Point2f p0, cv::Point2f p1, float y);

    float CalcLineValueY(cv::Point2f p0, cv::Point2f p1, float x);

    double CalcDistancePointToLine(cv::Point2f p, cv::Point2f lineP0, cv::Point2f lineP1);

    cv::Point2f CalcFootOfPerpendicular_PointToLine(cv::Point2f p, cv::Point2f lineP0, cv::Point2f lineP1);

    bool CalcLinesIntersection(cv::Point2f &intersection, cv::Point2f lineAp0, cv::Point2f lineAp1, cv::Point2f lineBp0, cv::Point2f lineBp1);

    double CalcDegree(cv::Vec2f v0, cv::Vec2f v1);
};

