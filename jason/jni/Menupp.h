/**
 * @file	menupp.h
 *
 * @brief	The purpose of this file is to provide the general
 * 			data useful to Menu++. The inline code is used for
 * 			openGL ES related tasks.
 */

enum BUTTONS
{
    BUTTON_1                    = 1,
    BUTTON_2                    = 2,
    BUTTON_3                    = 4,
    BUTTON_4                    = 8,
    BUTTON_5					= 16
};

static const char* lineMeshVertexShader = " \
  \
attribute vec4 vertexPosition; \
 \
uniform mat4 modelViewProjectionMatrix; \
 \
void main() \
{ \
   gl_Position = modelViewProjectionMatrix * vertexPosition; \
} \
";


static const char* lineFragmentShader = " \
 \
precision mediump float; \
 \
void main() \
{ \
   gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); \
} \
";
