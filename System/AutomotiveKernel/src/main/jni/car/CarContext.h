/* /////////////////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//                                 MIT License
//                            Copyright (c) 2019 VIA, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//
// ////////////////////////////////////////////////////////////////////////////////////////////// */

#pragma once

#include <car/CarDefines.h>
#include <opencv2/core/core.hpp>

namespace via {
namespace car {

class CarContext
{
public:
    CarContext();
    CarContext(CarTypes carType);
    ~CarContext();

    /**
    @brief Load & save Configuration by tag <Car>
    */
    bool load(const std::string &xmlPath);
    bool load(cv::FileStorage &fs);
    bool save(const std::string &exportFullPath);
    bool save(cv::FileStorage &fs);
    void copyFrom(CarContext &src);

    void setCarType(CarTypes carType);
    void setCarAxleParams(CarAxleParams &paras);
    void setCarBodyShellParams(CarBodyShellParams &params);
    void setCarTireParams(CarTireParams &param);
    void setCarSpeedParams(CarSpeedParams &paras);
    void setCarSteeringParams(CarSteeringParams &params);
    void setCarSteeringControlParams(CarSteeringControlParams &param);


    CarTypes getCarType();
    void getCarAxleParams(CarAxleParams &paras);
    void getCarBodyShellParams(CarBodyShellParams &params);
    void getCarTireParams(CarTireParams &param);
    void getCarSpeedParams(CarSpeedParams &paras);
    void getCarSteeringParams(CarSteeringParams &params);
    void getCarSteeringControlParams(CarSteeringControlParams &param);

private:
    void reset();

    CarTypes carType;
    CarAxleParams carAxleParams;
    CarBodyShellParams carBodyShellParams;
    CarTireParams carTireParams; 
    CarSpeedParams carSpeedParams;
    CarSteeringParams carSteeringParams;
    CarSteeringControlParams carSteeringControlParams;
};


}
}