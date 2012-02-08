#include <string.h>
#include "EntreeTarget.h"

EntreeTarget::EntreeTarget(string name, int id)
{
	this->itemName = new char[strlen(name)];
	this->itemId = id;
	strcpy(this->itemName, name);
	this->itemSelected = false;
}


EntreeTarget::~EntreeTarget()
{}
