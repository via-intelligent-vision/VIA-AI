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

#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui.hpp>
#include "CarModel.h"

#include <android/log.h>
#define  LOG_TAG    "CarModel"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace via::car;
using namespace cv;

const float mass_civic = 1315.35;       //kg
const float wheelbase_civic = 2.70;     //m
const float rotationalInertia_civic = 2500; // kg * m^2
const float centerToFront_civic = wheelbase_civic * 0.4f;   //m
const float centerToRear_civic = wheelbase_civic - centerToFront_civic; //m
const float tireStiffnessFront_civic = 192150;  // N/rad
const float tireStiffnessRear_civic = 202500;   // N/rad
const float tire_stiffness_factor = 0.677f;

#define TAG_Controller          "Controller"
#define TAG_LatPID              "LatitudePID"
#define TAG_ctlP                "ctlP"
#define TAG_ctlI                "ctlI"
#define TAG_ctlD                "ctlD"
#define TAG_ctlF                "ctlF"
#define TAG_ctlK                "ctlK"
#define TAG_Param               "Param"
#define TAG_Param_InMinSpeed    "Param_InMinSpeed"
#define TAG_Param_InMaxSpeed    "Param_InMaxSpeed"
#define TAG_mParam_StiffnessGain    "Param_StiffnessGain"
#define TAG_Param_B                "Param_B"
#define TAG_Param_C                "Param_C"
#define TAG_Param_D                "Param_D"
#define TAG_Param_E                "Param_E"
#define TAG_Param_Friction         "Param_Friction"

//  Speed [ 30 ~ 60 ]  ---- [1.0 - 0.05  ]
template <class T>
T interpolate(T vInMinSpeed, T vInMaxSpeed, T speed, T minSpeed, T maxSpeed) {
    T v;
    if (speed <= minSpeed) {
        v = vInMinSpeed;
    }
    else if (speed >= maxSpeed) {
        v = vInMaxSpeed;
    }
    else {
        v = vInMinSpeed + ((vInMaxSpeed - vInMinSpeed) / (maxSpeed - minSpeed)) * (speed - minSpeed);
    }
    return v;
}


CarModel::CarModel()
{
    this->mParam_InMinSpeed = 0;
    this->mParam_InMaxSpeed = 0;
    this->mParam_StiffnessGain = 1.0f;

    this->mParam_B =  -0.168112f;
    this->mParam_C = 1.4216f;
    this->mParam_D = 4667.0f;
    this->mParam_E = -0.16f;
    this->mParam_Friction = 1.0f;
}

CarModel::~CarModel()
{
}

void CarModel::init(via::car::CarContext &carCtx)
{
    this->carCtx.copyFrom(carCtx);
}

bool CarModel::load(const std::string &xmlPath)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        cv::FileStorage storage;
        bool ret = storage.open(xmlPath, cv::FileStorage::READ);
        if (!ret) {
            errStream << "Open file error :" << xmlPath;
            throw std::runtime_error(errStream.str());
        }
        ret = this->load(storage);
        storage.release();
    }
    catch (cv::Exception &error) {   //it's a fatal error, throw to abort
        LOGE("[opencv exception] %s", error.what());
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        LOGE("[runtime error] %s", error.what());
    }

    return ret;
}

