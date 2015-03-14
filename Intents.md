#How to create custom Intents for the auto-launch feature.

# What is an Intent? #

Click this link:
http://developer.android.com/guide/topics/intents/intents-filters.html
for an overview of the Android Intent.  It includes some examples and an explanation of the Action, Data, and Type fields.

A2DP Volume 2.0.9 and up includes a custom Intent builder.  This can be used to launch an Intent when a device connects.  To use this feature, click on a device in the device list on the main screen of A2DP Volume.  A dialog box will pop up.  Click the center Edit button in that dialog. Click the start app on connect button. A list of activity types will be shown.  Select Custom Intent.  The custom Intent builder screen will appear.

You must create an intent by filling in the action, data, and/or type correctly.  You must test the intent before clicking the OK button.  At least one of the fields must be filled in to test.

There is no validation so it is up to you to create and test your intent properly before storing it.  This feature is really for the extra nerdy folks who understand Android programming.

Note:  This feature was leveraged from App Alarm Pro.  You can see documentation for App Alarm here: http://episode6.wordpress.com/2010/03/27/appalarm/


# Examples #

  * Type in a website address, complete with the protocol, in the Data field.  Leave the other fields blank.  Click the Test button. For instance, http://www.google.com will launch that website in the default browser.

  * I found this on the web: http://zuggabecka.wordpress.com/a1-challenge-for-apps-2010/radio-fm4-%C2%A0appalarm/

  * Here is another app that does something similar: http://ig88.cwfk.net/?page_id=73