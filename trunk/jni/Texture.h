/**
 * @file 	Texture.h
 *
 * @brief	A utility class for textures.
 *
 * @note    Copyright (c) 2010-2011 QUALCOMM Incorporated.
 *          All Rights Reserved.
 *          Qualcomm Confidential and Proprietary
 **/

#ifndef _QCAR_TEXTURE_H_
#define _QCAR_TEXTURE_H_

// Include files
#include <jni.h>

typedef char* string;

// A utility class for textures.
class Texture
{
public:

    // Constructor
    Texture();

    // Destructor.
    ~Texture();

    // Returns the width of the texture.
    unsigned int getWidth() const;

    // Returns the height of the texture.
    unsigned int getHeight() const;

    // Returns the name of the texture.
    string getName();

    // Returns the id of the texture.
    unsigned int getId();

    // Create a texture from a jni object:
    static Texture* create(JNIEnv* env, jobject textureObject);
 
    // The width of the texture.
    unsigned int mWidth;

    // The height of the texture.
    unsigned int mHeight;

    // The name of the texture
    string mName;

    // The number of channels of the texture.
    unsigned int mChannelCount;

    // The pointer to the raw texture data.
    unsigned char* mData;

    // The ID of the texture
    unsigned int mTextureID;
};


#endif //_QCAR_TEXTURE_H_
