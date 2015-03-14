#Android programming features used in this software.

# Introduction #
This page is for new Android developers


# Details #

There are examples of many Android SDK features in this source code.  Here are a few highlights:

  * `SQLite` database creating, reading, and writing
  * Multi-thread task handling
  * `ListView`
  * Basic Buttons, `TextViews`, and `CheckBoxes`
  * `SeekBar`
  * Use of Android Intents to trigger function calls and for extracting data from Intent
  * Use of many Bluetooth features
  * Use of Android Location services, starting/stopping a location listener, and using the location to spawn a URL that can be used by several services including Google Maps.
  * Implementation of starting a service after device boot based on user configured preference.
  * Use of Android Preferences features
  * Time elements
  * File reading and writing
  * Widget with button to trigger a service action
  * starting a start\_sticky service
  * multiple language support
  * Use of `MyApplication` class
  * Use of `AIDL` interface
  * Force connection of a Bluetooth receiver
  * Use of Text-To-Speech (TTS) services
  * Reading text (SMS) messages
  * Launch another application
  * Attempt to stop another application
  * Creating intents to launch home screen shortcuts, create a shortcut, list installed packages, Pandora integration, etc.
  * `AudioManager` usage for adjusting stream volume and taking/abandoning focus

Some of this source is based on other examples found on the web or in the Android Developers website.  Feel free to use any parts of this code as you wish.

Those wanting to make basic apps without learning Java and detailed programming can do so here: http://appinventor.mit.edu/
App Inventor lets non-programmers create apps using puzzle pieces and a website.

# Ready to get started? #
There are a few things to install to develop in Android and download this project.

  1. Download the Android development environment and SDK.  Go here: http://developer.android.com/sdk/index.html and follow the directions.

> 2) Download and install Mercurial.  Get it here:
http://mercurial.selenic.com/
There is also a nice tutorial here:
http://hginit.com/

> 3) Optional: Install Tortoise to simplify the user interface for Mercurial. http://mercurial.selenic.com/wiki/TortoiseHg

> 4) Clone this repository with Mercurial.  Instruction are here:
http://code.google.com/p/a2dpvolume/source/checkout

> 5) Load this project into the SDK.  Open up the Eclipse environment (installed with SDK) and load this project into your workspace. Find where you cloned the project to.  This will be a folder on your PC.  Using Eclipse open the "Package Explorer" window.  You can find it under `[Window] ->  [Show View] -> [Package Explorer]`.  Right click in the package explorer window and select `[Import]`.  Expand the `[Android]` folder and click on `[Existing Android Code Into Workspace]`.  Now you should see then project files.