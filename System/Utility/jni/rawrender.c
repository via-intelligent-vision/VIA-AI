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

#include "colorConvert.h"
#include <string.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <malloc.h>
#include "math.h"
#define TAG    "ADAS"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define GO_GENERAL_PATH 0
#define FAST_COPY_YV21 0


#define ENABLE_VERIFY_SIGN 0
#define ENABLE_VERIFY_PLATFORM 1

// This definition is copied from https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html#COLOR_FormatYUV420Flexible
int COLOR_FormatYUV420Planar = 0x13;

//Please refer to the ImageFormat definition from below path
//C:\Users\RichardChen\AppData\Local\Android\sdk\sources\android-24\android\graphics\ImageFormat.java
enum{
    ImageFormat_UNKNOWN = 0,
    ImageFormat_RGB_565 = 4,
    ImageFormat_YV12 = 0x32315659,
    ImageFormat_Y8 = 0x20203859,
    ImageFormat_Y16 = 0x20363159,
    ImageFormat_NV16 = 0x10,
    ImageFormat_NV21 = 0x11,
    ImageFormat_YUY2 = 0x14,
    ImageFormat_JPEG = 0x100,
    ImageFormat_YUV_420_888 = 0x23,
    ImageFormat_YUV_422_888 = 0x27,
    ImageFormat_YUV_444_888 = 0x28,
    ImageFormat_FLEX_RGB_888 = 0x29,
    ImageFormat_FLEX_RGBA_8888 = 0x2A,
    ImageFormat_RAW_SENSOR = 0x20,
    ImageFormat_RAW_PRIVATE = 0x24,
    ImageFormat_RAW10 = 0x25,
    ImageFormat_RAW12 = 0x26,
    ImageFormat_DEPTH16 = 0x44363159,
    ImageFormat_DEPTH_POINT_CLOUD = 0x101,
    ImageFormat_PRIVATE = 0x22
};

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_NV21TOYUV420SP
(JNIEnv * env, jobject thiz, jbyteArray srcarray,jbyteArray dstarray,jint ySize) {
	unsigned char *src = (unsigned char *)(*env)->GetByteArrayElements(env,srcarray, 0);
	unsigned char *dst = (unsigned char*)(*env)->GetByteArrayElements(env,dstarray, 0);
	NV21TOYUV420SP(src,dst,ySize);
	(*env)->ReleaseByteArrayElements(env,srcarray,src,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,dstarray,dst,JNI_ABORT);
	return;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_YUV420SPTOYUV420P
(JNIEnv * env, jobject thiz, jbyteArray srcarray,jbyteArray dstarray,jint ySize) {
	unsigned char *src = (unsigned char *)(*env)->GetByteArrayElements(env,srcarray, 0);
	unsigned char *dst = (unsigned char*)(*env)->GetByteArrayElements(env,dstarray, 0);
	YUV420SPTOYUV420P(src,dst,ySize);
	(*env)->ReleaseByteArrayElements(env,srcarray,src,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,dstarray,dst,JNI_ABORT);
	return;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_NV21TOYUV420P
(JNIEnv * env, jobject thiz, jbyteArray srcarray,jbyteArray dstarray,jint ySize) {
	unsigned char *src = (unsigned char *)(*env)->GetByteArrayElements(env,srcarray, 0);
	unsigned char *dst = (unsigned char*)(*env)->GetByteArrayElements(env,dstarray, 0);
	NV21TOYUV420P(src,dst,ySize);
	(*env)->ReleaseByteArrayElements(env,srcarray,src,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,dstarray,dst,JNI_ABORT);
	return;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_NV21TOARGB
(JNIEnv *env, jobject thiz,jbyteArray srcarray,jintArray dstarray,jint width,jint height){
		unsigned char *src = (unsigned char *)(*env)->GetByteArrayElements(env,srcarray, 0);
		unsigned int *dst = (unsigned int*)(*env)->GetIntArrayElements(env,dstarray, 0);
		NV21TOARGB(src,dst,width,height);
		(*env)->ReleaseByteArrayElements(env,srcarray,src,JNI_ABORT);
		(*env)->ReleaseIntArrayElements(env,dstarray,dst,JNI_ABORT);
		return;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_NV21Transform
(JNIEnv * env, jobject thiz, jbyteArray srcarray,jbyteArray dstarray,jint srcwidth,jint srcheight,jint directionflag) {
	unsigned char *src = (unsigned char*)(*env)->GetByteArrayElements(env,srcarray, 0);
	unsigned char *dst = (unsigned char*)(*env)->GetByteArrayElements(env,dstarray, 0);
	NV21Transform(src,dst,srcwidth,srcheight,directionflag);
	(*env)->ReleaseByteArrayElements(env,srcarray,src,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,dstarray,dst,JNI_ABORT);
	return;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_ColorHelper_FIXGLPIXEL
(JNIEnv * env, jobject thiz, jintArray srcarray,jintArray dstarray,jint w,jint h) {
        unsigned int *src = (unsigned int *)(*env)->GetIntArrayElements(env,srcarray, 0);
        unsigned int *dst = (unsigned int *)(*env)->GetIntArrayElements(env,dstarray, 0);
        FIXGLPIXEL(src,dst,w,h);
        (*env)->ReleaseIntArrayElements(env,srcarray,src,JNI_ABORT);
        (*env)->ReleaseIntArrayElements(env,dstarray,dst,JNI_ABORT);
        return;
}

#define JNI_FALSE 0
#define JNI_TRUE 1

//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingToSurface
(JNIEnv * env, jobject thiz,jobject javaSurface,jbyteArray pixelsArray,jint srcBufferOffset,jint srcWidth,jint srcHeight,jint srcSize) {
	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	if(window!=NULL)
	{
		ANativeWindow_setBuffersGeometry(window,srcWidth,srcHeight,ImageFormat_NV21);
		ANativeWindow_Buffer buffer;
		if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
			unsigned char *pSrc = (unsigned char*)(*env)->GetByteArrayElements(env,pixelsArray, 0);
			unsigned char *pDstBuffer = buffer.bits;

			pSrc = pSrc+srcBufferOffset;

			if(buffer.width==buffer.stride){
				memcpy(pDstBuffer, pSrc,  srcSize);
			}else{
				int height = srcHeight*3/2;
				int width = srcWidth;
				int i=0;
				for(;i<height;++i)
					memcpy(pDstBuffer +  buffer.stride * i
						, pSrc + width * i
						, width);
			}
			(*env)->ReleaseByteArrayElements(env,pixelsArray,pSrc,JNI_ABORT);
			ANativeWindow_unlockAndPost(window);
		}
		ANativeWindow_release(window);
	}
	return;
}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_getPointerFromByteBuffer(JNIEnv * env,jobject thiz,jbyteArray pixelsArray, jint offset) {
	unsigned char *pix = (unsigned char *)(*env)->GetDirectBufferAddress(env,pixelsArray);
	unsigned char *pixels = pix+offset;
	return (jlong) pixels;
}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_copyImageYToDestBuffer(JNIEnv * env,jobject thiz,jbyteArray src, jint src_w, jint src_h,jbyteArray dest, jint x, jint y, jint dest_w, jint dest_h) {

	jbyte* pSrc = (*env)->GetByteArrayElements(env, src, NULL);
	unsigned char *pDest = (unsigned char *)(*env)->GetDirectBufferAddress(env,dest);

	unsigned char *pSrcTmp = pSrc;
	unsigned char *pDestTmp = pDest;

	pDestTmp+=dest_w*y;
	for(int i=0;i<src_h;i++) {
		memcpy((pDestTmp+x), pSrcTmp, src_w);
		pDestTmp+=dest_w;
		pSrcTmp+=src_w;
	}

	(*env)->ReleaseByteArrayElements(env, src, pSrc, JNI_ABORT);

	return (jlong) pDest;

}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_copyYV12ToByteBufferNV12(JNIEnv *env, jobject thiz, jbyteArray src, jint cropW, jint cropH, jint W, jint H, jbyteArray dest) {
    unsigned char *pSrc = (unsigned char *)(*env)->GetDirectBufferAddress(env,src);
    unsigned char *pDest = (unsigned char *)(*env)->GetDirectBufferAddress(env,dest);
    unsigned char *pDestUV = pDest+cropW*cropH;

    unsigned char *pSrcY = pSrc;
    unsigned char *pSrcU = pSrcY+W*H;
    unsigned char *pSrcV = pSrcU+W*H/4;

    for(int i=0;i<cropH;i++) {
        memcpy(pDest+i*cropW, pSrcY+i*W, cropW);
    }

    int k = 0;
    for(int j=0;j<cropH/2;j++) {
        for (int i = 0; i < cropW/2; i++) {
            *(pDestUV+k) = *(pSrcU+j*W/2+i);
            *(pDestUV+k+1) = *(pSrcV+j*W/2+i);
            k = k+2;
        }
    }

    return pDest;
}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_copyYV12ToByteBufferNV21(JNIEnv *env, jobject thiz, jbyteArray src, jint cropW, jint cropH, jint W, jint H, jbyteArray dest) {
    unsigned char *pSrc = (unsigned char *)(*env)->GetDirectBufferAddress(env,src);
    unsigned char *pDest = (unsigned char *)(*env)->GetDirectBufferAddress(env,dest);
    unsigned char *pDestUV = pDest+cropW*cropH;

    unsigned char *pSrcY = pSrc;
    unsigned char *pSrcU = pSrcY+W*H;
    unsigned char *pSrcV = pSrcU+W*H/4;

    for(int i=0;i<cropH;i++) {
        memcpy(pDest+i*cropW, pSrcY+i*W, cropW);
    }

    int k = 0;
    for(int j=0;j<cropH/2;j++) {
        for (int i = 0; i < cropW/2; i++) {
            *(pDestUV+k+1) = *(pSrcU+j*W/2+i);
            *(pDestUV+k) = *(pSrcV+j*W/2+i);
            k = k+2;
        }
    }

    return pDest;
}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_newLookupTable(JNIEnv * env,jobject thiz, jint src_length, jint dest_length) {
	int* table = (int*) malloc(dest_length*sizeof(int));
	float multiX = (float)(dest_length)/src_length;
	for(int i=0;i<dest_length;i++) {
		table[i] = round((i)/multiX);
	}
	return (jlong) table;
}

JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_deleteLookupTable(JNIEnv * env,jobject thiz, jlong lookup_table) {
	int* table = (int*) lookup_table;
	free(table);
}



JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_copyImageYScaleToDestBuffer(JNIEnv * env,jobject thiz,jbyteArray src, jint src_w, jint src_h,jbyteArray dest, jint dest_w, jint dest_h, jlong table_w, jlong table_h) {
    jbyte* pSrc = (*env)->GetByteArrayElements(env, src, NULL);
    unsigned char *pDest = (unsigned char *)(*env)->GetDirectBufferAddress(env,dest);

    unsigned char *pSrcTmp = pSrc;
    unsigned char *pDestTmp = pDest;

	int* scalingIndexTableX = (int*) table_w;
	int* scalingIndexTableY = (int*) table_h;

    for(int i=0;i<dest_h; i++) {
        for (int j = 0; j < dest_w; j++) {
            *(pDestTmp+j+i*dest_w) = *(pSrcTmp+scalingIndexTableX[j]+scalingIndexTableY[i]*src_w);
        }
    }

    (*env)->ReleaseByteArrayElements(env, src, pSrc, JNI_ABORT);
    return (jlong) pDest;
}



JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_copyImageAccordingPitch(JNIEnv * env,jobject thiz, jbyteArray pixelsArray, jint offset,jint src_rowStride, jlong dest_pt,jint width, jint height, jint pitch) {
	unsigned char *pix = (unsigned char *)(*env)->GetDirectBufferAddress(env,pixelsArray);
	unsigned char *pixels = pix+offset;
	unsigned char *dest = (unsigned char*) dest_pt;

	unsigned char *dest_tmp = dest;
	int i = 0;
	while(i<height) {
		memcpy(dest_tmp, pixels, width);
		dest_tmp+=pitch;
		pixels+=src_rowStride;
		i++;
	}

	return (jlong) dest;
}

JNIEXPORT jlong JNICALL Java_com_viatech_utility_tool_NativeRender_allocateMemory(JNIEnv * env,jobject thiz, jint size) {
	unsigned char *dst = (unsigned char*) malloc(size);
	return (jlong) dst;
}


JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_releaseMemory(JNIEnv * env,jobject thiz,jlong pt) {
	unsigned char *pix = (unsigned char*) pt;
	free(pix);
}

//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingToSurface2
(JNIEnv * env, jobject thiz,jobject javaSurface,jbyteArray pixelsArray,jint srcBufferOffset,jint srcWidth,jint srcHeight,jint srcSize, jint srcColorFormat, jint srcStride) {
	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	if(window!=NULL)
	{
        if(srcColorFormat == COLOR_FormatYUV420Planar) { //Because ImageFormat do not support YV21, we set Buffer format YV12
            ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_YV12);
        }
        else { //for nv21
            ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_NV21);
        }
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
            unsigned char *pSrc = (unsigned char *) (*env)->GetDirectBufferAddress(env,
                                                                                       pixelsArray);
            pSrc = pSrc + srcBufferOffset;
            unsigned char *pDstBuffer = buffer.bits;
			LOGD("Buffer Stride:%d",buffer.stride);

            //for srcFormat == YV21 to dstFormat == YV12
            if(srcColorFormat == COLOR_FormatYUV420Planar){
                int i=0, j=0, k=0;

                for(;i<srcHeight;++i)
                    memcpy(pDstBuffer +  buffer.stride * i
                            , pSrc + srcStride * i
                            , srcWidth);

                for(j = srcHeight;j<srcHeight*5/4;++j)
                    memcpy(pDstBuffer +  buffer.stride * (j+buffer.height/4)
                            , pSrc + srcStride * j
                            , srcWidth);

                for(k = srcHeight*5/4;k<srcHeight*3/2;++k)
                    memcpy(pDstBuffer +  buffer.stride * (k-buffer.height/4)
                            , pSrc + srcStride * k
                            , srcWidth);
            }
            //for srcFormat == dstFormat
            else if (buffer.width == buffer.stride) {
                memcpy(pDstBuffer, pSrc, srcSize);
            }
            else {
                int yuvHeight = srcHeight * 3 / 2;
                int i = 0;

                for (; i < yuvHeight; ++i)
                    memcpy(pDstBuffer + buffer.stride * i, pSrc + srcWidth * i, srcWidth);
            }
            ANativeWindow_unlockAndPost(window);
        }
        ANativeWindow_release(window);
	}
	return;
}


//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingPtToSurface
		(JNIEnv * env, jobject thiz,jobject javaSurface,jlong sourcePt,jint srcWidth,jint srcHeight,jint srcSize, jint srcColorFormat, jint srcStride) {
	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	if(window!=NULL)
	{
		if(srcColorFormat == COLOR_FormatYUV420Planar) { //Because ImageFormat do not support YV21, we set Buffer format YV12
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_YV12);
		}
		else { //for nv21
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_NV21);
		}
		ANativeWindow_Buffer buffer;
		if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
			unsigned char *pSrc = (unsigned char *) sourcePt;
			unsigned char *pDstBuffer = buffer.bits;
			LOGD("Buffer Stride:%d",buffer.stride);

			//for srcFormat == YV21 to dstFormat == YV12
			if(srcColorFormat == COLOR_FormatYUV420Planar){
				int i=0, j=0, k=0;

				for(;i<srcHeight;++i)
					memcpy(pDstBuffer +  buffer.stride * i
							, pSrc + srcStride * i
							, srcWidth);

				for(j = srcHeight;j<srcHeight*5/4;++j)
					memcpy(pDstBuffer +  buffer.stride * (j+buffer.height/4)
							, pSrc + srcStride * j
							, srcWidth);

				for(k = srcHeight*5/4;k<srcHeight*3/2;++k)
					memcpy(pDstBuffer +  buffer.stride * (k-buffer.height/4)
							, pSrc + srcStride * k
							, srcWidth);
			}
				//for srcFormat == dstFormat
			else if (buffer.width == buffer.stride) {
				memcpy(pDstBuffer, pSrc, srcSize);
			}
			else {
				int yuvHeight = srcHeight * 3 / 2;
				int i = 0;

				for (; i < yuvHeight; ++i)
					memcpy(pDstBuffer + buffer.stride * i, pSrc + srcWidth * i, srcWidth);
			}
			ANativeWindow_unlockAndPost(window);
		}
		ANativeWindow_release(window);
	}
	return;
}

