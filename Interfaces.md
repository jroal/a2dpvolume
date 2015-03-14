#Explanation of application interfaces available to other apps.

# Introduction #

A2DP Volume has some intent broadcasters and receivers that can by used by other apps.

## Text message TTS receiver interface ##
> service.java has an intent receiver as follows:
```
IntentFilter messageFilter = new IntentFilter("a2dp.vol.service.MESSAGE"); 
```
> This intent receiver is registered when reading text messages is enabled and TTS initialized properly and a device that is configured to read text messages is connected.  It is deregistered when the device disconnects or the service is stopped.

> Here is a snippet from a test app that sends a message to be read by A2DP Volume:
```
				// this was just created to test the intent and processing in A2DP Volume
				final String IRun = "a2dp.vol.service.MESSAGE";
				Intent i = new Intent();
				i.setAction(IRun);
				i.putExtra("message", "Test string");
				a2dp.vol.chat.ContactListActivity.this.sendBroadcast(i);
```

> It is recommended that you trim the string before sending.  Here is an example of that:

```
private void sendText(String mText){
		
		final String str = mText.trim();
		
		final String IRun = "a2dp.vol.service.MESSAGE";
		Intent i = new Intent();
		i.setAction(IRun);
		i.putExtra("message", str);
		this.sendBroadcast(i);
	}
```

> Also, make sure you send everything needed for the message to have proper context.  For instance, you should send the service name, the sender name, the subject, and the text as appropriate.  Also included any necessary pauses between them.  commas create a short pause.

## Device connected/disconnected broadcasts ##
When A2DP Volume sees a device connect, it broadcasts and intent as follows:

```
 String Ireload = "a2dp.Vol.main.RELOAD_LIST";
                Intent itent = new Intent();
                itent.setAction(Ireload);
                itent.putExtra("connect", bt2.getMac());
                application.sendBroadcast(itent);
```

The `bt2.getMac()` is the MAC address of the Bluetooth device that connected.  Virtual devices (power, headset, audio jack) and single digit numbers like 1, 2, and 3.

The same is true for disconnecting:
```
final String Ireload = "a2dp.Vol.main.RELOAD_LIST";
                Intent itent = new Intent();
                itent.setAction(Ireload);
                itent.putExtra("disconnect", bt2.getMac());
                application.sendBroadcast(itent);
```

Here is sends disconnect instead of connect in the extras.  Again it also sends the device MAC address.