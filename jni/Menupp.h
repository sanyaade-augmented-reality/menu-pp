/**
 * @file	Menupp.h
 *
 * @brief	The purpose of this file is to provide the general
 * 			data useful to Menu++. The inline code is used for
 * 			openGL ES related tasks.
 */

#define MAX_TRACKABLES 3

enum BUTTONS
{
    BUTTON_1                    = 1,
    BUTTON_2                    = 2,
    BUTTON_3                    = 4,
};

enum ActionType {
    ACTION_DOWN,
    ACTION_MOVE,
    ACTION_UP,
    ACTION_CANCEL
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