//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingTopOrBottonHalfToSurface
		(JNIEnv * env, jobject thiz,jobject javaSurface,jbyteArray pixelsArray,jint srcBufferOffset,jint type,jint srcWidth,jint srcHeight,jint size, jint srcColorFormat, jint srcStride) {
	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	if(window!=NULL)
	{
        if(srcColorFormat == COLOR_FormatYUV420Planar) { //Because ImageFormat do not support YV21, we set Buffer format YV12
            ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight / 2, ImageFormat_YV12);
        }
        else { //for nv21
            ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight / 2, ImageFormat_NV21);
        }
		ANativeWindow_Buffer dstBuffer;
		if (ANativeWindow_lock(window, &dstBuffer, NULL) == 0) {
			unsigned char *pSrc = (unsigned char*)(*env)->GetDirectBufferAddress(env,pixelsArray);
			pSrc = pSrc+srcBufferOffset;

			int height = srcHeight/2;
			int width = srcWidth;
			int i;

            int yOffset=type*(srcWidth*srcHeight/2);
            int uvOffsetForNV21=type*(srcWidth*srcHeight/4);
            int uvOffserForYV21=type*(srcWidth*srcHeight/8);

			unsigned char* pSrcYPlane = pSrc + yOffset;
			unsigned char* pSrcUVPlane = pSrc + srcWidth*srcHeight;

			unsigned char* pDstBuffer = dstBuffer.bits;

			memset(pDstBuffer,0,height*width*3/2);

            //step1 : Src y to Dst y
			for(i=0;i<height;++i) {
				memcpy(pDstBuffer, pSrcYPlane, width);
				pDstBuffer+=dstBuffer.stride;
				pSrcYPlane+=width;
			}

            //step2 : Src uv to Dst uv
            if(srcColorFormat == COLOR_FormatYUV420Planar){ // step 2 ,case 1:for srcFormat == YV21 to dstFormat == YV12
                pSrcUVPlane += uvOffserForYV21;//jump to corresponding Src U plane
                for (i = 0; i < srcHeight / 8; ++i) {
                    int j = 0;
                    for (; j < width; ++j) {
                        *(pDstBuffer + j) = *(pSrcUVPlane + j + srcStride*srcHeight/4);
                    }
                    pDstBuffer += dstBuffer.stride;
                    pSrcUVPlane += srcStride;
                }
                pSrcUVPlane +=srcStride*srcHeight/8;//jump to corresponding Src V plane
                for (i = srcHeight / 8; i < srcHeight / 4; ++i) {
                    int j = 0;
                    for (; j < width; ++j) {
                        *(pDstBuffer + j) = *(pSrcUVPlane + j - srcStride*srcHeight/4);
                    }
                    pDstBuffer += dstBuffer.stride;
                    pSrcUVPlane += srcStride;
                }
            }
            else {//step 2 ,case 2: for nv21
                pSrcUVPlane += uvOffsetForNV21;
                for (i = 0; i < height / 2; ++i) {
                    int j = 0;
                    for (; j < width / 2; ++j) {
                        *(pDstBuffer + j * 2 + 0) = *(pSrcUVPlane + j * 2 + 0);
                        *(pDstBuffer + j * 2 + 1) = *(pSrcUVPlane + j * 2 + 1);
                    }
                    pDstBuffer += dstBuffer.stride;
                    pSrcUVPlane += width;
                }
            }
			ANativeWindow_unlockAndPost(window);
		}
		ANativeWindow_release(window);
	}
	return;
}

