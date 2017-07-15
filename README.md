A2DP Volume
===========

Please read the [manual](https://github.com/jroal/a2dpvolume/wiki/Manual) to get started.

<a href="https://f-droid.org/packages/a2dp.Vol" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"/></a>
<a href="https://play.google.com/store/apps/details?id=a2dp.Vol" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="100"/></a>

Automatically adjusts the media volume on connect and resets on disconnect. This is done so the stereo streaming audio will work properly. Intended primarily for car Bluetooth systems. Also automatically captures location information so you can find where you left your car. The location can also be automatically captured when exiting Car Mode on your Android device. The location can be read by any app that understands GPS data such as Google Maps, GPS Status, web browsers, etc. A2DP Volume version 2.2.0 and above can also read out text messages using Text To Speech (TTS) services while you are connected. Each paired Bluetooth device or virtual device (home dock, car dock, power connection, or headset plug) can be configured to your liking. 

This app has experienced scope creep now and it does much more than originally intended. In addition to the media volume and location features, it now can automatically launch apps and shortcuts, connect to another Bluetooth device when the first device connects, and turn OFF WIFI and turn ON GPS when a device is active. Since version 2.2.0 it can also read your text messages for you when connected to a device. Since 2.11.0 it can also read certain notification messages. That feature can help reduce driver distraction. A2DP Volume also works with car docks, home docks, and the headset plug. Rather than just global features, each feature can be configured by device so you can completely customize your control.

The main version of this application requires Android 4.4 or higher.

One of the goals of this app is to eliminate unnecessary driver distractions. By automatically handling certain settings and events the driver can leave the phone in a pocket or purse and keep their eyes on the road where they belong. We have purposely not implemented several feature requests that would have required interaction while driving.

This app does not pair Bluetooth devices. Follow the instructions for your devices to pair them using Android. You must pair your Bluetooth devices before using this app.

This app was created for use by the developers. We make no money from it at all. It is free, open source, contains no ads, and collects no data. Once we created it we simply felt others could benefit from it as well so why not give it away. We have other day jobs. This is just a hobby.

Test versions can be downloaded by joining the [testers community](https://plus.google.com/u/0/communities/110152746998730594422) on g+.

User Information
================

The wiki is located [here](https://github.com/jroal/a2dpvolume/wiki).

Please read the [manual](https://github.com/jroal/a2dpvolume/wiki/Manual) to get started.

Looking for translators. So far this app is in US English and translated to German, Danish, and French. If you want another language supported, post an issue to the issues list with a way to contact you or [email me](mailto:jroal@comcast.net) direct.

If you can't get the text message reader working, read [this](https://github.com/jroal/a2dpvolume/wiki/Reading-Messages).

For known issues, see [the issue list](https://github.com/jroal/a2dpvolume/issues).

If you want to be part of the testers group to get access to alpha and beta versions, go [here](https://plus.google.com/communities/110152746998730594422).
After you join that community, become a tester using this [link](https://play.google.com/apps/testing/a2dp.Vol).

Basic Install
=============

. Pair device using the Android settings screen. +[menu key] -> [Settings] -> [Wireless & networks] -> [Bluetooth settings] -> [Scan for devices]+. Your device will need to show connected on this screen. [Here](http://www.youtube.com/watch?v=8-wuRA9I0RM) is a video explaining it.
. Install my app from the Google Play Store, F-Droid or this website. 
. In my app, click +[Find Devices]+. This should return a list of paired Bluetooth devices. Your Bluetooth will need to be ON for this to work. That means the Bluetooth symbol should be in the status bar area. If it is not ON this app will attempt to turn it ON when finding devices. If you plan to use this app with home dock, car dock, or the audio jack, you need to first select that in the preferences. See manual for details.
. Select each Bluetooth device from the list in my app. Short click on the device in the list will open a dialog box so you can configure it.  
. Click the +[Edit]+ (center button) and you can now edit the specific settings for the Bluetooth device. Here is where you set options you want for this specific device. See the manual for more details. 
. Click the +[Save]+ button when complete. 
. Back at the main menu in my app, you should see the right button now says "Stop Service". That means the app is ready to adjust volume and capture location data. If you were already connected to the Bluetooth device when you installed and configured my app then you will need to disconnect and reconnect for it to trigger the volume and location capture.  


If you can read software code, look in the "code" tab above.  

Issues
======

For known issues, see [the issue list](https://github.com/jroal/a2dpvolume/issues).

Please submit any bug reports or ideas for future enhancements in the issues list (issues tab above). Before submitting a new issue, make sure your issues does not already exists on that list. If it does, you can add comments and star it so you are informed of updates. Also, please submit error reports if you experience a crash. Android automates this for you. When most crashes happen Android provides a dialog that allows you to either force close or report. Please report. These reports tell me what happened so I can fix it.

Additional Features
===================

You can name each Bluetooth device with a more meaningful name. Android native gets the device name from the remote device and does not let you rename it (until Android 4.0 and up). If you have more than one of those devices, they all look the same in Android except for the MAC address.

Allows you to configure each device separately to:  

  * Capture location on disconnect. Triggers a GPS listener to capture the most accurate location. If a GPS location of a configurable accuracy is not found in a configurable amount of time, it turns the listener back OFF to save your battery
  * Adjust media volume on connect, restore on disconnect
  * Adjust phone in-call volume on connect, restore on disconnect
  * Read text messages (SMS) out loud over speaker phone or Bluetooth speakerphone while connected
  * Read notification messages (2.11.0 and up) out loud over speaker phone or Bluetooth speakerphone while connected
  * Disable WiFi while connected
  * Launch an app, playlist, shortcut, or custom intent on connect and stop it on disconnect
  * Force connect a Bluetooth device when the first device connects. For instance, when you enter Car Mode, force connect an A2DP receiver
  * Enter car mode on connect

This project was written in Java using the Android SDK. Parts of the project were leveraged from examples or other open source applications.

Other Details
=============

A2DP = Advanced Audio Distribution Profile. It is a Bluetooth communication profile for streaming stereo audio.  

This application currently only supports US English, French, and German. I can add support for other languages if people request it. Add an issue to the issues list for the languages you want supported. Be prepared to help us with the translations. If one of the non-English translations is incorrect, please enter an issue for that as well. Here is more info on what it takes to help with [translations](https://github.com/jroal/a2dpvolume/wiki/Translations).

Why do we create this app for free with no ads?  Good question. I was really not sure for the longest time until this [video explained it](http://www.youtube.com/watch?v=tJr9QajdCNc). The only donation we would like is good ratings and comments on Google Play Store. If you can participate in coding, testing, translating, etc... we would welcome that help too. Also, please spread the word to help promote this app.

Note: This application was made available on Google Play Store starting with version 1.2.1. October 30, 2010.

No warranties or liabilities expressed or implied. This is a free open source project. There are no ads and we do not collect any data.

There are clones being sold on the Play Store. At least one of these is charging for an older version. Avoid these scams!

If you would like to become a alpha/beta tester please join this [group](https://plus.google.com/u/0/communities/110152746998730594422).

Use a bar code scanner in your Android device to scan the image below. This will find the application on the Google Play Store for you.

<a href="https://play.google.com/store/apps/details?id=a2dp.Vol">
  <img alt="QR Code A2DP Volume"
       src="http://jimroal.com/exe/QR.png" />
</a>

Here are a few screenshots:

![Image](http://jimroal.com/A2DPScreens/Main.png)
![Image](http://jimroal.com/A2DPScreens/EditDevice.png)

Click [here](https://github.com/jroal/a2dpvolume/wiki/ScreenShots) for more screenshots.

Click [here](http://www.youtube.com/watch?v=3sy_pCbJHA0&list=PL8B87E2415E38D95E&feature=plpp_play_all) for the video.

I created a simple tester app that can be used to invoke Car Mode and for sending text strings to A2DP Volume simulating a message from an app. You can get it [here](https://github.com/jroal/a2dpvolume/wiki/Notification-tester-app).
