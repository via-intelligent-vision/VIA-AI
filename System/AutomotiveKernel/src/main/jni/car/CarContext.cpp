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

#include <stdexcept>
#include <opencv2/core/core.hpp>
#include <car/CarContext.h>
// ------------------------------------------------------------------------------------
using namespace via::car;
using namespace cv;
using namespace std;
// ------------------------------------------------------------------------------------
#define TAG_Car "Car"
#define TAG_CarType "CarType"
#define TAG_Wheelbase "Wheelbase"
#define TAG_Track "Track"
#define TAG_VehicleWidth "VehicleWidth"
#define TAG_TailToBackWheelLength "TailToBackWheelLength"
#define TAG_HeadToFrontWheelLength "HeadToFrontWheelLength"
#define TAG_TireDegree_LeftFront "TireDegree_LeftFront"
#define TAG_TireDegree_RightFront "TireDegree_RightFront"
// ------------------------------------------------------------------------------------
CarContext::CarContext()
        :CarContext(CarTypes::HONDA_CRV_2017_BOSCH)
{
}

CarContext::CarContext(CarTypes carType)
{
    reset();
    this->setCarType(carType);
}

CarContext::~CarContext()
{
}

void CarContext::reset()
{
    carSpeedParams.carSpeed = 0;
    carSpeedParams.engineSpeed = 0;
    carSpeedParams.wheelSpeed_FrontLeft = 0;
    carSpeedParams.wheelSpeed_FrontRight = 0;
    carSpeedParams.wheelSpeed_RearLeft = 0;
    carSpeedParams.wheelSpeed_RearRight = 0;

    carSteeringParams.steerAngle = 0;
    carSteeringParams.steerMaxAngle = 500;
    carSteeringParams.steeringCtlAngleRate = 0;

    carSteeringControlParams.steerTorque = 0;
    carSteeringControlParams.steerTorqueRequest = 0;
    carSteeringControlParams.unit_SteerTorque_to_SteerAngle = 0;
}

/**
@brief Load & save Configuration by tag <Car>
*/
bool CarContext::load(const std::string &xmlPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        bool ret = storage.open(xmlPath, cv::FileStorage::READ);
        if (!ret) {
            errStream << "CarContext Open file error :" << xmlPath;
            throw std::runtime_error(errStream.str());
        }
        ret = this->load(storage);
        storage.release();
    }
    catch (cv::Exception &error) {   //it's a fatal error, throw to abort
        printf("CarContext  Error : %s", error.what());
        ret = false;
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        printf("CarContext  Error : %s", error.what());
        ret = false;
    }

    return ret;
}

bool CarContext::load(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        FileNode nodeCar;    // Due to compatibility, <Car> is accepted. 
        if (fs[TAG_Car].isNamed()) {
            nodeCar = fs[TAG_Car];
        }

        if (!nodeCar.isNone()) {
            if (nodeCar[TAG_CarType].isNamed()) {
                int type;
                nodeCar[TAG_CarType] >> type;
                this->carType = (CarTypes)type;
            }

            if (nodeCar[TAG_Wheelbase].isNamed()) {
                nodeCar[TAG_Wheelbase] >> this->carAxleParams.wheelBase;
            }
            if (nodeCar[TAG_Track].isNamed()) {
                nodeCar[TAG_Track] >> this->carAxleParams.frontTrack;
                nodeCar[TAG_Track] >> this->carAxleParams.rearTrack;
            }
            if (nodeCar[TAG_VehicleWidth].isNamed()) {
                nodeCar[TAG_VehicleWidth] >> this->carBodyShellParams.vehicleWidth;
            }
            if (nodeCar[TAG_TailToBackWheelLength].isNamed()) {
                nodeCar[TAG_TailToBackWheelLength] >> this->carBodyShellParams.vehicleTailLength;
            }
            if (nodeCar[TAG_HeadToFrontWheelLength].isNamed()) {
                nodeCar[TAG_HeadToFrontWheelLength] >> this->carBodyShellParams.vehicleFrontLength;
            }
            if (nodeCar[TAG_TireDegree_LeftFront].isNamed()) {
                nodeCar[TAG_TireDegree_LeftFront] >> this->carTireParams.tireDegree_LeftFront;
            }
            if (nodeCar[TAG_TireDegree_RightFront].isNamed()) {
                nodeCar[TAG_TireDegree_RightFront] >> this->carTireParams.tireDegree_RightFront;
            }

            ret = true;
        }
        else {
            errStream << "No element <" << TAG_Car << "> in this file";
            throw runtime_error(errStream.str());
        }
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        throw error;
    }

    return ret;
}

bool CarContext::save(const std::string &exportFullPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        ret = storage.open(exportFullPath, cv::FileStorage::WRITE);
        if (!ret) {
            errStream << "Open file error :" << exportFullPath;
            throw std::runtime_error(errStream.str());
        }
        this->save(storage);
        storage.release();

        ret = true;
    }
    catch (cv::Exception &error) {   //it's a fatal error, throw to abort
        printf("Error : %s", error.what());
        ret = false;
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        printf("Error : %s", error.what());
        ret = false;
    }

    return ret;
}

bool CarContext::save(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        if (!fs.isOpened()) {
            errStream << "FileStorage couldn't be empty.";
        }
        else {
            fs << TAG_Car << "{";

            fs << TAG_CarType << (int)this->carType;
            fs << TAG_Wheelbase << this->carAxleParams.wheelBase;
            fs << TAG_Track << this->carAxleParams.frontTrack;
            fs << TAG_VehicleWidth << this->carBodyShellParams.vehicleWidth;
            fs << TAG_TailToBackWheelLength << this->carBodyShellParams.vehicleTailLength;
            fs << TAG_HeadToFrontWheelLength << this->carBodyShellParams.vehicleFrontLength;
            fs << TAG_TireDegree_LeftFront << this->carTireParams.tireDegree_LeftFront;
            fs << TAG_TireDegree_RightFront << this->carTireParams.tireDegree_RightFront;

            fs << "}";  // end of TAG_Car
            ret = true;
        }
    }
    catch (cv::Exception e) {
        throw;
    }

    return ret;
}