bool CarModel::load(cv::FileStorage &fs)
{
    bool ret = false;
    std::ostringstream errStream;

    try {
        FileNode nodeController;    // Due to compatibility, <LDWS> <FCWS> <Camera> is accepted.
        if (fs[TAG_Controller].isNamed()) {
            nodeController = fs[TAG_Controller];
        }

        if (!nodeController.isNone()) {
            FileNode nodeLatPID;
            if (nodeController[TAG_LatPID].isNamed()) {
                nodeLatPID = nodeController[TAG_LatPID];
            }

            if (!nodeLatPID.isNone()) {
                if (nodeLatPID[TAG_ctlK].isNamed()) {
                    FileNode nodeCtl = nodeLatPID[TAG_ctlK];
                    nodeCtl[TAG_Param_InMinSpeed] >> this->mParam_InMinSpeed;
                    nodeCtl[TAG_Param_InMaxSpeed] >> this->mParam_InMaxSpeed;
                    nodeCtl[TAG_mParam_StiffnessGain] >> this->mParam_StiffnessGain;
                    nodeCtl[TAG_Param_B] >> this->mParam_B;
                    nodeCtl[TAG_Param_C] >> this->mParam_C;
                    nodeCtl[TAG_Param_D] >> this->mParam_D;
                    nodeCtl[TAG_Param_E] >> this->mParam_E;
                    nodeCtl[TAG_Param_Friction] >> this->mParam_Friction;
                }
                ret = true;
            }
            else {

            }
        }
        else {
            errStream << "No element <" << TAG_Controller << "> in this file";
        }
    }
    catch (std::runtime_error &error) {   //it's a fatal error, throw to abort
        throw error;
    }

    static bool dbg = true;
    if(dbg) {
        LOGE("10km speedGain %f", interpolate<float>(mParam_InMinSpeed, mParam_InMaxSpeed, 10, 30, 70));
        LOGE("69km speedGain %f", interpolate<float>(mParam_InMinSpeed, mParam_InMaxSpeed, 69, 30, 70));
        LOGE("31km speedGain %f", interpolate<float>(mParam_InMinSpeed, mParam_InMaxSpeed, 31, 30, 70));
        LOGE("50km speedGain %f", interpolate<float>(mParam_InMinSpeed, mParam_InMaxSpeed, 50, 30, 70));
        LOGE("this->mParam_StiffnessGain %f", this->mParam_StiffnessGain);
        LOGE("Tire param : this->mParam_B %f", this->mParam_B);
        LOGE("Tire param : this->mParam_C %f", this->mParam_C);
        LOGE("Tire param : this->mParam_D %f", this->mParam_D);
        LOGE("Tire param : this->mParam_E %f", this->mParam_E);
        LOGE("Tire param : this->mParam_Friction %f", this->mParam_Friction);
        dbg = false;
    }
    return ret;
}

float CarModel::TireModel(float slipAngle)
{
    float B = 0.21112;
    float C = 1.36;
    float D = 6677;
    float E = -2.16;
    float u = 1.0f;

    slipAngle *= 180.0f / CV_PI;

    //B = -0.308112;
    //B = -0.208112;
    B = this->mParam_B;
    C = this->mParam_C;
    D = this->mParam_D;
    E = this->mParam_E;
    u = this->mParam_Friction;

    /*B = -0.188112;
    C = 1.4216;
    D = 4677;
    E = -2.16;*/
    // Latitude force
    float theta_deg = (1 - E) * slipAngle + (E / B) * atan(B * slipAngle);
    float F = u * D * sin(C * atan(B * theta_deg));
    return F;
}

