#How to get the text message reader working.

# A2DP Volume and TTS #

A2DP Volume can use any text-to-speech (TTS) engine for reading text messages (SMS) and Google Talk messages.  This requires several things to work:

  1. You must have a TTS engine installed.  Some common ones are Pico and eSpeak.  I use Pico and have good results.
  1. That TTS engine must be initialized and have downloaded the latest speech data.  This happens automatically in most cases.  You can test for this in Android by going to `[menu button]` then `[voice input and output settings]`.  Click on text-to-speech settings.  At the top click `[listen to example]`.  If this does not work, A2DP Volume wont either.
  1. You must have a default TTS engine selected.  If you have more than one TTS engine installed, and you don't have a default selected, it will pop up a dialog asking you for which one to use rather than actually reading anything.
  1. You must enable reading text messages in A2DP Volume.  This is done by checking this box in the preferences in A2DP Volume.
  1. You must ALSO select read text messages for each device.  This is done by selecting the device in the list and clicking edit in the popup dialog.
  1. You must select a valid stream.  This is done at the bottom of the preferences menu.  It is called SMS stream.  Depending on your devices, some may not work at all so you will need to try all 3 until you find the one that works best.  You can select the stream in the edit device screen.  Click a device in your list and then click edit when the dialog pops up.
  1. In the devices you would like TTS to read messages, you need to enable it in the edit device screen.  Make sure you save when done editing!  The save button is at the bottom of this screen.
  * Note: You must have TTS enabled in BOTH the preferences and EACH of the devices you want to read text messages while connected.

## If it still does not work ##
  * Reboot your device.
  * If you use Handscent: Handscent => Settings => Application Settings => Default Messaging Application = Disable.  See comment below.  If you have other apps installed that intercept SMS they may have a similar issue.
  * Make sure you are watching the Android device screen while trying to read the first text.  If any dialogs pop up, make sure you select a default before clicking OK.    It is common for more than one TTS engine to be installed on your device and if a default is not selected, it will just sit and wait until you select one.  Obviously, don't try this while driving!  Make sure you get it working before you drive anywhere.
  * Make sure you have a default TTS engine selected and working.
  * Go into `[manage applications]` in Android.  Select your TTS engines and clear defaults.  Then test the TTS engine again as described above.  You will need to select a default again.
  * Different devices work with different streams.  Try each of the TTS streams to see which one if any work for you.  This is a configuration in each device in A2DP Volume. Generally A2DP devices use the music stream.  If your device also supports hands free calling you can use the voice call stream.  Try each one.  Make sure to stop and restart the service when you change this configuration.
  * If all else fails, uninstall and reinstall A2DP Volume

# Known working configurations #
  * Droid 3 (Android 2.3.4) with Motorola T605 car kit: Use the Pico TTS engine with any of the 3 streams.  Voice call stream uses the Bluetooth hands free stream (mono).  Music stream will send to the stereo output.  Alarm stream will use the phones speaker.

  * Droid 2 (Android 2.3.3) with Motorola T605 car kit: Use the Pico TTS engine with any of the 3 streams.  Voice call stream uses the Bluetooth hands free stream (mono).  Music stream will send to the stereo output.  Alarm stream will use the phones speaker.

  * Droid X (Android 2.2) with Motorola T605 car kit: Use the Pico TTS engine with any of the 3 streams.  Voice call stream uses the Bluetooth hands free stream (mono).  Music stream will send to the stereo output.  Alarm stream will use the phones speaker.

  * Droid Bionic (Android 2.3.3 and 4.04) with Motorola T605 car kit: Use the Pico TTS engine with any of the 3 streams.  Voice call stream uses the Bluetooth hands free stream (mono).  Music stream will send to the stereo output.  Alarm stream will use the phones speaker.

  * Motorola Xoom WiFi (Android 4.1) with Motorola T605 car kit: There are some issues with Jelly Bean overall though.  Media stream only.

# If you have any helpful information... #
... please post to this wiki.  Your information can be valuable to others.  I only have Droid 1, Droid 2, Droid 3, and Droid Bionic to develop on.  I also use the emulator of course.  My only Bluetooth devices are Motorola T605, Motorola HF850, GoGrove, and a Monoprice A2DP receiver. There are hundreds of other devices out there.  If you have found ways to get it working please let others know here.  Thanks.

Note: Don't comment here if you think you fund a bug or need a reply to a question.  If you have a question email [me](mailto:jroal@comcast.net).  If it is a defect, report it on the [issues list](http://code.google.com/p/a2dpvolume/issues/list).

Accessing the edit device screen:
This screen shows the result from a short press on a device in the list.  Here you can select to edit this device or delete this device from the list.  The OK button just returns you to the app main screen.

![http://jimroal.com/A2DPScreens/Image2.png](http://jimroal.com/A2DPScreens/Image2.png)

The screen below shows where to set the enable reading text messages.  When you check that box, more dialog will appear below as shown.  There you can set the delay and the stream.

![http://jimroal.com/A2DPScreens/EditDevice3.png](http://jimroal.com/A2DPScreens/EditDevice3.png)