void CarContext::copyFrom(CarContext &src)
{
    this->carType = src.carType;
    this->carAxleParams.copyFrom(src.carAxleParams);
    this->carBodyShellParams.copyFrom(src.carBodyShellParams);
    this->carTireParams.copyFrom(src.carTireParams);
    this->carSpeedParams.copyFrom(src.carSpeedParams);
    this->carSteeringParams.copyFrom(src.carSteeringParams);
    this->carSteeringControlParams.copyFrom(src.carSteeringControlParams);
}

void CarContext::setCarType(CarTypes carType)
{
    this->carType = carType;

    switch (carType) {
        case CarTypes::LUXGEN_M7_Turbo:
            carAxleParams.wheelBase = 291;
            carAxleParams.frontTrack = 160.5;
            carAxleParams.ratioCenterToFront = 0.41f;
            carAxleParams.rearTrack = 160.5;
            carBodyShellParams.vehicleWidth = 160.5;
            carBodyShellParams.vehicleTailLength = 90;
            carBodyShellParams.vehicleFrontLength = 90;
            carTireParams.tireDegree_LeftFront = 32.5;
            carTireParams.tireDegree_RightFront = 32.5;
            carSteeringParams.steerMaxAngle = 540.0f;
            break;
        case CarTypes::HONDA_CRV_2017_BOSCH:
            carAxleParams.wheelBase = 266;
            carAxleParams.ratioCenterToFront = 0.41f;
            carAxleParams.frontTrack = 160.5;
            carAxleParams.rearTrack = 160.5;
            carBodyShellParams.vehicleMass = 1541 + 60 * 5; // kg
            carBodyShellParams.vehicleWidth = 160.5;
            carBodyShellParams.vehicleTailLength = 90;
            carBodyShellParams.vehicleFrontLength = 90;
            carTireParams.tireDegree_LeftFront = 31.5;
            carTireParams.tireDegree_RightFront = 31.5;
            carSteeringParams.steerMaxAngle = 425.0f;
            carSteeringParams.steeringRatio = 12.3f;
            break;
        default:
            break;
    }
}

void CarContext::setCarAxleParams(CarAxleParams &params)
{
    this->carAxleParams.wheelBase   = params.wheelBase;
    this->carAxleParams.frontTrack  = params.frontTrack;
    this->carAxleParams.rearTrack   = params.rearTrack;
}

void CarContext::setCarBodyShellParams(CarBodyShellParams &params)
{
    this->carBodyShellParams.vehicleMass        = params.vehicleMass;
    this->carBodyShellParams.vehicleWidth       = params.vehicleWidth;
    this->carBodyShellParams.vehicleLength      = params.vehicleLength;
    this->carBodyShellParams.vehicleFrontLength = params.vehicleFrontLength;
    this->carBodyShellParams.vehicleTailLength  = params.vehicleTailLength;
}

void CarContext::setCarTireParams(CarTireParams &params)
{
    this->carTireParams.tireDegree_LeftFront    = params.tireDegree_LeftFront;
    this->carTireParams.tireDegree_RightFront   = params.tireDegree_RightFront;
}

void CarContext::setCarSpeedParams(CarSpeedParams &params)
{
    this->carSpeedParams.carSpeed               = params.carSpeed;
    this->carSpeedParams.engineSpeed            = params.engineSpeed;
    this->carSpeedParams.wheelSpeed_FrontLeft   = params.wheelSpeed_FrontLeft;
    this->carSpeedParams.wheelSpeed_FrontRight  = params.wheelSpeed_FrontRight;
    this->carSpeedParams.wheelSpeed_RearLeft    = params.wheelSpeed_RearLeft;
    this->carSpeedParams.wheelSpeed_RearRight   = params.wheelSpeed_RearRight;
}

void CarContext::setCarSteeringParams(CarSteeringParams &params)
{
    this->carSteeringParams.steerAngle      = params.steerAngle;
    this->carSteeringParams.steerMaxAngle   = params.steerMaxAngle;
    this->carSteeringParams.steeringCtlAngleRate  = params.steeringCtlAngleRate;
}

void CarContext::setCarSteeringControlParams(CarSteeringControlParams &params)
{
    this->carSteeringControlParams.steerTorque                      = params.steerTorque;
    this->carSteeringControlParams.steerTorqueRequest               = params.steerTorqueRequest;
    this->carSteeringControlParams.unit_SteerTorque_to_SteerAngle   = params.unit_SteerTorque_to_SteerAngle;
}

CarTypes CarContext::getCarType()
{
    return carType;
}

void CarContext::getCarAxleParams(CarAxleParams &params)
{
    params.copyFrom(this->carAxleParams);
}

void CarContext::getCarBodyShellParams(CarBodyShellParams &params)
{
    params.copyFrom(this->carBodyShellParams);
}

void CarContext::getCarTireParams(CarTireParams &params)
{
    params.copyFrom(this->carTireParams);
}

void CarContext::getCarSpeedParams(CarSpeedParams &params)
{
    params.copyFrom(this->carSpeedParams);
}

void CarContext::getCarSteeringParams(CarSteeringParams &params)
{
    params.copyFrom(this->carSteeringParams);
}

void CarContext::getCarSteeringControlParams(CarSteeringControlParams &params)
{
    params.copyFrom(this->carSteeringControlParams);
}