//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingYUV
(JNIEnv * env, jobject thiz,jobject javaSurface,jbyteArray y,jbyteArray u,jbyteArray v,
 jint yPixelStride,jint uPixelStride,jint vPixelStride,
 jint yRowSrtide, jint uRowStride, jint vRowStride, jint srcWidth,jint srcHeight) {

	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	if(window!=NULL)
	{
		unsigned char *pSrcYPlane = (unsigned char*)(*env)->GetDirectBufferAddress(env,y);
		unsigned char *pSrcUPlane = (unsigned char*)(*env)->GetDirectBufferAddress(env,u);
		unsigned char *pSrcVPlane = (unsigned char*)(*env)->GetDirectBufferAddress(env,v);


		if(uPixelStride==1) {
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_YV12);
		} else {
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeight, ImageFormat_NV21);
		}

		ANativeWindow_Buffer dstBuffer;
		if (ANativeWindow_lock(window, &dstBuffer, NULL) == 0) {

			unsigned char* pDstBuffer = dstBuffer.bits;
			int yPlaneSize = srcWidth*srcHeight;
			int i,j;

			if((dstBuffer.width==dstBuffer.stride  && srcWidth == yRowSrtide && uPixelStride == 2) && !GO_GENERAL_PATH) {
				// faster
				// Y
				memcpy(pDstBuffer, pSrcYPlane, yPlaneSize);

				// UV
				int uvPlaneSize = yPlaneSize/2;
				int uSize = uvPlaneSize/2;

				// copy V
				memcpy(pDstBuffer + yPlaneSize, pSrcUPlane + 1, uvPlaneSize - 1);
				// copy U
				unsigned char *src = pSrcUPlane;
				unsigned char *dst = pDstBuffer + yPlaneSize + 1;
				int i = 0;
				while (i < uSize) {
					(*dst) = (*src);
					dst += 2;
					src += 2;
					++i;
				}

			} else {
				// Y
				for(i=0;i<srcHeight;i++) {
					memcpy(pDstBuffer+dstBuffer.stride*i, pSrcYPlane+yRowSrtide*i, srcWidth);
				}

				if(dstBuffer.format==ImageFormat_NV21) {
					// UV
					for (i = 0; i < srcHeight / 2; i++) {
						for (j = 0; j < srcWidth / 2; j++) {
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j * 2 + 0) = *(
									pSrcVPlane + vRowStride * i + j * vPixelStride);
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j * 2 + 1) = *(
									pSrcUPlane + uRowStride * i + j * uPixelStride);
						}
					}
				} else if(dstBuffer.format==ImageFormat_YV12) {

					for (i = 0; i < srcHeight/4; i++) {
#if FAST_COPY_YV21
                        // because pixel stride = 1, using memcpy instead of pointwise copy
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride * i, pSrcVPlane + uRowStride * (i*2), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride * i + srcWidth/2, pSrcVPlane + uRowStride * (i*2+1), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeight/4+ dstBuffer.stride * i, pSrcUPlane + vRowStride * (i*2), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeight/4+ dstBuffer.stride * i + srcWidth/2, pSrcUPlane + vRowStride * (i*2+1), srcWidth/2);
#else

						for (j = 0; j < srcWidth/2; j++) {

							// U
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j) =
									*(pSrcVPlane + uRowStride * (i*2) + j * uPixelStride);

							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + srcWidth/2 + j) =
									*(pSrcVPlane + uRowStride * (i*2+1) + j * uPixelStride);

							// V
							*(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeight/4+ dstBuffer.stride * i + j) =
									*(pSrcUPlane + vRowStride * (i*2) + j * vPixelStride);

							*(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeight/4+ dstBuffer.stride * i + srcWidth/2 + j) =
									*(pSrcUPlane + vRowStride * (i*2+1) + j * vPixelStride);

						}
