/**
 * @file 	Texture.cpp
 *
 * @brief	Implementation of class Texture
 *
 * @note    Copyright (c) 2010-2011 QUALCOMM Incorporated.
 *          All Rights Reserved.
 *          Qualcomm Confidential and Proprietary
 **/

// Include files
#include "Texture.h"
#include "Utils.h"
#include <string.h>

Texture::Texture() :
mWidth(0),
mHeight(0),
mChannelCount(0),
mData(0),
mTextureID(0)
{}


Texture::~Texture()
{
    if (mData != 0)
        delete[]mData;
}

string Texture::getName()
{
	return this->mName;
}

unsigned int Texture::getId()
{
	return this->mTextureID;
}

Texture*
Texture::create(JNIEnv* env, jobject textureObject)
{

    Texture* newTexture = new Texture();

    // Handle to the Texture class:
    jclass textureClass = env->GetObjectClass(textureObject);

    // Get width:
    jfieldID widthID = env->GetFieldID(textureClass, "mWidth", "I");
    if (!widthID)
    {
        LOG("Field mWidth not found.");
        return 0;
    }
    newTexture->mWidth = env->GetIntField(textureObject, widthID);

    // Get height:
    jfieldID heightID = env->GetFieldID(textureClass, "mHeight", "I");
    if (!heightID)
    {
        LOG("Field mHeight not found.");
        return 0;
    }
    newTexture->mHeight = env->GetIntField(textureObject, heightID);

    // Get name:
    jfieldID nameID = env->GetFieldID(textureClass, "mName", "Ljava/lang/String;");
    if (!nameID)
    {
        LOG("Field mName not found.");
        return 0;
    }
    jstring name = (jstring) env->GetObjectField(textureObject, nameID);
    newTexture->mName = (string) env->GetStringUTFChars(name, 0);

    // Always use RGBA channels:
    newTexture->mChannelCount = 4;

    // Get data:
    jmethodID texBufferMethodId = env->GetMethodID(textureClass , "getData", "()[B");
    if (!texBufferMethodId)
    {
        LOG("Function GetTextureBuffer() not found.");
        return 0;
    }
    
    jbyteArray pixelBuffer = (jbyteArray)env->CallObjectMethod(textureObject, texBufferMethodId);    
    if (pixelBuffer == NULL)
    {
        LOG("Get image buffer returned zero pointer");
        return 0;
    }

    jboolean isCopy;
    jbyte* pixels = env->GetByteArrayElements(pixelBuffer, &isCopy);
    if (pixels == NULL)
    {
        LOG("Failed to get texture buffer.");
        return 0;
    }

    newTexture->mData = new unsigned char[newTexture->mWidth * newTexture->mHeight * newTexture->mChannelCount];

    unsigned char* textureData = (unsigned char*) pixels;

    int rowSize = newTexture->mWidth * newTexture->mChannelCount;
    for (int r = 0; r < newTexture->mHeight; ++r)
    {
        memcpy(newTexture->mData + rowSize * r, pixels + rowSize * (newTexture->mHeight - 1 - r), newTexture->mWidth * 4);
    }

    // Release:
    env->ReleaseByteArrayElements(pixelBuffer, pixels, 0);
    
    return newTexture;
}