void CarModel::getDrivingTrajectory(float timeCount, int timeStepCount, float velocity_kmh,
                                    float steerAngleStart, float steerAngleEnd,
                                    float steerTurningRate, float steerMaxTrunRate, float steerControlDelayTime_s,
                                    PathNode *tC)
{
    steerAngleStart *= -1;
    steerAngleEnd *= -1;
    steerTurningRate *= -1;

    // try to find slice lead time slice to 0.01s at least.
    int TIME_SLICE_GAIN = 8;
    do {
        float t = timeCount / 0.01f;
        t /= timeStepCount;

        TIME_SLICE_GAIN = (int)ceil(t);
    } while (false);

    int timeSlice = (timeStepCount -1) * TIME_SLICE_GAIN;
    //printf("TIME_SLICE_GAIN %d\n", TIME_SLICE_GAIN);

    // Dynamic Bicycle Model
    do {
        float cameraToFrontWheel_cm = -75;
        CarSteeringParams carSteeringParams;
        CarAxleParams carAxleParams;
        CarBodyShellParams carBodyShellParams;
        carCtx.getCarAxleParams(carAxleParams);
        carCtx.getCarSteeringParams(carSteeringParams);
        carCtx.getCarBodyShellParams(carBodyShellParams);
        const float lf = 0.01f * carAxleParams.wheelBase * carAxleParams.ratioCenterToFront;
        const float lr = 0.01f * carAxleParams.wheelBase * (1.0f - carAxleParams.ratioCenterToFront);

        const float tireStiffnessFront = (tireStiffnessFront_civic * tire_stiffness_factor) *
                                         (carBodyShellParams.vehicleMass / mass_civic) *
                                         (lr / (0.01f * carAxleParams.wheelBase)) / (centerToRear_civic / wheelbase_civic);
        const float tireStiffnessRear = (tireStiffnessRear_civic * tire_stiffness_factor) *
                                        (carBodyShellParams.vehicleMass / mass_civic) *
                                        (lf / (0.01f * carAxleParams.wheelBase)) / (centerToFront_civic / wheelbase_civic);
        const float rotationalInertia = this->mParam_StiffnessGain * rotationalInertia_civic * carBodyShellParams.vehicleMass *
                                        ((0.01f * carAxleParams.wheelBase) * (0.01f * carAxleParams.wheelBase)) / (mass_civic * wheelbase_civic* wheelbase_civic);

        //steer angel to wheel angel
        steerAngleEnd   /= carSteeringParams.steeringRatio;
        steerAngleStart /= carSteeringParams.steeringRatio;
        steerMaxTrunRate   /= carSteeringParams.steeringRatio;
        steerTurningRate /= carSteeringParams.steeringRatio;

        // to Rad
        steerAngleEnd   *= CV_PI / 180.0f;
        steerAngleStart *= CV_PI / 180.0f;
        steerMaxTrunRate *= CV_PI / 180.0f;
        steerTurningRate *= CV_PI / 180.0f;

        float dir = ((steerAngleEnd - steerAngleStart) < 0) ? -1.0f : 1.0f;
        float dt = timeCount / (timeSlice - 1);

        float v_ms = (velocity_kmh * 1000.0f) / 3600.0f;
        float vx = v_ms;
        float vy = 0;
        float yaw = 0;
        float dYaw = 0;
        float ddYaw = 0;
        float ax = 0;
        float ay = 0;
        float X = 0;
        float Y = 0;
        float curSteer = steerAngleStart;

        for (int ti = 0; ti < timeSlice; ti++) {
            float time = ti * dt;

            // steering
            if (time > steerControlDelayTime_s) {
                curSteer += steerMaxTrunRate * dt * dir;
            }
            else {
                curSteer += steerTurningRate * dt;
            }


            if (dir > 0 && curSteer >= steerAngleEnd) {
                curSteer = steerAngleEnd;
            }
            else if (dir < 0 && curSteer <= steerAngleEnd) {
                curSteer = steerAngleEnd;
            }


            float vyf = vy + lf * dYaw;
            float vyr = vy - lr * dYaw;
            float vxf = vx;
            float vxr = vx;

            float steerf = curSteer;
            float steerr = 0;

            float vlf = vyf * sin(steerf) + vxf * cos(steerf);
            float vlr = vyr * sin(steerr) + vxr * cos(steerr);
            float vcf = vyf * cos(steerf) - vxf * sin(steerf);
            float vcr = vyr * cos(steerr) - vxr * sin(steerr);

            float thetaf = atan2(vcf, vlf); // slip angle
            float thetar = atan2(vcr, vlr);

            float Flf = 0;
            float Flr = 0;
            float Fcf = TireModel(thetaf);    // Force apply in front wheel
            float Fcr = TireModel(thetar);    // Force apply in rear wheel

            //printf("Flf %f , ori %f\n", Flf, 0.5f * tireStiffnessFront * alphaf);

            float Fyf = Flf * sin(steerf) + Fcf * cos(steerf);
            float Fyr = Flr * sin(steerr) + Fcr * cos(steerr);
            float Fxf = Flf * cos(steerf) - Fcf * sin(steerf);
            float Fxr = Flr * cos(steerr) - Fcr * sin(steerr);


            ax =  vy * dYaw + 2 * (Fxf + Fxr) / carBodyShellParams.vehicleMass;
            ay = -vx * dYaw + 2 * (Fyf + Fyr) / carBodyShellParams.vehicleMass;
            vx += ax * dt;
            vy += ay * dt;


            ddYaw = 2 * (lf * Fyf - lr * Fyr) / (rotationalInertia);
            dYaw += ddYaw * dt;
            yaw += dYaw *dt;


            X = X + ((vx * cos(yaw) - vy * sin(yaw)) * dt);
            Y = Y + ((vx * sin(yaw) + vy * cos(yaw)) * dt);
            //printf("steeringTurnAngle %f , yaw %f\n", steeringTurnAngle * 180.0 / CV_PI, yaw * 180.0 / CV_PI);

            if ((ti + 1) % TIME_SLICE_GAIN == 0) {
                int id = (ti + 1) / TIME_SLICE_GAIN;
                tC[id].x = X;
                tC[id].y = Y;
                tC[id].yaw = yaw;
                tC[id].steerAngle = steerf;
                tC[id].lateralAccel = ay;
                tC[id].time = time;
            }

        }

        // coordinate trnasform
        tC[0].x = 0;
        tC[0].y = 0;
        tC[0].yaw = 0;
        tC[0].steerAngle = (float)(steerAngleStart * 180.0f / CV_PI) * carSteeringParams.steeringRatio * -1.0f;
        tC[0].lateralAccel = 0;
        for (int ti = 0; ti < timeStepCount; ti++) {
            float tmp = tC[ti].x;
            tC[ti].x = -100.0f * tC[ti].y;
            tC[ti].y = 100.0f * tmp + cameraToFrontWheel_cm;
            tC[ti].steerAngle = (float)(tC[ti].steerAngle * 180.0f / CV_PI) * carSteeringParams.steeringRatio * -1.0f;
            tC[ti].lateralAccel *= -1;
        }
    } while (false);


#ifdef DEPRECATED_20181019
    steerAngleStart *= -1;
    steerAngleEnd *= -1;
    lanePredictSideVelocity_ms *= -1;
    if(lanePredictSideVelocity_ms >  5.0f) lanePredictSideVelocity_ms = 5.0f;
    if(lanePredictSideVelocity_ms < -5.0f) lanePredictSideVelocity_ms = -5.0f;

    // try to find slice lead time slice to 0.01s at least.
    int TIME_SLICE_GAIN = 8;
    do {
        float t = timeCount / 0.01f;
        t /= timeStepCount;

        TIME_SLICE_GAIN = (int)ceil(t);
        if(TIME_SLICE_GAIN < 6) TIME_SLICE_GAIN = 6;
    } while (false);

    int timeSlice = timeStepCount * TIME_SLICE_GAIN;
    //printf("TIME_SLICE_GAIN %d\n", TIME_SLICE_GAIN);
    //LOGE("timeSlice %d\n", timeSlice);

    // Dynamic Bicycle Model
    do {
        float cameraToFrontWheel_cm = 75;
        float a_cmss = 0.0f;
        CarSteeringParams carSteeringParams;
        CarAxleParams carAxleParams;
        CarBodyShellParams carBodyShellParams;
        carCtx.getCarAxleParams(carAxleParams);
        carCtx.getCarSteeringParams(carSteeringParams);
        carCtx.getCarBodyShellParams(carBodyShellParams);
        const float lf = 0.01f * carAxleParams.wheelBase * carAxleParams.ratioCenterToFront;
        const float lr = 0.01f * carAxleParams.wheelBase * (1.0f - carAxleParams.ratioCenterToFront);

        const float tireStiffnessFront = (tireStiffnessFront_civic * tire_stiffness_factor) *
                                         (carBodyShellParams.vehicleMass / mass_civic) *
                                         (lr / (0.01f * carAxleParams.wheelBase)) / (centerToRear_civic / wheelbase_civic);
        const float tireStiffnessRear = (tireStiffnessRear_civic * tire_stiffness_factor) *
                                        (carBodyShellParams.vehicleMass / mass_civic) *
                                        (lf / (0.01f * carAxleParams.wheelBase)) / (centerToFront_civic / wheelbase_civic);
        const float rotationalInertia = rotationalInertia_civic * carBodyShellParams.vehicleMass *
                                        ((0.01f * carAxleParams.wheelBase) * (0.01f * carAxleParams.wheelBase)) / (mass_civic * wheelbase_civic* wheelbase_civic);

        //steer angel to wheel angel
        steerAngleEnd   /= carSteeringParams.steeringRatio;
        steerAngleStart /= carSteeringParams.steeringRatio;
        steerMaxTrunRate   /= carSteeringParams.steeringRatio;

        //LOGE("steerAngleStart %f , steerAngleEnd %f , steerMaxTrunRate %f\n", steerAngleStart, steerAngleEnd, steerMaxTrunRate);

        float dir = ((steerAngleEnd - steerAngleStart) < 0) ? -1.0f : 1.0f;
        float timeStep = (timeCount - steerControlDelayTime_s) / (timeSlice - 1);


        float v_cms = (velocity_kmh * 100000.0f) / 3600.0f;
        float v_ms = (velocity_kmh * 1000.0f) / 3600.0f;
        //float vx = v_ms;
        //float vy = 0;
        float vy = 0;
        float vx = 0;
        if(v_ms > fabs(lanePredictSideVelocity_ms)) {
            vy = lanePredictSideVelocity_ms;
            vx = sqrtf(v_ms * v_ms - vy * vy);
        }
        else {
            vx = v_ms;
            vy = 0;
        }

        float yaw = 0;
        float dYaw = 0;
        float ddYaw = 0;
        float ax = 0;
        float ay = 0;
        float X = 0;
        float Y = 0;

        float inertialHeading = 0.0f;
        float prevSteeringTurnAngle = 0;
        for (int ti = 0; ti < timeSlice; ti++) {
            float time = ti * timeStep;
            float steeringTurnAngle = 0;
            if(time < steerControlDelayTime_s) {
                steeringTurnAngle = steerAngleStart;
            }
            else {
                steeringTurnAngle = steerAngleStart + steerMaxTrunRate * ti * timeStep * dir;
            }

            if (dir > 0 && steeringTurnAngle >= steerAngleEnd) {
                steeringTurnAngle = steerAngleEnd;
            }
            else if (dir < 0 && steeringTurnAngle <= steerAngleEnd) {
                steeringTurnAngle = steerAngleEnd;
            }

            float dt = timeStep;

            // to Rad
            steeringTurnAngle *= CV_PI / 180.0f;

            // get init position
            /*if (ti == 0) {
                tC[ti].x = 0.0f;
                tC[ti].y = 0.0f;
            }
            // get trajectory after control delay
            else */
            {
                float thetaf = (float)atan2(vy + lf * dYaw, vx);
                float thetar = (float)atan2(vy - lr * dYaw, vx);
                float alphaf = steeringTurnAngle - thetaf;
                float alphar = 0 - thetar;
                float Flf = this->mParam_StiffnessGain * tireStiffnessFront * alphaf;    // Force apply in front wheel
                float Flr = this->mParam_StiffnessGain * tireStiffnessRear  * alphar;    // Force apply in rear wheel
                //LOGE("this->mParam_StiffnessGain %f", this->mParam_StiffnessGain);

                Point2f Ff;
                Point2f Fr;
                Ff.x = 0;
                Ff.y = Flf;
                Fr.x = 0; // no rotation in rear wheel.
                Fr.y = Flr;


                ddYaw = 2 * (lf * Ff.y - lr * Fr.y) / (rotationalInertia);
                dYaw += ddYaw * dt;
                yaw += dYaw *dt;


                ax = dYaw * vy;
                ay = -dYaw * vx + 2 * (Ff.y * cos(steeringTurnAngle) + Fr.y) / carBodyShellParams.vehicleMass;
                vx += ax * dt;
                vy += ay * dt;

                X = X + (vx * cos(yaw) - vy * sin(yaw)) * dt;
                Y = Y + (vx * sin(yaw) + vy * cos(yaw)) * dt;
                //printf("steeringTurnAngle %f , yaw %f\n", steeringTurnAngle * 180.0 / CV_PI, yaw * 180.0 / CV_PI);

                if ((ti +1) % TIME_SLICE_GAIN == 0) {
                    int id = (ti + 1) / TIME_SLICE_GAIN -1;
                    tC[id].x = X;
                    tC[id].y = Y;
                }
            }
        }

        // coordinate trnasform
        for (int ti = 0; ti < timeStepCount; ti++) {
            float tmp = tC[ti].x;
            tC[ti].x = -100.0f * tC[ti].y;
            tC[ti].y = 100.0f * tmp + cameraToFrontWheel_cm;
        }
    } while (false);
#endif

}

