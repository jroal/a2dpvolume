#Tips and Tricks.



# Introduction #

This Wiki covers a few tips and tricks for using A2DP Volume effectively.  It also covers how to use the app to fix problems with Android and Bluetooth device compatibility issues.

# Volume does not always adjust #

Some devices will connect, disconnect, reconnect a couple times.  This causes A2DP Volume to engage several times very fast.  In the confusion an adjusted volume can be captured as last used.  I recommend not using the "Automatically remember last used media volume on disconnect" feature in the edit device screen.

![http://jimroal.com/A2DPScreens/EditDevice1.png](http://jimroal.com/A2DPScreens/EditDevice1.png)

If the problem persists, try increasing the delay time between device connecting and volume adjusting up.  I find 12s or so to give consistent results but it really depends on your mobile device and the Bluetooth device.  Results vary.

Another issue is a volume warning message on some devices.  My Motorola devices do this.  One time after boot when you try to adjust media volume up an audio warning message blocks the adjustment until you acknowledge it.  Very annoying.  I have not found a way around this other than to not reboot.  Luckily my MAXX goes for months without needing a reboot.

# My music won't start playing when commanded from Bluetooth device #

I ran into this issue myself.  I use 2 different music players: Google Play and Amazon.  Amazon does not know what to do if you have not played something since boot.  It just sits there.  Very frustrating when using the car system.  The Amazon player is actually a bit crappy but I like using their cloud and I like purchasing from them.  Google Play creates an "instant mix" if you command play from a Bluetooth device but have not yet selected anything to play.  This is much nicer.  The way I fixed this issue it I have A2DP Volume auto start Google Play Music.  I don't have it start a play list but rather just have it start.  It won't start playing by itself but since it is now in the foreground it sits ready to play your last play list or instant mix when you command play from the Bluetooth device.  You can of course also commend a play list to start but I just don't like to use that functionality since it will always start it from the beginning.

# My music stutters when playing over Bluetooth #

This is a known issue in Android that this app corrects.  Use the "disable WiFi on connect" feature in A2DP Volume.  This is a check box in the edit device screen.  Short click the device in the main screen of A2DP Volume to see the edit device screen.  Make sure the disable WiFi on connect box is checked.  Scroll to the bottom and click save.

# My Bluetooth music device won't connect when I connect to my car hands free #

A2DP Volume has a feature to auto connect another Bluetooth device when the first device connects.  This can be used in several ways.

  * When you use a home dock, car dock, or audio jack you can force connect a Bluetooth device.  The common use case is for car dock.  When you dock your mobile device in a car dock you want to also connect an A2DP receiver.  Use A2DP Volume to respond to car dock (see preferences).  This will add it to the device list.  Now edit the car dock device and use the also connect Bluetooth device to connect to your receiver.
  * You have a Bluetooth device that supports hands free and A2DP but it does not automatically connect to A2DP.  This has been an issue with some devices.  In this case use A2DP volume to also connect back to the same device.  When the hands free profile connects, it will force connect A2DP.

# A2DP Volume does not always work #
This can happen for several reasons.

  * If you allow automatic upgrading the app will not restart after the upgrade.  You can fix this by disabling automatic upgrading in Android Market.  Android Market will still notify you that upgrades are available.
  * If your device is running low on RAM it will close background apps to free up space.  To help reduce the likelihood of this, use the notify in foreground feature in A2DP Volume.  This will place the icon on the notification bar in Android and keep the app as a foreground app.  Some don't like the icon so I made this configurable but if you choose to not use the icon, you risk the app being killed on you.
  * If you do not auto start A2DP Volume it won't be running when you forget to manually start it.  Always use the start at boot feature to prevent this problem.  Make sure you always install to the internal memory and NOT the SD card.
  * If you leave the preferences menu open in A2DP Volume.  When you open the preferences menu it stops the background service.  This is done because the service will need to load the new preferences once you are done editing them.  Anytime you edit the preferences, also use the back key to save them and restart the service.  Don't navigate away from the preferences screen without using the back key.

# `FileNotFound` error and no location capture #
This happens if the device name contains characters that are not legal in file names.  For instance: "/", "\", or ":".  Rename the device eliminating all of these characters.  [Issue 100](https://code.google.com/p/a2dpvolume/issues/detail?id=100) was launched to fix this. This was fixed in 2.7.4 and up.  The illegal names are now renamed automatically.

# A2DP Volume does not start at boot #
  * Make sure you have the "start at boot" checked in the preferences.
  * Make sure you install to the device memory and NOT the SD card.  The SD card is often not mounted until after boot.  Apps on the SD card cannot auto start because of this. I could do like most and just force you to install to local memory but I prefer to give the users freedoms.
  * If the app is upgraded, it will not restart by itself.  The next time you boot the device it will work fine.