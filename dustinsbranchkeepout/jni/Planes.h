/**
 * @file	Planes.h
 *
 * @brief	The purpose of this file is to provide the geometric
 * 			plane definitions that will be utilized in the application
 * 			for rendering. The rectangular plane is used to bind the
 * 			descriptions of the entrees and the square plane is used
 * 			to bind the entree images to.
 */

static const float planeVertices[] =
{//	  x	    y	 z
    -0.5, -0.5, 0.0,
     0.5, -0.5, 0.0,
     0.5,  0.5, 0.0,
    -0.5,  0.5, 0.0,
};

static const float rectPlaneVertices[] =
{//	  x 	y	 z
    -1.5, -0.5, 0.0,
     1.5, -0.5, 0.0,
     1.5,  0.5, 0.0,
    -1.5,  0.5, 0.0,
};

static const float planeTexCoords[] =
{//	 x	  y
    0.0, 0.0,
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0
};

static const float rectTexCoords[] =
{//  x    y
    0.0, 0.0,
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0
};

static const float planeNormals[] =
{//	 x    y    z
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0
};

static const float rectNormals[] =
{//	 x    y    z
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0
};

static const unsigned short planeIndices[] =
{//	x  y
    0, 1,
    2, 0,
    2, 3
};

static const unsigned short rectIndices[] =
{// x  y
    0, 1,
    2, 0,
    2, 3
};
