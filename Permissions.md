#This page explains why each permission is needed and what it is used for.

This app does not keep any contact, message, SMS, etc information nor does it send anything off of your device.  The permissions are all required to support the features of the app, most of which were requested by users to begin with.  The app is completely open source so you can browse the code to confirm that there is no misuse of your data. Another way to confirm is to look at data usage in Android settings.

# Your location #
## Coarse (network-based) location ##
Access coarse location sources such as the cellular network database to determine an approximate device location, where available. Malicious applications can use this to determine approximately where you are.
  * This is needed during the location capture when disconnecting.  It is only used for the timeout period that you configure in A2DP Volume preferences.

## Fine (GPS) location ##
Access fine location sources such as the Global Positioning System on the device, where available. Malicious applications can use this to determine where you are, and may consume additional battery power.
  * This is needed during the location capture when disconnecting.  It is only used for the timeout period that you configure in A2DP Volume preferences.

# Your messages #
## Receive SMS ##
Allows application to receive and process SMS messages. Malicious applications may monitor your messages or delete them without showing them to you.
  * This is used for the text message (SMS) reader feature.  It responds to the text message being recieved and then send the message to the Text-To-Speech engine.  The messages are not modified, stored, or manipulated in any way.

# Network communication #
## Create Bluetooth connections ##
Allows an application to view configuration of the local Bluetooth device, and to make and accept connections with paired devices.
  * This is needed for the automatic Bluetooth connect feature, and to listen for Bluetooth device connections.  It is also used to read details about Bluetooth devices such as the MAC address, which is used to uniquely identify each device for storage and retrieval of the settings you select in A2DP Volume.

# Storage #
## Modify/delete USB storage contents modify/delete SD card contents ##
Allows an application to write to the USB storage. Allows an application to write to the SD card.
  * This is used to store the location data on disconnect, and for exporting and importing the database and location information wheh you use the Manage Data screen in A2DP Volume.

# System tools #
## Change Wi-Fi state ##
Allows an application to connect to and disconnect from Wi-Fi access points, and to make changes to configured Wi-Fi networks.
  * This is used to toggle the WIFI state based on your device specific configurations in A2DP Volume.

## Bluetooth Administration ##
Allows an application to configure the local Bluetooth device, and to discover and pair with remote devices.
  * This is needed for the automatic Bluetooth connect feature, and to listen for Bluetooth device connections.  It is also used to read details about Bluetooth devices such as the MAC address, which is used to uniquely identify each device for storage and retrieval of the settings you select in A2DP Volume.

# Your location #
## Access extra location provider commands ##
Access extra location provider commands. Malicious applications could use this to interfere with the operation of the GPS or other location sources.
  * This is used to populate the device specific location data that is stored on device disconnect.

# Network communication #
## View Wi-Fi state ##
Allows an application to view the information about the state of Wi-Fi.
  * This is used to toggle the WIFI state based on your device specific configurations in A2DP Volume.

# System tools #
## Kill background processes ##
Allows an application to kill background processes of other applications, even if memory isn't low.
  * This is used to attempt to stop a process that was started when a device connected.  You configure an app to launch (if desired).  It will try to stop it on disconnect.  In reality since Froyo (Android 2.2) this generally does not work anyway.

## Automatically start at boot ##
Allows an application to have itself started as soon as the system has finished booting. This can make it take longer to start the device and allow the application to slow down the overall device by always running.
  * This is used for the automatic start at boot feature, if enabled.

# Your personal information #
## read contact data ##
Allows an application to read all of the contact (address) data stored on your device. Malicious applications can use this to send your data to other people.
  * This is used for the SMS reading feature.  When you receive an SMS and you have the read SMS feature enabled in A2DP Volume it will look up the name of the sender using the phone number they texted you with.  Added in version 2.3.10.

# Phone calls #
## read phone state and identity ##
Allows the application to access the phone features of the device. An application with this permission can determine the phone number and serial number of this phone, whether a call is active, the number that call is connected to and the like.
  * This is used to tell if you are currently in a phone call.  If a phone call is active, text message reading is suspended until the call has ended.

# Default #
## press keys and control buttons ##
Allows the app to deliver its own input events (key presses, etc.) to other apps. Malicious apps may use this to take over the tablet. Allows the app to deliver its own input events (key presses, etc.) to other apps. Malicious apps may use this to take over the phone.
  * This is used to send commands to stop the media from playing on disconnect.  The media must be stopped in order to stop the app.  This was a feature requested by many.

# Other #

## Google Talk ##
Several permissions were needed to read Google Talk messages since version 2.9.0 when that feature was implemented.

## SMS ##
There are some permissions needed for reading SMS (text) messages for the integration of the SMS reader feature.

## Accessibility Service ##
This is used in 2.10.0 and up for setting up the accessibility service to read notifications out while a device is connected.

---

If you have any concerns about these permissions you can always browse the actual code and see how they are used.  This project is free, open source, and it has no ads.  Open source means that all the programming is open to view by anyone anytime right here on this website.