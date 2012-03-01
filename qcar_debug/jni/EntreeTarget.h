/**
 * @file	EntreeTarget.h
 *
 * @brief	Provides class definition of entree targets.
 */

#include <QCAR/Tool.h>

typedef char* string;

class EntreeTarget {
public:
	int itemId;
	string itemName;
	bool itemSelected;
	bool itemActive;
	QCAR::Matrix44F curPose;

    // Constructor
    EntreeTarget(string name, int id);

    // Destructor.
    ~EntreeTarget();

    // Set current pose matrix
    void setPose(QCAR::Matrix44F pose);

    // Check to see if active
    bool isActive();
};
