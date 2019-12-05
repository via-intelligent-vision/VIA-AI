#pragma once

#include <vector>
#include <opencv2/core.hpp>

class CurveFitting
{
public :
    static bool QuadraticCurveFitting(std::vector<cv::Point2f> &srcPoints, double &coefficientA, double &coefficientB, double &coefficientC);
    static bool CubicCurveFitting(std::vector<cv::Point2f> &srcPoints, double &coefficientA, double &coefficientB, double &coefficientC, double &coefficientD);

private:
    static void ComputeMarquardtMatrix(cv::Mat *pMarquardtMatrix, cv::Mat *pJJ, double lambda);

    static double ComputeError(double* pCoefficient, std::vector<cv::Point2f> &srcPoints);
    static void ComputeResidual(double* pResidual, double* pCoefficient, std::vector<cv::Point2f> &srcPoints);
    static void ComputeJacobian(double* pJ, std::vector<cv::Point2f> &srcPoints);

    static double ComputeError_CubicCurve(double* pCoefficient, std::vector<cv::Point2f> &srcPoints);
    static void ComputeResidual_CubicCurve(double* pResidual, double* pCoefficient, std::vector<cv::Point2f> &srcPoints);
    static void ComputeJacobian_CubicCurve(double* pJ, std::vector<cv::Point2f> &srcPoints);
};