void CarModel::getDrivingTrajectory_K(float timeCount, int timeStepCount, float velocity_kmh,
    float steerAngleStart, float steerAngleEnd, float steerTrunRate, float steerControlDelayTime_s,
    PathNode *tC)
{
    float speedGain = interpolate<float>(mParam_InMinSpeed, mParam_InMaxSpeed, velocity_kmh, 30, 70);
    //float speedGain = 1;
    steerAngleStart *= -1;
    steerAngleEnd *= -1;

    // try to find slice lead time slice to 0.01s at least.
    int TIME_SLICE_GAIN = 8;
    do {
        float t = timeCount / 0.01f;
        t /= timeStepCount;
        TIME_SLICE_GAIN = (int)ceil(t);
    } while (false);
    if (TIME_SLICE_GAIN < 1) TIME_SLICE_GAIN = 1;

    int timeSlice = (timeStepCount -1)* TIME_SLICE_GAIN;

    // Kinematic Bicycle Model
    do {
        float cameraToFrontWheel_cm = -75;
        float a_cmss = 0.0f;
        CarSteeringParams carSteeringParams;
        CarAxleParams carAxleParams;
        carCtx.getCarAxleParams(carAxleParams);
        carCtx.getCarSteeringParams(carSteeringParams);
        const float lf = carAxleParams.wheelBase * carAxleParams.ratioCenterToFront;
        const float lr = carAxleParams.wheelBase * (1.0f - carAxleParams.ratioCenterToFront);

        //steer angel to wheel angel
        steerAngleEnd /= carSteeringParams.steeringRatio;
        steerAngleStart /= carSteeringParams.steeringRatio;
        steerTrunRate /= carSteeringParams.steeringRatio;

        float dir = ((steerAngleEnd - steerAngleStart) < 0) ? -1.0f : 1.0f;
        float steerTurnStep = (steerAngleEnd - steerAngleStart) / (timeSlice - 1);
        float dt = timeCount / (timeSlice - 1);

        float v_cms = (velocity_kmh * 100000.0f) / 3600.0f;
        float X = 0;
        float Y = 0;
        float curSteer = steerAngleStart;
        float inertialHeading = (float)(0.0f * CV_PI /180.0f);

        for (int ti = 0; ti < timeSlice; ti++) {
            float time = ti * dt;

            // steering
            if (time > steerControlDelayTime_s) {
                curSteer += steerTrunRate * dt * dir;
            }
            if (dir > 0 && curSteer >= steerAngleEnd) {
                curSteer = steerAngleEnd;
            }
            else if (dir < 0 && curSteer <= steerAngleEnd) {
                curSteer = steerAngleEnd;
            }


            // get init position
            if (ti == 0) {
                tC[ti].x = 0.0f;
                tC[ti].y = 0.0f;
            }
                // get trajectory after control delay
            else {
                double beta = atan((lr / (lr + lf)) * tan(curSteer * CV_PI / 180.0f));
                X = X + (v_cms * (float)cos((inertialHeading + beta) * speedGain) * dt);
                Y = Y + (v_cms * (float)sin((inertialHeading + beta) * speedGain) * dt);

                inertialHeading += (v_cms / lr) * sin(beta) * dt;
                //inertialHeading += (v_cms * cos(beta) / carAxleParams.wheelBase) * tan(curSteer * CV_PI / 180.0f) * dt;
                v_cms += a_cmss * dt;
            }

            if ((ti + 1) % TIME_SLICE_GAIN == 0) {
                int id = ((ti + 1) / TIME_SLICE_GAIN);
                tC[id].x = X;
                tC[id].y = Y;
                tC[id].yaw = inertialHeading;
            }
        }

        // convert to object space.
        tC[0].x = 0;
        tC[0].y = 0;
        tC[0].yaw = 0;
        for (int ti = 0; ti < timeStepCount; ti++) {
            float tmp = tC[ti].x;
            tC[ti].x = -tC[ti].y;
            tC[ti].y = tmp + cameraToFrontWheel_cm;
        }
    } while (false);

    // 2018/10/19 version
#ifdef DEPRECATED_20181019
    float cameraToFrontWheel_cm = 75;
    float a_cmss = 0.0f;
    CarSteeringParams carSteeringParams;
    CarAxleParams carAxleParams;
    carCtx.getCarAxleParams(carAxleParams);
    carCtx.getCarSteeringParams(carSteeringParams);
    const float lf = carAxleParams.wheelBase * carAxleParams.ratioCenterToFront;
    const float lr = carAxleParams.wheelBase * (1.0f - carAxleParams.ratioCenterToFront);

    //steer angel to wheel angel
    steerAngleEnd /= carSteeringParams.steeringRatio;
    steerAngleStart /= carSteeringParams.steeringRatio;
    steerTrunRate /= carSteeringParams.steeringRatio;

    float dir = ((steerAngleEnd - steerAngleStart) < 0) ? -1.0f : 1.0f;
    float timeStep = timeCount / (timeStepCount - 1);


    float v_cms = (velocity_kmh * 100000.0f) / 3600.0f;

    float inertialHeading = 0.0f;
    for (int ti = 0; ti < timeStepCount; ti++) {
        float steeringTurnAngle = steerAngleStart + steerTrunRate * ti * timeStep * dir;
        if (dir > 0 && steeringTurnAngle >= steerAngleEnd) {
            steeringTurnAngle = steerAngleEnd;
        }
        else if (dir < 0 && steeringTurnAngle <= steerAngleEnd) {
            steeringTurnAngle = steerAngleEnd;
        }


        // get init position
        if (ti == 0) {
            tC[ti].x = 0.0f;
            tC[ti].y = 0.0f;
        }
        // get trajectory after control delay
        else {
            float dt = timeStep;
            double beta = atan( (lr / (lr + lf)) * tan(steeringTurnAngle * CV_PI / 180.0f));
            tC[ti].x = tC[ti - 1].x + v_cms * (float)cos((inertialHeading + beta) * speedGain) * dt;
            tC[ti].y = tC[ti - 1].y + v_cms * (float)sin((inertialHeading + beta) * speedGain) * dt;
            inertialHeading += (v_cms / lr) * sin(beta) * dt;
            v_cms += a_cmss * dt;
        }
    }


    for (int ti = 0; ti < timeStepCount; ti++) {
        float tmp = tC[ti].x;
        tC[ti].x = tC[ti].y;
        tC[ti].y = tmp + cameraToFrontWheel_cm;
    }
#endif

}