#endif
					}
				}
			}
			ANativeWindow_unlockAndPost(window);
		}
		ANativeWindow_release(window);
	}
	return;
}

//rendering
JNIEXPORT void JNICALL Java_com_viatech_utility_tool_NativeRender_renderingYUVTopOrBottonHalf
(JNIEnv * env, jobject thiz,jobject javaSurface,jbyteArray y,jbyteArray u,jbyteArray v,
 jint yPixelStride,jint uPixelStride,jint vPixelStride,
 jint yRowSrtide, jint uRowStride, jint vRowStride, jint type , jint srcWidth, jint srcHeight) {

	ANativeWindow* window = ANativeWindow_fromSurface(env, javaSurface);
	int srcHeightHalf = srcHeight/2;


	unsigned char *y_pixels = (unsigned char*)(*env)->GetDirectBufferAddress(env,y);
	unsigned char *u_pixels = (unsigned char*)(*env)->GetDirectBufferAddress(env,u);
	unsigned char *v_pixels = (unsigned char*)(*env)->GetDirectBufferAddress(env,v);

	// assume y_stride == 1
	unsigned char *pSrcYPlane = y_pixels + yRowSrtide*srcHeightHalf*type; // type==0 top type==1 botton
	unsigned char *pSrcUPlane = u_pixels + uRowStride*srcHeightHalf/2*type;;
	unsigned char *pSrcVPlane = v_pixels + vRowStride*srcHeightHalf/2*type;

	if(window!=NULL)
	{
		if(uPixelStride==1) {
			// YUV420planer case
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeightHalf, ImageFormat_YV12);
		} else {
			ANativeWindow_setBuffersGeometry(window, srcWidth, srcHeightHalf, ImageFormat_NV21);
		}

		ANativeWindow_Buffer dstBuffer;
		if (ANativeWindow_lock(window, &dstBuffer, NULL) == 0) {


			unsigned char* pDstBuffer = dstBuffer.bits;
			int yPlaneSize = srcWidth*srcHeightHalf;
			int i,j;
			if((dstBuffer.width == dstBuffer.stride && srcWidth == yRowSrtide && uPixelStride == 2) && !GO_GENERAL_PATH) {
				//faster
				// Y
				memcpy(pDstBuffer, pSrcYPlane, yPlaneSize);

				// UV
				int uvPlaneSize = yPlaneSize/2;
				int uSize = uvPlaneSize/2;
				// V
				memcpy(pDstBuffer + yPlaneSize, pSrcUPlane + 1, uvPlaneSize - 1);
				// U
				unsigned char *src = pSrcUPlane;
				unsigned char *dst = pDstBuffer + yPlaneSize + 1;
				int i = 0;
				while (i < uSize) {
					(*dst) = (*src);
					dst += 2;
					src += 2;
					++i;
				}
			} else {
				// Y
				for (i = 0; i < srcHeightHalf; i++) {
					memcpy(pDstBuffer + dstBuffer.stride * i, pSrcYPlane + yRowSrtide * i,
						   srcWidth);
				}

				if (dstBuffer.format == ImageFormat_NV21) {
					// UV
					for (i = 0; i < srcHeightHalf / 2; i++) {
						for (j = 0; j < srcWidth / 2; j++) {
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j * 2 + 0) = *(
									pSrcVPlane + vRowStride * i + j * vPixelStride);
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j * 2 + 1) = *(
									pSrcUPlane + uRowStride * i + j * uPixelStride);
						}
					}
				} else if (dstBuffer.format == ImageFormat_YV12) {

					for (i = 0; i < srcHeightHalf/4; i++) {
#if FAST_COPY_YV21
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride * i, pSrcVPlane + uRowStride * (i*2), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride * i + srcWidth/2, pSrcVPlane + uRowStride * (i*2+1), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeightHalf/4+ dstBuffer.stride * i, pSrcUPlane + vRowStride * (i*2), srcWidth/2);
						memcpy(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeightHalf/4+ dstBuffer.stride * i + srcWidth/2, pSrcUPlane + vRowStride * (i*2+1), srcWidth/2);
#else
						for (j = 0; j < srcWidth/2; j++) {
							// U
							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + j) =
									*(pSrcVPlane + uRowStride * (i*2) + j * uPixelStride);

							*(pDstBuffer + yPlaneSize + dstBuffer.stride * i + srcWidth/2 + j) =
									*(pSrcVPlane + uRowStride * (i*2+1) + j * uPixelStride);
							// V
							*(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeightHalf/4+ dstBuffer.stride * i + j) =
									*(pSrcUPlane + vRowStride * (i*2) + j * vPixelStride);

							*(pDstBuffer + yPlaneSize + dstBuffer.stride*srcHeightHalf/4+ dstBuffer.stride * i + srcWidth/2 + j) =
									*(pSrcUPlane + vRowStride * (i*2+1) + j * vPixelStride);

						}
#endif
					}
				}
			}

			ANativeWindow_unlockAndPost(window);
		}
		ANativeWindow_release(window);
	}
	return;
}
