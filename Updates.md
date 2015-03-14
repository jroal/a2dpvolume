#Major updates after V1.2.3.

For more details about the changes, see the change list http://code.google.com/p/a2dpvolume/source/list

# 2.11.12 December 14 2014 #
  * Fixed [issue 193](https://code.google.com/p/a2dpvolume/issues/detail?id=193)

# 2.11.11 May 21 2014 #
  * Fixed null pointer exception from app chooser when people would unselect everything.  Did not see this as a valid use case since you have the "Enable reading notification messages" preference as well as the enabling in accessibility to make it work.  No reason to ever unselect everything but apparently some people still did.
  * Fixed bug that allowed TTS to read messages even if device was configured not to.

# 2.11.10 March 18 2014 #
  * Fixed [issue 184](https://code.google.com/p/a2dpvolume/issues/detail?id=184).
  * Found another place in the code that could cause [issue 183](https://code.google.com/p/a2dpvolume/issues/detail?id=183) and fixed it.
  * Added translated strings for [issue 182](https://code.google.com/p/a2dpvolume/issues/detail?id=182).

# 2.11.9 March 16 2014 #
  * Fixed [issue 183](https://code.google.com/p/a2dpvolume/issues/detail?id=183).

# 2.11.8 January 12 2014 (Amazon) #
  * Fix to support Amazon devices.
  * Fixed [issue 182](https://code.google.com/p/a2dpvolume/issues/detail?id=182) (published 2/23 oops)
  * Note:  I made a mistake and there are 2 version 2.11.8 apk's out there.  One only made it to the Amazon store.  The other has all the updates in it and was published as beta to the Play Store.  Messy I know but low impact in the end.

# 2.11.7 January 5 2013 (Play Store) #
  * Completed translations.

# 2.11.6 January 4 2014 #
  * Fixed bug causing crash when accessibility was active, device connected, and a non-notification message was delivered.
  * Added a URL check to the text reader.  It now replaces all URL strings with just the letters URL when it reads it out.

# 2.11.5 December 27 2013 #
  * Fixed notification icon.  Wrong resource cause over sized icon.
  * Improved accessibility.  Now reads Jelly Bean enhanced notifications to get all Hangouts messages, as well as others.
  * Added generic app name retriever instead of just a few.  Now it will read out any app that you have set up.
  * Completed German and French translations

# 2.11.4 December 18 2013 #
  * Changed notification priority to minimum.  This feature was added in API16 and allows this app to not have the notification in the notification bar but it will show when you pull down notifications.  Nice.
  * Fixed a bug that use speaker phone more for voice call stream.

# 2.11.3 December 15 2013 #
  * Closed [issue 175](https://code.google.com/p/a2dpvolume/issues/detail?id=175).  Added a configuration screen to set up packages to respond to messages from.  This way the user can decide what apps they want to have notification messages read from while the A2DP Volume accessibility service is active.

# 2.11.2 December 14 2013 #
  * Changed minimum SDK to 15 (Android 4.03).  Older software will no longer be supported.
  * Removed gps enable feature that only worked up to Gingerbread.
  * Fixed several issues relating to deprecated features in Android.
  * Fixed [issue 176](https://code.google.com/p/a2dpvolume/issues/detail?id=176)
  * Fixed [issue 166](https://code.google.com/p/a2dpvolume/issues/detail?id=166)
  * Fixed [issue 162](https://code.google.com/p/a2dpvolume/issues/detail?id=162)
  * Fixed [issue 152](https://code.google.com/p/a2dpvolume/issues/detail?id=152)
  * Fixed [issue 145](https://code.google.com/p/a2dpvolume/issues/detail?id=145)
  * Fixed [issue 107](https://code.google.com/p/a2dpvolume/issues/detail?id=107)

# 2.11.0 and 2.11.1 December 9 2013 #
  * Working on [issue 145](https://code.google.com/p/a2dpvolume/issues/detail?id=145).  Should be fixed now.
  * Fixed [issue 178](https://code.google.com/p/a2dpvolume/issues/detail?id=178)
  * Re-purposed the GTalk preference for the new accessibility service since if is intended to read Hangouts, Gmail, etc.  The old GTalk preference has a new purpose and name now.  If you had enable GTalk checked, it is now enable notification reading.
  * Completed [issue 144](https://code.google.com/p/a2dpvolume/issues/detail?id=144).

# 2.10.1 / 2.10.2 September 27 2013 #
  * Bug fix for missing permissions
  * Other bug fixes related to accessibility service.

# 2.10.0 September 17 2013 #
  * Added accessibility service to read out notifications from many apps while connected.  This must be enabled in the Android accessibility settings.

# 2.9.8 September 7 2013 (Play Store) #
  * Fixed [issue 163](https://code.google.com/p/a2dpvolume/issues/detail?id=163)
  * Added missing German and French translations

# 2.9.7 April 28 2013 (Play Store) #
  * Fix incompatibility with Android versions less than 4.
  * Still missing some translations

# 2.9.6 April 18 2013 (Play Store 4/27/2013) #
  * Fixed [issue 160](https://code.google.com/p/a2dpvolume/issues/detail?id=160).
  * Fixed [issue 117](https://code.google.com/p/a2dpvolume/issues/detail?id=117).
  * NOTE:  This version only supports Android 4.0 and up.  By mistake I let it out to the Play Store without that restriction.  If you were upgraded and you have an older Android version you will need to downgrade to 2.9.5 or earlier.  Sorry.

# 2.9.5 April 14 2013 #
  * Fixed [issue 149](https://code.google.com/p/a2dpvolume/issues/detail?id=149).

# 2.9.4 January 23 2013 #
  * Added string limiter to text message reading.  This was done for [issue 151](https://code.google.com/p/a2dpvolume/issues/detail?id=151).  Now messages will only read out the first 350 characters.

# 2.9.3 January 1 2013 (Play Store 1/17/2013) #
  * Fixed [issue 145](https://code.google.com/p/a2dpvolume/issues/detail?id=145).

# 2.9.2 December 28 2012 #
  * Bug fixes for 2.9.1 where the Talk message receiver would stay active after disconnect.
  * Other minor fixes in an attempt to correct [issue 117](https://code.google.com/p/a2dpvolume/issues/detail?id=117).

# 2.9.1 December 26 2012 #
  * Fixed the Google Talk reader so it uses proper user name.

# 2.9.0 December 26 2012 #
  * Implemented Google Talk message reader.  Work in progress but does read message.
  * Implemented a message receiver to handle incoming messages from IM services.  This was part of [issue 76](https://code.google.com/p/a2dpvolume/issues/detail?id=76) implementation. Tested with a test app and verified functionality.  While this intent receiver was intended for use by A2DP Volume Chat other apps can also use it.

# 2.8.17 December 18 2012 (market) #
  * Fixed [issue 140](https://code.google.com/p/a2dpvolume/issues/detail?id=140)

# 2.8.16 December 16 2012 (market) #
  * Replaced ugly icons with better ones

# 2.8.15 December 8 2012 #
  * Used Android Lint tool to optimize code.
  * Removed unused strings
  * Removed unnecessary graphics

# 2.8.14 November 21 2012 (market) #
  * Fixed [issue 142](https://code.google.com/p/a2dpvolume/issues/detail?id=142)

# 2.8.12 August 29 2012 #
  * Added Danish translations per [issue 136](https://code.google.com/p/a2dpvolume/issues/detail?id=136)

# 2.8.11 August 3 2012 (market) #
  * Fixed [issue 124](https://code.google.com/p/a2dpvolume/issues/detail?id=124)

# 2.8.10 August 3 2012 #
  * Fixed [issue 130](https://code.google.com/p/a2dpvolume/issues/detail?id=130)

# 2.8.9 July 27 2012 (market) #
  * Fix for [issue 131](https://code.google.com/p/a2dpvolume/issues/detail?id=131)

# 2.8.7 and 2.8.8 July 21 2012 (market) #
  * Fixed SQlite commends to make app compatible with Jelly Bean fixing [issue 127](https://code.google.com/p/a2dpvolume/issues/detail?id=127) and the others that were duplicates.

# 2.8.6 May 3 2012 (market) #
  * Fixed [issue 111](https://code.google.com/p/a2dpvolume/issues/detail?id=111)
  * Improved edit device GUI by hiding settings that are not active due to unchecked options.
  * Fixed [issue 110](https://code.google.com/p/a2dpvolume/issues/detail?id=110) - Removed GPS enable setting for Android 4.0 and up.  Android removed the ability to do this on purpose.

# 2.8.5 April 30 2012 (market) #
  * finished translations for below fixes and published to market.

# 2.8.4 beta April 29 2012 #
  * Implemented auto Bluetooth enable feature per [issue 99](https://code.google.com/p/a2dpvolume/issues/detail?id=99).

# 2.8.3 beta April 29 2012 #
  * Fixed [issue 98](https://code.google.com/p/a2dpvolume/issues/detail?id=98).  Note: this will only retrieve the remote alias name from ICS and up when finding a new device.

# 2.8.2 beta April 27 2012 #
  * Finally fixed [issue 105](https://code.google.com/p/a2dpvolume/issues/detail?id=105), which has several other issues now rolled into it.  I found a way to make Pandora pause on disconnect!  Also tested with Spotify and that works now.

# 2.8.1 beta April 24 2012 #
  * Fixed pause command key send for [issue 104](https://code.google.com/p/a2dpvolume/issues/detail?id=104) and [issue 105](https://code.google.com/p/a2dpvolume/issues/detail?id=105).  This required adding 2 more permissions.
  * Fixed auto volume capture feature bug.
  * Changed app so it can only be installed to internal memory.  Installing to SD card was causing too many complaints of auto-start feature not working.  Apps installed to the SD card cannot auto-start.

# 2.8.0 beta April 23 2012 #
  * Added more icons for [issue 32](https://code.google.com/p/a2dpvolume/issues/detail?id=32).  Now they are device specific rather than global.
  * Fixed [issue 100](https://code.google.com/p/a2dpvolume/issues/detail?id=100)
  * Fixed [issue 101](https://code.google.com/p/a2dpvolume/issues/detail?id=101)
  * Fixed [issue 102](https://code.google.com/p/a2dpvolume/issues/detail?id=102)
  * Fixed [issue 103](https://code.google.com/p/a2dpvolume/issues/detail?id=103)
  * Added a cancel button to the edit device screen. Back button now saves rather than aborting.  Changed focus to prevent virtual keyboard from congesting screen.

# 2.7.2 April 6 2012 (market) #
  * I had to remove the attempted fix in 2.7.1 after it actually caused more errors than it fixed.

# 2.7.1 April 6 2012 (market) #
  * I attempted to fix a database open error that had 1 reported occurrence from Google crash reports.

# 2.7.0 April 5 2012 (market) #
  * Completed issues 95 and 97 with translations.

# 2.6.0 April 4 2012 #
  * Implemented power connection device per [issue 97](https://code.google.com/p/a2dpvolume/issues/detail?id=97).  Now when the device is connected to power, it can trigger actions similar to home dock, car dock, and headset plug.
  * Implemented GPS enable/disable feature per [issue 95](https://code.google.com/p/a2dpvolume/issues/detail?id=95).  When a device connects you can now enable GPS while the device is connected.  GPS will return to the previous state after disconnect.  However, if location capture is enabled, GPS will stay enabled until 2s after the maximum configured capture time to allow a location to be captured properly.

# 2.5.4 March 25 2012 (market) #
  * Fixed [issue 91](https://code.google.com/p/a2dpvolume/issues/detail?id=91).  Added device specific configuration to stop the launched app when the device disconnects.  The functionality of stopping the launched app has been in the app for a long time but now you can select to have it stop the app or not.  It is defaulted to true (stop the app).

# 2.5.3 March 23 2012 (market) #
  * Fixed [issue 94](https://code.google.com/p/a2dpvolume/issues/detail?id=94)

# 2.5.2 March 2 2012 (market) #
  * Removed filter that eliminated the current device from the list of Bluetooth devices to force connect to.  This allows a device that has both hands free and A2DP protocols to force connect its own A2DP when the hands free connects.

# 2.5.1 February 15 2012 (market) #
  * Added missing translations.

# 2.5.0 February 12 2012 #
  * Added volume up ramp rate per [issue 89](https://code.google.com/p/a2dpvolume/issues/detail?id=89). Also added new preference to enable it.
  * Added volume up delay configuration per [issue 89](https://code.google.com/p/a2dpvolume/issues/detail?id=89).

# 2.4.2 February 11 2012 #
  * Added more improved support to stop music players and kill apps that were started to correct [issue 48](https://code.google.com/p/a2dpvolume/issues/detail?id=48) and [issue 88](https://code.google.com/p/a2dpvolume/issues/detail?id=88).
  * Added check to Bluetooth connect to prevent connect then disconnect on some pre-API 11 devices.  Found issue on Bionic with Jaguar factory Bluetooth hands free using auto connect to GoGroove BT for A2DP only.

# 2.4.1 February 10 2012 #
  * Added more improved support to stop music players and kill apps that were started to correct [issue 48](https://code.google.com/p/a2dpvolume/issues/detail?id=48) and [issue 88](https://code.google.com/p/a2dpvolume/issues/detail?id=88).

# 2.4.0 February 24 2012 #
  * Added improved support to stop music players and kill apps that were started to correct [issue 48](https://code.google.com/p/a2dpvolume/issues/detail?id=48) and [issue 88](https://code.google.com/p/a2dpvolume/issues/detail?id=88).

# 2.3.17 xxxx #
  * Added cleanup activities to ensure the text message reader was disabled and audio properly routed if the Bluetooth was disabled while connected to a device or if the service was killed after text message reading.

# 2.3.16 January 28 2012 (market) #
  * Fixed [issue 87](https://code.google.com/p/a2dpvolume/issues/detail?id=87)

# 2.3.15 January 27 2012 (market) #
  * Fixed [issue 84](https://code.google.com/p/a2dpvolume/issues/detail?id=84)
  * Fixed [issue 85](https://code.google.com/p/a2dpvolume/issues/detail?id=85)

# 2.3.14 January 22 2012 (market) #
  * Changed attributes so app would show up in Market for tablets
  * Fixed [issue 82](https://code.google.com/p/a2dpvolume/issues/detail?id=82)
  * Added checks to ensure location listeners were unregistered if app is closed.  There were some rare complaints that A2DP Volume was keeping the GPS listener active and killing batteries.
  * Added a "none" device to clear Bluetooth launch in the edit device screen.  Now you don't have to backspace it out.

# 2.3.12/2.3.13 January 12 2012 (market) #
  * Fixed [issue 80](https://code.google.com/p/a2dpvolume/issues/detail?id=80)

# 2.3.11 January 10 2012 (market) #
  * Added to AIDL interface to support Honeycomb and up, fixing [issue 78](https://code.google.com/p/a2dpvolume/issues/detail?id=78).  This was also a problem for ICS.
  * Fixed [issue 77](https://code.google.com/p/a2dpvolume/issues/detail?id=77).

# 2.3.10 December 23 2011 (market) #
  * Implemented SMS contact look up per [issue 64](https://code.google.com/p/a2dpvolume/issues/detail?id=64).

# 2.3.9 September 20 2011 (market) #
  * Fixed [issue 73](https://code.google.com/p/a2dpvolume/issues/detail?id=73).

# 2.3.8 September 5 2011 (market) #
  * Changed TTS initialization to only happen when device is connected to help free up resources.
  * Added updated German and French translations
  * Added check before launching location capture to improve reliability

# 2.3.7 September 3 2011 #
  * Fixed [issue 68](https://code.google.com/p/a2dpvolume/issues/detail?id=68) and [issue 70](https://code.google.com/p/a2dpvolume/issues/detail?id=70)
  * Added SMS stream configuration for alarm stream
  * Added a check to ensure all devices in the database have mac addresses.  There was a crash report for a null pointer exception that appeared to be caused by a missing mac address.

# 2.3.6 August 31 2011 #
  * Added French translations (real ones this time)

# 2.3.5 August 30 2011 #
  * Working on [issue 63](https://code.google.com/p/a2dpvolume/issues/detail?id=63) and [issue 68](https://code.google.com/p/a2dpvolume/issues/detail?id=68) (but not quite done)
  * Added preference for TTS stream

# 2.3.3 August 28 2011 #
  * Added configurable delay in preferences for reading text messages.  Defaulted this to 3s.  Can set 0s, 3s, or 10s.
  * Added a check when user selects enabling TTS in a device if it is not enabled in preferences it will enable it and check TTS installation.
  * Added check is Bluetooth SCO is not available off-call then will revert to music stream for TTS.

# 2.3.2 August 24 2011 #
  * Fixed force close if both car dock and home dock were connected at the same time.
  * Fixed bug in stored volume that would leave it latched to max if the volume had been at max once when a device connected.  This bug has been there for a very long time.  Also fixed this for stored in-call volume.
  * Fixed notify icon updates and volume controls to handle multiple connections and disconnects properly.

# 2.3.1 August 23 2011 #
  * Finally fixed [issue 63](https://code.google.com/p/a2dpvolume/issues/detail?id=63).  Now it uses the phone call Bluetooth speaker/stream for reading the SMS messages.

# 2.3.0 August 21 2011 #
  * Added in-call phone volume control per [issue 59](https://code.google.com/p/a2dpvolume/issues/detail?id=59).

# 2.2.5 August 21 2011 #
  * Changed volume adjustment toast message (old vol - new vol) to a media volume UI display
  * Capture old volume using preference service to improve robustness.

# 2.2.4 August 16 2011 (market) #
  * Added music stream pause when reading text message
  * Improved localization
  * Added 10s delay in reading text message to get past most notifications (until a better solution is found)

# 2.2.3 August 14 2011 (market) #
  * Renamed the Headset Plug to Audio Jack.  Also made this a resource string to enable translations
  * Fixed potential infinite loop when TTS service is not found.  Now uses a dialog box to prompt the user before installing TTS.

# 2.2.1 & 2.2.2 August 13 2011 #
  * Fixed [issue 61](https://code.google.com/p/a2dpvolume/issues/detail?id=61) by removing the media volume slider bar on the main screen.  It really served no purpose and just drove confusion.
  * Fixed [issue 60](https://code.google.com/p/a2dpvolume/issues/detail?id=60).

# 2.2.0 August 13 2011 #
  * Added SMS reader feature per [issue 47](https://code.google.com/p/a2dpvolume/issues/detail?id=47).
  * Small bug fix in service to prevent force close when service was stopping.

# 2.1.0 August 12 2011 (market) #
  * Added support for the stereo plug (headset jack) per [issue 56](https://code.google.com/p/a2dpvolume/issues/detail?id=56).  Now A2DP Volume will respond to a headset connect and disconnect like any other device.

# 2.0.18 & 2.0.19 August 11 2011 #
  * Improvements in the location capture and reporting
  * Removed redundant variables in the service.java and StorLoc.java files
  * Close database on destroy
  * Some variables designated as volatile to improve robustness
  * Fixes for [issue 58](https://code.google.com/p/a2dpvolume/issues/detail?id=58).

# 2.0.17 August 10 2011 (market) #
  * Fixed bugs in service that could cause force close if a Bluetooth device connected that was not in the database
  * Fixed list view so user can click anywhere in row instead of just on word.
  * Proper closing of database and registering and unregistering of broadcast receivers to prevent memory leaks.


# 2.0.16 August 9 2011 #
  * Fixes for [issue 44](https://code.google.com/p/a2dpvolume/issues/detail?id=44).
  * Some simplicity improvements in the code that speed it up and make it more reliable.
  * Fixed bug that would cause service to automatically restart if it was turned OFF but screen reoriented.
  * Updates to German translations

# 2.0.15 August 7 2011 #
  * More updates for [issue 57](https://code.google.com/p/a2dpvolume/issues/detail?id=57)
  * Added real German translations (Thanks Uwe!)
  * Fixed home dock preference bug

# 2.0.14 August 7 2011 #
  * Fixes for [issue 57](https://code.google.com/p/a2dpvolume/issues/detail?id=57)

# 2.0.13 August 6 2011 #
  * Removed Spanish ([issue 55](https://code.google.com/p/a2dpvolume/issues/detail?id=55)) and Portuguese language support after complaints of poor translations being unusable.  We now have a person working on real translations for French, Spanish, and German.  All future language support will be manual by people who know both English and the language to translate into and not using Google translate.
  * Numerous fixes to handle multiple simultaneous connections and ensure volume control is correct.
  * Fixed device list update to show `**` by all currently connected devices.
  * Implemented a Bluetooth disabled listener to remove all connected Bluetooth devices from connected device list when Bluetooth is disabled.
  * Implemented connected icon per [issue 32](https://code.google.com/p/a2dpvolume/issues/detail?id=32).

# 2.0.11 and 2.0.12 July 31 2011 (market) #
  * Fixes for [issue 52](https://code.google.com/p/a2dpvolume/issues/detail?id=52) and [issue 53](https://code.google.com/p/a2dpvolume/issues/detail?id=53)

# 2.0.10 July 28 2011 #
  * Added StoreLoc.java service to handle the location capture stuff separately from the main service.  This was done to correct random force close issues as well as [issue 44](https://code.google.com/p/a2dpvolume/issues/detail?id=44) and [issue 50](https://code.google.com/p/a2dpvolume/issues/detail?id=50) on the issues list.
  * Other robustness improvements and bug fixes.  See change log in source.

# 2.0.9 July 22 2011 #
  * Added Pandora integration, custom Intents, shortcut builder, and homescreen shortcuts to the start app on connect feature.  This was also created leveraging parts of code from App Alarm Pro.  This was part of [issue 38](https://code.google.com/p/a2dpvolume/issues/detail?id=38).

# 2.0.8.2 July 15 2011 (market) #
  * Bug fix in use of Log function.  Was sending null's in some cases and force closing.
  * Bluetooth connect now checks to see if the device is in the DB.  If not, it will automatically add it.  lacking this was causing force close when the Bluetooth device disconnected and the device was not in the DB.

# 2.0.8.1 July 14 2011 (market) #
  * Fix for [issue 45](https://code.google.com/p/a2dpvolume/issues/detail?id=45).
  * Fix force close on disconnect of some devices when database entry missing.

# 2.0.8 July 11 2011 (market) #
  * Actually got bluetooth auto-connect feature working and tested thanks to users who commented on [issue 35](https://code.google.com/p/a2dpvolume/issues/detail?id=35).  This version was released to the Android Market.

# 2.0.6  & 2.0.7 July 2 to 10, 2011 #
  * Implemented the package browser parts from App Alarm Pro to be more user friendly.  Since this loads slow, I also implemented a long click listener to just pull up the old simple package list.
  * More fixes for bluetooth auto-connect feature.
  * Added Home Dock support.

# 2.0.5 July 1 2011 #
  * Added preference for internal storage of location files instead of SD card storage.  This was implemented per [issue 42](https://code.google.com/p/a2dpvolume/issues/detail?id=42).  While implementing this the SD card directory changed from BluetootVol to A2DPVol to better match the app name.

# 2.0.4 & 2.0.3 June 30 2011 #
  * Added a false Bluetooth device for Car Dock.  This was done to give the car dock all the features like any other device.
  * Fixed [issue 43](https://code.google.com/p/a2dpvolume/issues/detail?id=43).
  * Fixed a bug where the device list was not updated on the main screen after a device was edited.
  * Fixed the implementation of bluetooth auto connect using the .aidl file.  This was implemented incorrectly in 2.0.2.

# 2.0.2 June 26 2011 #
  * Added automatic wifi toggle per [issue 41](https://code.google.com/p/a2dpvolume/issues/detail?id=41).  Configure by device.
  * Added bluetooth auto-connect feature per [issue 35](https://code.google.com/p/a2dpvolume/issues/detail?id=35).  Configure by device.
  * Replaced the device configuration menu to support new features.  Added packager list for package auto-launch.

# 2.0.1 June 18 2011 #
  * Added a package auto launcher per [issue 38](https://code.google.com/p/a2dpvolume/issues/detail?id=38).  Each device can auto launch a package (another app) when the bluetooth connects.  Find the package name with an app like Android System Info or App Referrer.  Type this in to the blank text box in the edit device dialog.  For example, you can launch Pandora Radio by entering com.pandora.android in the blank text box.

# 1.4.5 May 21 2011 (market) #
  * Removed French language support because of bad translations
  * Improved location capture, added car\_mode detail file and reduced write cycles to SD card.

# 1.4.4 April 24 2011 (market) #
  * Removed German language support.  User complaints said translations in German were bad and asked to remove that support.
  * Added some more robustness checks on viewing Bluetooth device data (long press on item)

# 1.4.3 April 21 2011 (market) #
  * Added more details to the device specific location capture and cleaned up some inconsistencies with this data.
  * Code consolidation and cleanup in the service.java file.  This may reduce ram needed to run.

# 1.4.2 April 20 2011 #
  * Bug fixes that caused the app to force close when disconnecting bluetooth.  Added null checks in several places in service and data base interactions.

# 1.4.0 April 19 2011 #
  * Device specific location capture and storage.  Now creates an html file in the data export directory named after the device name.  This file opens in a browser and shows the user a link.  Clicking the link will act just like the widget but will show the location for this device only.  Long press of a device in the list now has a button in the dialog for location.  Clicking that button will open the location file in the browser.

# 1.3.7 and 1.3.8 April 2011 (market) #
  * Fixes for volume capture and return.

# 1.3.6 January 5 2011 (market) #
  * Added preference to allow user to enable or disable the use of passive location services with this app.
  * Added preference to allow user to enable or disable the use of network location services with this app.
  * Changed the app so that it forces only a single instance to be running at any time.  This prevents multiple instances from concurrently running.

# 1.3.5 December 26 2010 (market) #
  * Added a listener trigger for NETWORK\_PROVIDER for the location capture.  This was done to handle devices that don't have satellite GPS such as Samsung.  This was also added to the 1.3.4.1 version of the A2DP Volume A2.1 version.
  * Added a listener trigger for PASSIVE\_PROVIDER for the location capture.  This should help get a faster location capture in some cases.  This is only supported on Android 2.2 and higher.
Here is more information on the location listeners:
http://www.android10.org/index.php/articleslocationmaps/226-android-location-providers-gps-network-passive

# 1.3.4 December 24 2010 (market) #
  * Added a check to make sure bluetooth was enabled when finding devices.  The app will now ask you to enable bluetooth if it is not enabled.  Once enabled, then it will find devices.

# 1.3.3 December ?? 2010 (market) #
  * Added a dialog box when clicking the "delete data" button in the menu.  Now it will ask if you really want to wipe out the entire database before just doing it.  I found out I needed this that hard way.
  * Added checks to the database access to prevent errors accessing an empty database.  This also allowed the removal of the dialog saying you must close and reopen the application. Added intent to trigger list reload automatically. This was an error posted to the Android Market.  Only the 2nd error ever reported after over 1300 downloads.
  * Added Portuguese language support. Also moved many more strings to the strings file so more things are translated.
  * Changed the notify to show a car icon when connected to a bluetooth device and the normal app icon when not connected but running.  If you pull down the message from the status bar it will tell you what device is currently connected as well.
  * Exported locations now have a .txt extension so they can be opened easier.

# 1.3.2 December 4 2010 (market) #
  * Removed check for audio service when finding bluetooth devices.  Samsung phones did not support this so no devices were ever found.  This also gives the ability to ignore other bluetooth devices such as computers or other phones.
  * Added Spanish language support.  This was done using MOTODEV which uses Google Translate to make the translated strings.

# 1.3.1 November 27th 2010 (market) #
  * Fixed a major bug in the way the preferences and defaults were handled.  The issue was with the defaults being set incorrectly which made loading them fail and skip all user settings.  In addition, the default GPS timeout was incorrectly set to 15ms instead of 15,000ms (or 15s).  This made the GPS listener worthless unless the user changed this value.
  * Added a status bar icon feature that can be enabled in the preferences.  When enabled, an icon will show in the status bar anytime the service is running.  This helps drastically reduce the chances the service will be closed due to low memory as it now has the same priority as an active application.

# 1.3.0 (market) #

  * Added widget for retrieving last stored location.  Now you can add this widget to the home screen.
  * Added configuration to allow user to stop the pop-up messages.  Some folks may not like those.
  * Added multi-language support (or at least started)  It now supports French (mostly) as well as English.  I just used Google translate for this.  I know these mechanized translations don't always work right.  If anyone want to help translate, let me know please.
  * Added last location data exporter.  Exports a text file to the `BluetoothVol` directory on the SD card.  This is in the Manage Data screen.
  * Several improvements to the way the service starts and stops, and reports status.  The service now starts be default when you open the application.  If you make preference changes, the service will reinitialize to load the new preferences.
  * GUI changes to make it a bit more intuitive and have a better layout.  Too many people did not understand what "Location" meant.


# 1.2.5 (market) #

  * Added support for capturing car location when leaving Car Mode on the phone.  Added a preference setting so the user can enable or disable this feature.

# 1.2.4 November 2010 (market) #

  * Fixed the issue of force close when no location data was available when the bluetooth disconnected.  This was the very first ever crash report for this application.
  * Fixed the problem where the dialogs would disappear when the orientation of the phone or the keyboard state changed.
  * Fixed the edit device dialog so that it was viewable in landscape mode.
  * Created a better icon