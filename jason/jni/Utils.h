/**
 * @file	Utils.h
 *
 * @brief	A utility class for application operations.
 *
 * @note    Copyright (c) 2010-2011 QUALCOMM Incorporated.
 *          All Rights Reserved.
 *          Qualcomm Confidential and Proprietary
 **/

#ifndef _QCAR_Utils_H_
#define _QCAR_Utils_H_

// Includes:
#include <stdio.h>
#include <android/log.h>

// Utility for logging:
#define LOG_TAG    "IMAGE_TARGETS"
#define LOG(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

/// A utility class used by the QCAR SDK samples.
class Utils
{
public:

    /// Prints a 4x4 matrix.
    static void printMatrix(const float* matrix);

    /// Prints GL error information.
    static void checkGlError(const char* operation);
    
    /// Set the rotation components of this 4x4 matrix.
    static void setRotationMatrix(float angle, float x, float y, float z, 
        float *nMatrix);
    
    /// Set the translation components of this 4x4 matrix.
    static void translatePoseMatrix(float x, float y, float z,
        float* nMatrix = NULL);
    
    /// Applies a rotation.
    static void rotatePoseMatrix(float angle, float x, float y, float z, 
        float* nMatrix = NULL);
    
    /// Applies a scaling transformation.
    static void scalePoseMatrix(float x, float y, float z, 
        float* nMatrix = NULL);
    
    /// Multiplies the two matrices A and B and writes the result to C.
    static void multiplyMatrix(float *matrixA, float *matrixB, 
        float *matrixC);
    
    /// Initialize a shader.
    static unsigned int initShader(unsigned int shaderType, 
        const char* source);
    
    /// Create a shader program.
    static unsigned int createProgramFromBuffer(const char* vertexShaderBuffer,
        const char* fragmentShaderBuffer);
};

#endif // _QCAR_Utils_H_
