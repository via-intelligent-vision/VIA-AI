#pragma once
#include <opencv2/core.hpp>

class LongitudinalFilter
{
public :
    static void YellowEnhance(cv::Mat *groundFrame_8UC3, cv::Mat *resultFrame_8UC1);
    static void ColorEnhance(cv::Mat *groundFrame_8UC3, cv::Mat *resultFrame_8UC1);
    static void RedEnhance(cv::Mat *groundFrame_8UC3, cv::Mat *resultFrame_8UC1);
    static void WhiteEnhance(cv::Mat *groundFrame_8UC3, cv::Mat *resultFrame_8UC1);
    static void LaneMultiFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidthA, int laneMaxWidthB, int laneMaxHeight);
    static void MixFilter(cv::Mat &groundFrame_8UC1, cv::Mat &frameL1_8UC1, cv::Mat &frameL2_8UC1, int laneMaxWidthA, int laneMaxWidthB, int laneMaxHeight);
    static void MultiFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidthA, int laneMaxWidthB, int laneMaxHeight);
    static void Filter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidth, int laneMaxHeight);
    static void VerticalFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidth);
    static void HorizontalFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidth);
    static void InverseHorizontalFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, int laneMaxWidth);
    static void HorizontalThinningFilter(cv::Mat *srcFrame_8UC1);
    static double GetThreshVal_Otsu_8u(const cv::Mat& _src);
    static void DarkChannel_Path3(cv::Mat *srcImg_RGB, cv::Mat *dstDarkChannel);
    static void FirstTouchFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1, bool touchFromLeft);
    static void VerticalGradientFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1);
    static void HorizontalGradientFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1);
    static void BoundaryFilter(cv::Mat groundFrame_8UC1, cv::Mat resultFrame_8UC1);
private :
    static bool minFilter_Path3(cv::Mat *srcMinValue_8UC1, cv::Mat *dstDarkChannel_8UC1);
    static bool minValue3b(cv::Mat *srcImg_RGB, cv::Mat *dstMinValue);
};
