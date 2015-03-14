#Upgrading from "A2DP Volume A2.1 version" to the main application

Prior to version 1.3.2, this program was written only for Android 2.2.  A special version was created for Android 2.1 users.  If you originally installed the version for Android 2.1 and your phone has now been updated to Android 2.2 you will need to uninstall that before installing the main version from here or Android Market.  The main version is called "A2DP Volume" on Android Market.  The special version for Android 2.1 is called "A2DP Volume A2.1 version" and is the only version you can see on the Android Market if you have an Android 2.1 device.  Follow the steps below to complete the upgrade and save your device data, if desired.

  * Open A2DP Volume and select "Manage Data" from the menu.
  * Select "Export Database to SD Card".  Make sure you have an SD card installed and it is ready to be used by the device (not mounted on PC).  This will export the database to your SD card in a directory called "BluetoothVol".
  * From the Android home screen click the menu key, then settings, then "applications".
  * Find "A2DP Volume A2.1 version" on the list of programs and select it.
  * Click "uninstall".  This will completely remove the program from your phone.
  * Find "A2DP Volume" in Android Market on your Android 2.2 or higher device.
  * Download and install it.
  * Open the new application and select menu and "Manage Data".  Select "Export data to SD card".  This is used to create the new folder called A2DPVol.
  * Use a file manager such as ES File Explorer to copy the btdevices.db from the old BluetoothVol folder to the new A2DPVol folder on the SD Card.  Overwrite the existing btdevices.db file.
  * Open the new A2DP Volume application and select menu and "Manage Data".
  * Select "Import Database from SD Card".  This will replace the database file with the one from the SD card.

You don't need to save your device data if you don't mind starting all over.  However, you must uninstall the A2.1 version prior to installing the new version.  Failure to do so would leave both versions working on your device in parallel.