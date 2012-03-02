/**
 * @file	EntreeTarget.cpp
 *
 * @author	Aaron Alaniz (webheadz3@gmail.com)
 *
 * @brief	Provides class that stores information relevant
 * 			to a given entree.
 */

#include <string.h>
#include "EntreeTarget.h"

// Constructor
EntreeTarget::EntreeTarget(string name, int id)
{
	this->itemName = new char[strlen(name)];
	this->itemId = id;
	strcpy(this->itemName, name);
	this->itemSelected = false;
	this->itemActive = false;
}
// Destructor
EntreeTarget::~EntreeTarget()
{}

// Set Pose
void EntreeTarget::setPose(QCAR::Matrix44F pose)
{
	this->curPose = pose;
	this->itemActive = true;
}

// Check to see if item active
bool EntreeTarget::isActive()
{
	return this->itemActive;
}
