# Step 1 - Download these things according to your OS #

Eclipse Classic 3.7.1
http://www.eclipse.org/downloads/

Qualcomm AR SDK
https://developer.qualcomm.com/develop/mobile-technologies/augmented-reality/tools-and-resources

**remember where you install this**

Android NDK
http://developer.android.com/sdk/ndk/index.html

**remember where you extract this**


# Step 2 - Android ADT Plugin #

Run Eclipse and go to Help->Install New Software..

**if it complains about not having Java, download JDK from here http://www.oracle.com/technetwork/java/javase/downloads/index.html**

Enter this URL to install the Android ADT Plugin:
https://dl-ssl.google.com/android/eclipse/

check everything, accept licenses, install, restart eclipse

# Step 3 - Install Android SDK #
Upon restarting, you should receive the option to install the Android SDK. Be sure to install Android 2.1

# Step 4 - Setup Subclipse #
Run Eclipse and go to Help->Install New Software.. again

Enter this URL to install Subclipse:
http://subclipse.tigris.org/update_1.4.x

check everything, accept licenses, install, restart eclipse

# Step 5 - Create New Project in Eclipse #

File->New->Project..
go to SVN and Checkout Projects from SVN
Create New Repository
enter this URL: https://menu-pp.googlecode.com/svn/trunk/
Next Next Finish

# Step 6 - QCAR Library setup #

Right click your project and go to Build Path->Configure Build Path.. Edit QCAR.jar in the Libraries tab and change the path to
[your put the Qualcomm SDK](wherever.md)\Android\qcar-android-1-0-6\build\java\QCAR\QCAR.jar

# Step 7 - NDK builder setup #

Right click your project and go to Build Path->Configure Build Path..
Go to the Builders page on the left side of the window and edit NDK Builder

change the path to [you put the Android NDK](wherever.md)\android-ndk-[r7](https://code.google.com/p/menu-pp/source/detail?r=7)\ndk-build


# Upon the implementation of Gestures for the user guide menu, you may have to install a support package jar. #


1) open your Android SDK Manager. Should be under Window in Eclipse.


2) Go to Extras and check Android Support Package.


3) Right click your project and go to Android Tools -> Add Compatibility Library..


4) If you receive an error message it should name the folder that you need to copy your android-support-v4.jar into.


5) After moving the .jar, run Add Compatibility Library again. After a successful run, check your Build Paths for the android-support-v4.jar If it's there you are done