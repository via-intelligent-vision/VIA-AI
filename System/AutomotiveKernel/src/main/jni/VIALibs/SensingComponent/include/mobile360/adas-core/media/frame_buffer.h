#ifndef VIA_ADAS_FRAMEBUFFER_YUV420_H
#define VIA_ADAS_FRAMEBUFFER_YUV420_H

#include <mutex>

class FrameBuffer {

public:
    enum class Type : unsigned char {
        FRAME_TYPE_UNKNOWN,
        FRAME_TYPE_YUV_420_PLANAR,
        FRAME_TYPE_YUV_420_SEMI_PLANAR_NV12,
        FRAME_TYPE_YUV_420_SEMI_PLANAR_NV21,
        FRAME_TYPE_RGB_888_SEMI_PLANAR,
        FRAME_TYPE_RGB_888_PLANAR,
        FRAME_TYPE_BGR_888_SEMI_PLANAR,
        FRAME_TYPE_BGR_888_PLANAR,
        FRAME_TYPE_RGBA_8888_SEMI_PLANAR,
        FRAME_TYPE_RGBA_8888_PLANAR,
    };


    FrameBuffer();
    ~FrameBuffer();
    void init(int frameWidth, int frameHeight,
              int yStepStride, int uStepStride, int vStepStride,
              int yPixelStride, int uPixelStride, int vPixelStride,
              int roiX, int roiY, int roiWidth, int roiHeight);
    void init(Type srcType, int frameWidth, int frameHeight, int roiX, int roiY, int roiWidth, int roiHeight);


    void release();
    bool put(unsigned char *yFrame, unsigned char *uFrame, unsigned char *vFrame);
    bool put(Type srcType, unsigned char *pChannel_0, unsigned char *pChannel_1, unsigned char *pChannel_2, unsigned char *pChannel_3);
    bool compare(int frameWidth, int frameHeight,
                 int yStepStride, int uStepStride, int vStepStride,
                 int yPixelStride, int uPixelStride, int vPixelStride,
                 int roiX, int roiY, int roiWidth, int roiHeight);
    bool isInit();
    unsigned char *ptr();
    int getBufferWidth();
    int getBufferHeight();
    Type getBufferFrameType();
    Type getSrcFrameType();

private :
    std::mutex mBufferMutex;
    unsigned char *mBuffer;

    Type mSrcFrameType;
    int mSrcFrameWidth;
    int mSrcFrameHeight;
    int mSrcFrameStride_Y;
    int mSrcFrameStride_U;
    int mSrcFrameStride_V;
    int mSrcFramePixelStride_Y;
    int mSrcFramePixelStride_U;
    int mSrcFramePixelStride_V;

    Type mBufferType;
    int mSrcFrameROI_x;
    int mSrcFrameROI_y;
    int mBufferWidth;
    int mBufferHeight;

    int checkFrameFromat(int frameWidth, int frameHeight,
                        int yStepStride, int uStepStride, int vStepStride,
                        int yPixelStride, int uPixelStride, int vPixelStride);

    bool put_From_YUV420_Planar(unsigned char *yFrame, unsigned char *uFrame, unsigned char *vFrame);
    bool put_From_YUV420_SemiPlanar(unsigned char *yFrame, unsigned char *uFrame, unsigned char *vFrame);

    bool put_From_RGB888_Planar(unsigned char *rFrame, unsigned char *gFrame, unsigned char *bFrame);
    bool put_From_RGB888_SemiPlanar(unsigned char *rFrame, unsigned char *gFrame, unsigned char *bFrame);

    bool put_From_BGR888_Planar(unsigned char *bFrame, unsigned char *gFrame, unsigned char *rFrame);
    bool put_From_BGR888_SemiPlanar(unsigned char *bFrame, unsigned char *gFrame, unsigned char *rFrame);

    bool put_From_RGBA8888_SemiPlanar(unsigned char *rFrame, unsigned char *gFrame, unsigned char *bFrame, unsigned char *aFrame);
    

};


#endif //VIA_ADAS_FRAMEBUFFER_YUV420_H
