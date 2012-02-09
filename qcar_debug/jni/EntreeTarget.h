/**
 * @file	EntreeTarget.h
 *
 * @brief	Provides class definition of entree targets.
 */

using namespace std;

typedef char* string;

class EntreeTarget {
public:
	int itemId;
	string itemName;
	bool itemSelected;

    /// Constructor
    EntreeTarget(string name, int id);

    /// Destructor.
    ~EntreeTarget();
};
