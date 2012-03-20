/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

@file
    SampleMath.h

@brief
    A utility class.

==============================================================================*/


#ifndef _QCAR_SAMPLEMATH_H_
#define _QCAR_SAMPLEMATH_H_

// Includes:
#include <QCAR/Tool.h>

QCAR::Vec2F Vec2FSub(QCAR::Vec2F v1, QCAR::Vec2F v2);

float Vec2FDist(QCAR::Vec2F v1, QCAR::Vec2F v2);

QCAR::Vec3F Vec3FAdd(QCAR::Vec3F v1, QCAR::Vec3F v2);

QCAR::Vec3F Vec3FSub(QCAR::Vec3F v1, QCAR::Vec3F v2);

QCAR::Vec3F Vec3FScale(QCAR::Vec3F v, float s);

QCAR::Vec4F Vec3FDiv(QCAR::Vec4F v1, float s);

float Vec3FDot(QCAR::Vec3F v1, QCAR::Vec3F v2);

QCAR::Vec3F Vec3FCross(QCAR::Vec3F v1, QCAR::Vec3F v2);

QCAR::Vec3F Vec3FNormalize(QCAR::Vec3F v);

QCAR::Vec3F Vec3FTransform(QCAR::Vec3F& v, QCAR::Matrix44F& m);

QCAR::Vec3F Vec3FTransformNormal(QCAR::Vec3F& v, QCAR::Matrix44F& m);

QCAR::Vec4F Vec4FTransform(QCAR::Vec4F& v, QCAR::Matrix44F& m);

QCAR::Matrix44F Matrix44FIdentity();

QCAR::Matrix44F Matrix44FTranspose(QCAR::Matrix44F m);

float Matrix44FDeterminate(QCAR::Matrix44F& m);

QCAR::Matrix44F Matrix44FInverse(QCAR::Matrix44F& m);

#endif
