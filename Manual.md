#User Manual



Caution:  There are copies of this app being sold or redistributed with ads.  Do not use these copies!  Make sure you get the real version on the play store by developer JimRoal (https://play.google.com/store/search?q=JimRoal&c=apps). My software is free, open source, and no ads.  I create it for my own use and give it away for free, no strings attached.  It is just a hobby for me.

# Using the A2DP Volume app #
This manual represents the latest version of the A2DP Volume application.  If you have an older version, some of the features below may not be supported, or may work differently.

# Installation #
Requires Android 4.03 or higher.  Version 2.9.8 and below required Android 2.2 and most of those versions are still available on the downloads page.  If you have an older device and you go to the Play Store you can still download 2.9.8.

  * Use an Android bar code scanner from your Android device to scan this QR code
![http://jimroal.com/exe/QR.png](http://jimroal.com/exe/QR.png)
  * Follow the instructions when the download complete to install the launch the application

Make sure you set your task killer to ignore this application.

If you would like this app to do something else please submit an enhancement request here: http://code.google.com/p/a2dpvolume/issues/list
Some people have given the app a 1 star rating for it not having features they wanted.  Please do not give the app a bad rating for not having the features you wanted!

You can also just search Android Market for A2DP Volume
# Overview #
## Menu Buttons ##
  * **Apps for Accessibility:** Opens a dialog to select which apps you would like to have the notifications read for while connected.
  * **Preferences:** Opens the preferences menu to edit preferences.
  * **Delete Data:** deleted the device table allowing you to completely rebuild from scratch.
  * **Manage Data:** opens a screen with commands for importing and exporting the device data.
  * **Exit:** will kill the service and completely exit the program.
  * **Help:** takes you to this website.

## Main Screen Buttons ##
  * **Location:** retrieves the last captured location and opens it as a URL.  This is for viewing the last captured location (not for capturing it).
  * **Find Devices:** scans the Android device for all Bluetooth audio devices and loads them into the database and list.
  * **Start Service or Stop Service:** this button is used to start or stop the background service task.  This service must be running have any of the features work.  You can stop the service to prevent the features from working temporarily (until you start it again).


## Edit Device Dialog Box ##
  * **Top text box:** Enter a name you would like for the device such as the name of the car.
  * **Get Location On Disconnect?:** check to capture location when this device disconnects.  If unchecked location listener and data are not captured for this device.
  * **Disconnect WIFI when Bluetooth connects:** Check this box if you need to have WIFI automatically disabled when the device you are configuring device connects.  When the device disconnects, the WIFI state will return to its previous state.
  * **Enable GPS while connected:** Check this box if you want GPS enabled while this device is connected.  When the device disconnects, GPS will stay enabled until the location is captured, then will return to the previous state. Note: This was no longer supported in Android 4.0 and up and it was completely removed from the app in 2.11.2.
  * **Silence notifications while connected:** Check this box if you don't want your music interrupted with notifications while driving.
  * **Enable Reading Text Messages?:** if checked, and you have enabled this in the preferences as well, text messages (SMS) will be read to you via the audio stream while this device is connected.  Version 2.9.0 and up will also read Google Talk messages if you are signed in with your Google Talk app.  Version 2.11.0 and up added the ability to read notification messages from several apps like Hangouts, Skype, calendars, IBM Sametime, and a few others.  **You must enable A2DP Volume in accessibility in Android to make this work!**
  * **Set Media Volume on connect?:** if checked, the media volume will be adjusted to the slider bar value when a device connects.
  * **Media Volume slider bar:** is the media volume that will be set when connected to Bluetooth.  Note:  This only raises the volume to the max level supported by the device.  This is not a volume booster app.
  * **Set Phone Volume on connect?:** if checked, the in-call phone volume will be adjusted to the slider bar value when a device connects.
  * **Phone Volume slider bar:** is the media volume that will be set when connected to Bluetooth.  Note:  This only raises the volume to the max level supported by the device.  This is not a volume booster app.
  * **Start app on connect:** If you want another app to launch when the Bluetooth device connects, use the button to select a package name to launch.  Select a package from the list and it will show in the text box below the button.  For instance, entering com.pandora.android in this text box will launch Pandora Radio when the device connects.  Leave this field blank to disable the auto launch for this device.  This feature is available on version 2.0.0 and above.  2.0.9 and above gives you many other launch features including: create a shortcut, home screen shortcuts, Pandora playlist, and more.  2.8.17 added a check to make sure the phone was not ringing or on call before launching app.  If a call is active, the app will not automatically launch.
  * **Force app restart?:** If this is checked it will call the app restart function when launching an app you selected above.  Since Android 2.2 this will only affect background tasks.  It really depends on the app you are starting as to whether it is needed or not.  You will just have to try it both ways to get the desired result.  It will likely have no affect on most apps.
  * **Stop app on disconnect:** If you check this box A2DP Volume will stop the launched app on disconnect.  While this generally works, some apps (like Pandora) do not play well with others and may sometimes persist.  If you uncheck this box A2DP Volume will still pause the music on disconnect but will not kill the app.
  * **Launch Car Mode:** This will put the Android UI in **Car Mode**.  Behavior here all depends on your device and what car mode means for it.  Most devices will open a new UI optimized for driving. Others may require the **home** button be pressed to enter car mode. (added in 2.11.2)
  * **Also connect to Bluetooth device:** If you want to connect to another paired device, use the button to select that device to connect to.  The device MAC address will show in the test box.  You can clear the text box so it will not also connect to any Bluetooth device.  See the tips wiki for more details.
  * **Connected Icon:**  This allows you to select an icon for when a device connects.
  * **Delay for reading text message:**  This is the time delay between notification of a text message (SMS) and the reader actually reading it to you.  This is used to handle your phones notification to prevent the text message from being read until your notification is done.
  * **SMS Stream:**  A stream is a path for the sound to travel.  The phone and connected Bluetooth equipment will support various streams.  This selection will let you choose which one the text message is read through.  Music Stream will use the stereo A2DP stream.  Voice Call Stream will use the handsfree speakerphone stream.  Alarm Stream will use the path that alarm sounds use.  Each phone and Bluetooth device is a bit different.  Some of these may not work properly with your devices.  You will just have to try each of them to see what works best for your situation.  Since 2.9.0 this stream will also be used for reading Google Talk messages.
  * **Volume adjust delay:**  This is the time in seconds that A2DP Volume will wait after the first Bluetooth profile connects before the volume is adjusted.  This is used to prevent volume adjustments prior to the A2DP stream being active.
  * **Automatically remember last used media volume on disconnect:** Check this to have A2DP volume capture where you left the media volume while the device was connected to use when the device connects again.
  * **Ramp volume up:**  You can use this to have the volume ramp up rather than just jumping to the connected setting.  The ramp will increment the volume once per second until the selected volume is reached.
  * **Save:** after making your changes, you must press this to save your changes.  The back key will also save and exit in version 2.8.0 and up.

# Usage #
## Basic use ##

The service is automatically started when you launch the application.  The button text will change to "Stop Service" and a message will pop up saying the service has started (unless you configured the pop-ups off in the preferences).  The program is now ready to manage a2dp volume and capture location.

At this point you have not yet created specific settings for any of your devices for specific features.  New Bluetooth devices will be created with the default settings to adjust the media volume to max on Bluetooth connect, and capture your location on Bluetooth disconnect or Car Mode exit.

In order to read notification messages while connected you must enable A2DP Volume in Android accessibility services.  To do this open Android settings.  Then open **accessibility**.  You should see A2DP Volume near the top under **Services**.  Click it to open the settings.  Now turn it ON with the switch on the top right.  More details here: https://code.google.com/p/a2dpvolume/wiki/Accessibility_Settings

## Setting up devices ##

You must use the standard Android Bluetooth manager to find and pair with devices before using this application.  In order to manage device specific settings, click the Find A2DP Devices button.  This will look for all bonded (paired) Bluetooth devices, create a new database table, and load your Bluetooth devices into it.  Bonded devices are all the devices you have ever paired with and stored.  You can click on any device in the list to edit the settings. Click the save button to store your device settings.  The back button will exit the device editor without saving your changes.

If you click the Find Devices button after you have already loaded devices any new devices will be added to the database with default settings (set volume on connect, max volume, device name = received name in Android).  Any devices already in the database are unaltered.  Unpaired devices do not get removed during this function.  You can remove a device by short press on the device in the list and then use the delete button.

Make sure your Bluetooth is enabled before scanning for devices.  Devices may not be found if Bluetooth is not enabled.

If "respond to car mode" is checked in the main preferences, a "Car Dock" device will be added to the device list.  If respond to Home Dock is checked, a "Home Dock" device will be added to the list. If respond to Headset is checked, a "Headset" device will be added to the list. These devices can be configured like any other device in the list.  They are not Bluetooth devices.  Instead, they are modes the Android device can trigger.  This is useful if you want to enjoy all the same features of a Bluetooth device (volume adjust, location capture, etc).  Once you enable these special devices, you need to click the "find devices" button to get them added to your device list.

## Preferences ##
There are several preferences for the user to set.
  * Start at Boot.  If you check this box the program will launch the service in the background after the device is done booting up.  This way you don't need to remember to start it every time you restart the device.  **In order for this to work, you must install to the device and not the SD card.**
  * Show Pop-ups.  The messages that will not show if you uncheck this include: message when service starts and stops, the countdown timer when the GPS listener is looking for a location, and the message that pops up when your device connects via Bluetooth.  Other messages such as errors or no data are still shown.
  * Notify in Foreground.  Actually this does more than just put an icon on the status bar.  It also keeps the service running more often by giving it priority as a foreground service.  By default this is enabled always and it is recommended you keep it that way.  Enabling it ensures the service can perform it's function without being stopped by another application when the device is low on memory.  This does not guarantee the service will never get stopped in severe low memory conditions but it makes it far less likely.  Some don't like having the icon in the task bar so I made this a preference.  However, if you disable it the app may not work sometimes.  In 2.11.2 another option was added to show the icon only while connected.
  * Use Local File Storage.  Checking this box will use the device memory to store the device specific location files.  Uncheck it to store these files on the external memory (SD card).  The external memory is preferred if you ever want to copy, send, save or do anything else with these files.
  * Respond to Car Mode.  This will create a Car Dock device in the device list (after find devices is used) that will react to the Car Mode connect and disconnect events, similar to a Bluetooth connect/disconnect event.
  * Respond to Home Dock.  This will create a Home Dock device in the device list (after find devices is used) that will react to the Home Dock connect and disconnect events, similar to a Bluetooth connect/disconnect event.
  * Respond to Headset.  This will create a Headset device in the device list (after find devices is used) that will react to the Headset (stereo jack) connect and disconnect events, similar to a Bluetooth connect/disconnect event.
  * Respond to Power Connection. This will create a Power Connection device in the device list (after find devices is used) that will react to the device power connect and disconnect events, similar to a Bluetooth connect/disconnect event.  Power connection refers to plugging the device into a power source to charge the battery.
  * Enable Reading Text Messages.  This will enable the text message (SMS) reader feature.  This feature will read the text message over the audio stream using Google Text To Speech (TTS) services.  You must has a TTS service installed for this to work.  There are several TTS applications on the Android Market for free.  Pico TTS or eSpeak TTS for instance.  When you enable this feature A2DP Volume will test for a TTS service being available and ask you to install one if not.  Enabling this feature here just prepares A2DP Volume to read SMS.  You must also set up the devices you want this feature to work on individually.  For more info on this feature see http://code.google.com/p/a2dpvolume/wiki/Reading_SMS
  * Enable reading notification messages.  This feature allows the app to read out certain messages from the notification bar.  You MUST have A2DP Volume enabled in the Android accessibility services for this to work.  When a new notification shows up on the notification bar, A2DP Volume will read it out loud.  See details below, and here https://code.google.com/p/a2dpvolume/wiki/Accessibility_Settings
If you don't want to use this feature you can uncheck this preference.  You do not need to unselect all apps in the "apps for accessibility".  Also, you can turn this OFF in Android accessibility settings.
  * Hide Volume Pop-up.  When A2DP Volume adjusts volumes a user interface is shown so you can see the adjustment being made.  Some users complained about this feature so I added this setting to hide it if you like.
  * GPS Location Listener Timeout.  This sets the maximum time the location services (all of them) will run looking for a better location fix.  After this time, the location listeners will give up and turn OFF.  This is done for 2 reasons.  The most important reason being that after too long you are not likely near your car anymore.  The other being to conserve the battery.
  * GPS Max Inaccuracy.  This is the maximum inaccuracy to accept before turning OFF location listeners.  In most cases set this to the minimum (2 meters).  However, you can set this higher to accept a less accurate fix in exchange for a quicker location capture.
  * Use Passive Locations.  Passive locations are locations that are requested by other programs, or locations captured using other methods.  I have found this to be rather unreliable at this point so the default is to keep this OFF.  However, if these improve in the future, or your device has a hard time capturing the location fast enough you may want to try it.
  * Use Network Locations.  Network locations include those obtained using the cellular towers and WiFi hotspots.  Some devices don't support satellite GPS so this would be the only method they have of acquiring location.  This should be ON.  Network locations can be captured faster but are generally less accurate than satellite GPS.


Note: you must stop and restart the service if you made preference changes while the service was running.  Don't make the changes while connected to a device since some of the changes will not take affect and the connected device will not be detected.  Make preference changes before you have a device connection established.

## Reading Notifications (Accessibility Service) ##

Version 2.10.0 and later added a feature that uses Android accessibility service to read out notifications from many apps while you are connected to a device.  The apps can include anything you have installed on your device such as Gmail, Hangouts, Skype, K9 Mail, Android and Motorola email clients, and Google Keep reminders or any other app you set up in the configuration screen.

Since this feature utilized the accessibility framework within Android, you must enable A2DP Volume in the accessibility settings menu in the Android settings.  Disabling this feature is done in the same menu by turning OFF A2DP volume.

When a notification comes in, and you have connected to a device (A2DP Volume shows connected) the text that is briefly shown in the notification bar will be read to you.  The text that is shown varies by app.  For more information and detailed instructions, go here: https://code.google.com/p/a2dpvolume/wiki/Accessibility_Settings

It is up to the apps to determine what they put in notifications.  Hangouts for instance will put the text of the first unread message in the notification but subsequent messages from that sender will get rolled up into the sender name and the number of unread messages.  For instance, the first unread message might say "Joe Smith: hi".  The next message Joe sends will be shown as "Joe Smith: 2 new messages".  Skype will put the text of every message in the notification.

## Retrieving Location ##

After your device has disconnected a Bluetooth connection a location will be stored (if you configured it to do so above).  The location will also be captured when Car Mode is exited in Android (where supported).  Now you can click the Location button and see the location that was captured.  It opens as a map URL.  This can be viewed many ways.  Google maps will open and show the link.  You can also use a browser.  I like to use GPS Status (free application on Android Market market://search?q=pname:com.eclipsim.gpsstatus2 ). This gives you flexibility in how you use the captured location data.

If you push the Location button prior to ever capturing a location a "no data" message will be shown. This happens because you have no location data file captured yet.

There are settings in the preferences menu to set the GPS Listener Timeout and GPS Max Accuracy to Store.  The GPS Listener Timeout is the time after disconnect that it will keep the GPS alive trying to find a better location.  Setting this higher will look longer for a better location but you may not be as close to your car by the time it gets one.  About 20 seconds is recommended for most people.  The GPS Max Accuracy to Store is the distance in meters that it will accept to store the location and then stop the GPS listener.  Setting this smaller will capture a more accurate location but, depending on the time you give it in the previous setting, you may have walked farther from your car.  The GPS listener will stay alive trying to get the best possible location until one of these conditions are met.  About 10m (30 feet) is recommended for most people.  Your can completely disable the GPS listener by setting the timeout to "Disable GPS Listener".

Short press of the Location button retrieves the best stored location.  Long press retrieves the most accurate.  This does not matter much anymore since it will launch a GPS listener after disconnect (when configured to do so).  In most cases a new GPS location is captured and this will be both the most recent and the most accurate.  However, if the GPS listener times out, or GPS is not enabled, then you may need to know the most recent and most accurate.  The device name, time/date, and accuracy is displayed in the URL.  If the location was capture from a Car Mode exit, then the URL will have "My Car" instead of the device name.

If you have disabled any location services you may not get a decent location in the allotted time.  If you are in a place where a location cannot be determined, your last good location will be captured.  If you have just booted the device, and there is no location information at all, nothing is stored.  You will see the last location that you stored if you press the "stored location" button.  Always look at the date, time, car, and accuracy of the location to determine it's validity.

Long press of a device in the list will open a dialog showing details about that device.  It also has a button called Location.  Clicking that button will open up the last captured location file for that specific device.  If the file has not been created, a file not found error will show.  This way you don't lose the location of any device because a new device connected later.  The file opens in a browser and shows details about the location data.  It also has hyperlinks to open the location data in your favorite app that accepts location data (such as maps or GPS Status).  You can bookmark that file location and place it on your home screen (use the bookmarks widget in Android) for convenience.

There is also an A2DP Volume Widget.  Place the widget on your home screen.  A single click of the widget will show the last captured location of the most recently disconnected device.

## Database ##

There is a utility in the options menu to export the database to the SD card as XML or SQLite database.  I found this in an example and decided to implement it.  There is an import feature as well.  This assumes that you exported earlier and it looks in the export directory defined int he application for the file name also defined by the application.  It can only import a SQLite database that was created by the application.

If you just want to start over, you can delete the entire database using the Delete Data in the options menu.

Devices in the list on the main screen can be edited or deleted using a short press on the device name.

## Service ##
The service is the part of the program that runs in the background and responds to Bluetooth connect and disconnect events.  When the service is running the old media volume will be captured and the media volume will be adjusted on Bluetooth connect based on the device that connected and your settings for that device.  On disconnect the media volume will be restored and the location will be captured.  If the service is not running, none of this will happen.  There is a button on the main screen to start and stop the service.  If the button says "Start Service" then the service is not currently running.  If the button says "Stop Service" then the service is currently running.  If you configured your preferences to start at boot then the service automatically starts when the phone is finished booting up.

Android manages it's memory by closing applications that are not currently being viewed by the user.  This can kill the service too.  If you see a notification that the service has started when you did not just start the service, this is what happened.  In order to prevent this, select the "Notify in Foreground".  This will place an icon on the status bar which makes the service act as an active task.  This prevents the other processes from stopping it in most cases.  It is still possible in extreme cases that the service could get stopped.  By default this is disabled in the preferences.

## Widget ##
The widget can be used as a shortcut to view the last stored location.  In order to place the widget on the home screen, do the following:
  1. Find a blank place on a home screen (can be any of the home screens) and long press.
  1. A list of widgets is displayed.  On some devices you may have a few categories of widgets.  Select "Android Widgets" if you have more that one.
  1. Select "A2DP Volume" widget.  The widget will now be placed where you long pressed.

## Caveats ##

  * If you plan to use the text message reader, you must first enable it in the preferences.  You must ALSO enable it for each device.  Different phones and Bluetooth devices act differently.  You will need to try each of the SMS stream settings (in preferences) until you find one that works best.  If none work for you, please post an issue with details about your phone and device details.

  * Long press of a Bluetooth device in the list will show a pop-up window of the details of that device.  There is also a button for the devices last captured location.

  * Car Mode is supported in Android 2.2 and higher.  This is generally triggered by an application that is preinstalled in your device, but other applications on Android Market will also enter and exit Car Mode. For instance, on the original Droid the application is called "Car Home".

  * Droid 2 Car Dock does not trigger Car Mode for some strange reason. You can get other Car Mode applications in the Android market that do trigger Car Mode.

  * If you want to trigger a location capture manually, just open and close your Car Mode application.

  * If the application is killed with a task killer application, the features will not work correctly.  You may not capture a location on disconnect, the volume may be left at the connected setting, etc.  Make sure you add it to the ignore list in your task killer.

  * The device specific location data is stored in html files.  You can use a file explorer such as ES File Explorer to locate these files. Prior to version 2.0.5 they were stored in a directory called A2DPVol.  Long press the file you are interested in and it will give you the option to create a desktop shortcut for it.  Now anytime you want to see the last stored location of that device, you can just click this icon from the desktop.

  * Be careful when using the Bluetooth device connect feature.  Make sure you have that device configured properly to prevent any circular loops. For instance, don't have device 1 connect to device 2, and device 2 connect to device 1.  This may cause an infinite loop.  If this happens, get the device out of range so it cannot connect to any Bluetooth devices and reconfigure the devices in A2DP Volume.

  * When using the Bluetooth device connect feature the first device (usually home dock or car dock but can also be a Bluetooth device) will connect, then drive the connection of the A2DP device.  When that A2DP device connects, it will also trigger a connect in A2DP Volume.  Because of this, configure the A2DP device with the volume adjustment and app launch features (is desired).  If you configure the first device, the 2nd device may override the changes from the 1st device.  Also, the volume will likely get left at max even after disconnect.

  * The start app on connect feature can start apps but Froyo removed the ability to stop apps that are in the foreground.  A2DP volume tries to kill it (if you select the "stop app on disconnect" checkbox) using both the apprestart function and the killbackgrundprocess function but if the app is in the foreground these functions will be ignored by Android.  Also, shortcuts or anything that does not have a package cannot be stopped by A2DP Volume once started.

  * If you have your Bluetooth disabled and a device connects that is using the feature to force connect a Bluetooth device, the Bluetooth will automatically be enabled to allow the connection.  When the first device disconnects, the Bluetooth will be returned to the state it was before connection.  A use case for this is when you have a physical car dock and you force connect a Bluetooth A2DP receiver.  When you put your phone in the car dock your Bluetooth will auto-enable to allow the force connect.  When you remove your phone from the car dock, Bluetooth will return to disabled.

**Please post all enhancement requests and defect reports to the issue list: http://code.google.com/p/a2dpvolume/issues/list**