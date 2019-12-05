#pragma once

#include <opencv2/core.hpp>

#include "mobile360/adas-core/utils/LUT_PixelFormat.h"

class ScaledLookupTable
{
public:
    ScaledLookupTable();
    ~ScaledLookupTable();

    bool Empty();
    bool Lookup(LUT_PixelFormat dstPixelFmt, cv::Mat *dstImg, unsigned char *srcData, cv::Size srcDataSize);
    bool normalizedLookup(LUT_PixelFormat dstPixelFmt, cv::Mat *dstImg, unsigned char *srcData, cv::Size srcDataSize);

    bool Generate(LUT_PixelFormat srcPixelFmt, cv::Size *srcImgSize, cv::Rect *srcROI, cv::MatStep *srcImgStep, float scaleFactor);
    void Release();

    float GetScaleFactor();
    cv::Size GetLookupResultSize();
    cv::Rect GetSrcROI();

private:
    LUT_PixelFormat mSrcPixelFmt;
    float mScaleFactor;
    cv::Rect mSrcImgROI;
    cv::MatStep mSrcImgStep;
    cv::Size mSrcImgSize;
    cv::Size mDstImgSize;
    
    cv::Size mSize_LUTa;
    cv::Size mSize_LUTb;
    cv::Size mSize_LUTc;
    int *mLUTa;
    int *mLUTb;
    int *mLUTc;

    bool GenerateLUT_NV12();
    bool GenerateLUT_NV21();
    bool GenerateLUT_BGR();

    bool normalizedLookup_BGR_to_BGR(cv::Mat *dstImg_32FC3, unsigned char *srcBGR);
    bool Lookup_BGR_to_Gray(cv::Mat *dstImg_8UC1, unsigned char *srcImg_8UC3);
    bool Lookup_BGR_to_BGR(cv::Mat *dstImg_8UC3, unsigned char *srcImg_8UC3);
    bool Lookup_BGR_to_HSV(cv::Mat *dstImg_8UC3, unsigned char *srcImg_8UC3);
    bool Lookup_BGR_to_BGRandGray(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *srcImg_8UC3);

    bool normalizedLookup_NV12_to_BGR(cv::Mat *dstImg_32FC3, unsigned char *sourceNV12);
    bool Lookup_NV12_to_BGRandGray(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV12);
    bool Lookup_NV12_to_BGR(cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV12);
    bool Lookup_NV12_to_Gray(cv::Mat *prespectiveImg_8UC1, unsigned char *sourceNV12);

    bool Lookup_NV21_to_BGRandGray(cv::Mat *prespectiveImg_8UC1, cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV21);
    bool Lookup_NV21_to_BGR(cv::Mat *prespectiveImg_8UC3, unsigned char *sourceNV21);
    bool Lookup_NV21_to_Gray(cv::Mat *prespectiveImg_8UC1, unsigned char *sourceNV21);

};

