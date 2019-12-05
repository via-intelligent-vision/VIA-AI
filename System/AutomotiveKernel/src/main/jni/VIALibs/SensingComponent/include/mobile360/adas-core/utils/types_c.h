#ifndef ADAS_CORE_TPYES_C_H
#define ADAS_CORE_TPYES_C_H

struct ADASLine
{
    float x0;
    float y0;
    float x1;
    float y1;
    float r;
    float g;
    float b;
    float lineWidth;
};

struct ADASPoint
{
    float x;
    float y;
    float r;
    float g;
    float b;
};

#endif

