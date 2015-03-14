#This page describes how to get notifications from apps working in A2DP Volume.

# Introduction #

Version 2.10.0 and up of A2DP Volume implemented a new feature to read notifications while connected.  This new feature uses Android accessibility functionality to gain access to notifications from apps the user selects.  Below is the instructions to get this working.

# Details #

## Set up A2DP Volume and devices ##
After you install A2DP volume and get all your devices set up, make sure you enable "Enable reading notification messages" in the preferences menu of A2DP Volume.

![http://jimroal.com/A2DPScreens/preferences2b.png](http://jimroal.com/A2DPScreens/preferences2b.png)

Back out of the preferences so they take effect.  Now go to each device and check the "enable reading text messages?" checkbox.

![http://jimroal.com/A2DPScreens/EditDevice1b.png](http://jimroal.com/A2DPScreens/EditDevice1b.png)

Do this for each device that you want to have messages read while connected.

## Set up the apps for accessibility in A2DP Volume ##

Now click on the preferences button while in the A2DP Volume main screen.  Click on "apps for accessibility" in the menu.

![http://jimroal.com/A2DPScreens/appsforaccessibility.png](http://jimroal.com/A2DPScreens/appsforaccessibility.png)

You will see a list of all the apps installed on the device.  This includes system apps as well as downloaded apps and other packages.

![http://jimroal.com/A2DPScreens/accessibilityapps.png](http://jimroal.com/A2DPScreens/accessibilityapps.png)

Check the box next to the apps of your choice.  Be careful not to have silly thing happen such as checking A2DP Volume itself or car mode.

## Set up accessibility service in Android ##

Now go to Android settings.  Each manufacturer has a slightly different look but find "accessibility" settings.

![http://jimroal.com/A2DPScreens/accessibilitysettings1.png](http://jimroal.com/A2DPScreens/accessibilitysettings1.png)

Click on A2DP Volume in the services list.

![http://jimroal.com/A2DPScreens/accessibilitysettings2.png](http://jimroal.com/A2DPScreens/accessibilitysettings2.png)

Slide the switch at the top to ON.

![http://jimroal.com/A2DPScreens/accessibilitysettings3.png](http://jimroal.com/A2DPScreens/accessibilitysettings3.png)

# Caveats #

If you remove an app from the list in the apps for accessibility, you will need to go back to the Android accessibility settings and disable then re-enable A2DP Volume for the changes to take effect.