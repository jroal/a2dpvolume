package a2dp.Vol;

import android.bluetooth.BluetoothDevice;

/**
 * @author Jim Roal This is the class used to store and manipulate bluetooth
 *         devices
 */

public class btDevice {

	public String desc1; // Default device name as read via Bluetooth
	public String desc2; // Description as assigned by user
	public String mac; // mac address of the bluetooth device
	public String pname; // package name item to launch
	public boolean setV; // whether to adjust volume on this device or not
	public int defVol; // default volume to set to when connected. Normally this
	// is just max (15)
	public boolean getLoc; // this sets whether to capture location info for
	public String bdevice; // this is a bluetooth device to auto connect when
							// this device connects
	public boolean wifi; // whether to turn OFF wifi on connect or not this
							// device
	public boolean enableTTS; // enable the Text To Speech service for this
								// device?
	public String appaction; // app action string
	public String appdata; // app data string
	public String apptype; // app type string
	public boolean apprestart; // app restart flag
	public boolean appkill; // kill app on disconnect flag
	public boolean enablegps; // enable GPS while connected
	public boolean setpv; // whether to adjust phone volume or not
	public int phonev; // what to adjust phone volume to
	public int icon; // icon to show when connected
	public int smsdelay; // delay before reading SMS
	public int smsstream; // stream to use for TTS
	public int voldelay; // delay before setting volume
	public boolean volramp; // ramp volume
	public boolean autovol; // automatically store last used volume for this
							// device
	public boolean silent; // use silent mode while connected
	public boolean sleep; // use to enter sleep mode after app launch
	public boolean carmode; // use to enter car mode on connect

	/**
	 * @return the setpv
	 */
	public boolean isSetpv() {
		return setpv;
	}

	public long islSetpv() {
		if (isSetpv())
			return 1;
		else
			return 0;
	}

	/**
	 * @param setpv
	 *            the setpv to set
	 */
	public void setSetpv(boolean setpv) {
		this.setpv = setpv;
	}

	public void setSetpv(int sV) {
		if (sV > 0)
			this.setpv = true;
		else
			this.setpv = false;
	}

	/**
	 * @return the phonev
	 */
	public int getPhonev() {
		return phonev;
	}

	/**
	 * @param phonev
	 *            the phonev to set
	 */
	public void setPhonev(int phonev) {
		this.phonev = phonev;
	}

	/**
	 * @return the pname
	 */
	public String getPname() {
		return pname;
	}

	/**
	 * @param pname
	 *            the pname to set
	 */
	public void setPname(String pname) {
		this.pname = pname;
	}

	/**
	 * @return the appaction
	 */
	public String getAppaction() {
		return appaction;
	}

	/**
	 * @param appaction
	 *            the appaction to set
	 */
	public void setAppaction(String appaction) {
		this.appaction = appaction;
	}

	/**
	 * @return the appdata
	 */
	public String getAppdata() {
		return appdata;
	}

	/**
	 * @param appdata
	 *            the appdata to set
	 */
	public void setAppdata(String appdata) {
		this.appdata = appdata;
	}

	/**
	 * @return the apptype
	 */
	public String getApptype() {
		return apptype;
	}

	/**
	 * @param apptype
	 *            the apptype to set
	 */
	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	/**
	 * @return the apprestart
	 */
	public boolean isApprestart() {
		return apprestart;
	}

	/**
	 * @param apprestart
	 *            the apprestart to set
	 */
	public void setApprestart(boolean apprestart) {
		this.apprestart = apprestart;
	}

	/**
	 * @return the apprestart
	 */
	public long lApprestart() {
		if (apprestart)
			return 1;
		else
			return 0;
	}

	/**
	 * @param apprestart
	 *            the apprestart to set
	 */
	public void setApprestart(int apprestart) {
		if (apprestart > 0)
			this.apprestart = true;
		else
			this.apprestart = false;
	}

	/**
	 * @return the appkill
	 */
	public boolean isAppkill() {
		return appkill;
	}

	/**
	 * @param appkill
	 *            the appkill to set
	 */
	public void setAppkill(boolean appkill) {
		this.appkill = appkill;
	}

	/**
	 * @return the appkill
	 */
	public long lAppkill() {
		if (appkill)
			return 1;
		else
			return 0;
	}

	/**
	 * @param appkill
	 *            the appkill to set
	 */
	public void setAppkill(int appkill) {
		if (appkill > 0)
			this.appkill = true;
		else
			this.appkill = false;
	}

	public long lenablegps() {
		if (enablegps)
			return 1;
		else
			return 0;
	}

	/**
	 * @return the enablegps
	 */
	public boolean isEnablegps() {
		return enablegps;
	}

	/**
	 * @param enablegps
	 *            the enablegps to set
	 */
	public void setEnablegps(boolean enablegps) {
		this.enablegps = enablegps;
	}

	/**
	 * @param enablegps
	 *            the enablegps to set
	 */
	public void setEnablegps(int enablegps1) {
		if (enablegps1 > 0)
			this.enablegps = true;
		else
			this.enablegps = false;
	}

	/**
	 * @return the enableTTS
	 */
	public boolean isEnableTTS() {
		return enableTTS;
	}

	public long islEnableTTS() {
		if (enableTTS)
			return 1;
		else
			return 0;
	}

	/**
	 * @param enableTTS
	 *            the enableTTS to set
	 */
	public void setEnableTTS(boolean enableTTS) {
		this.enableTTS = enableTTS;
	}

	public void setEnableTTS(int value) {
		if (value > 0)
			this.enableTTS = true;
		else
			this.enableTTS = false;
	}

	/**
	 * @return the bdevice
	 */
	public String getBdevice() {
		return bdevice;
	}

	/**
	 * @param bdevice
	 *            the bdevice to set
	 */
	public void setBdevice(String bdevice) {
		this.bdevice = bdevice;
	}

	/**
	 * @return the wifi
	 */
	public boolean isWifi() {
		return wifi;
	}

	public long islWifi() {
		if (wifi)
			return 1;
		else
			return 0;
	}

	/**
	 * @param wifi
	 *            the wifi to set
	 */
	public void setWifi(boolean wifi) {
		this.wifi = wifi;
	}

	/**
	 * @param wifi
	 *            the wifi to set
	 */
	public void setWifi(int swifi) {
		if (swifi > 0)
			this.wifi = true;
		else
			this.wifi = false;
	}

	/**
	 * @return the getLoc. This is the flag used to determine if location should
	 *         be stored for this device
	 */
	public boolean isGetLoc() {
		return getLoc;
	}

	public long islGetLoc() {
		if (getLoc)
			return 1;
		else
			return 0;
	}

	/**
	 * @param getLoc
	 *            This is the flag used to determine if location should be
	 *            stored for this device the getLoc to set
	 */
	public void setGetLoc(boolean getLoc) {
		this.getLoc = getLoc;
	}

	public void setGetLoc(int g) {
		if (g >= 1)
			this.getLoc = true;
		else
			this.getLoc = false;
	}

	public btDevice() {
		// TODO Auto-generated constructor stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (desc2 == null)
			return desc1;
		else
			return desc2;
	}

	/**
	 * @return the desc1
	 */
	public String getDesc1() {
		return desc1;
	}

	/**
	 * @param desc1
	 *            the desc1 to set
	 */
	public void setDesc1(String desc1) {
		this.desc1 = desc1;
	}

	/**
	 * @return the desc2
	 */
	public String getDesc2() {
		return desc2;
	}

	/**
	 * @param desc2
	 *            the desc2 to set
	 */
	public void setDesc2(String desc2) {
		this.desc2 = FileNameCleaner.cleanFileName(desc2);
	}

	/**
	 * @return the mac address of the bluetooth device
	 */
	public String getMac() {
		return mac;
	}

	/**
	 * @param mac
	 *            the mac address to set
	 */
	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * @return the setV
	 */
	public boolean isSetV() {
		return setV;
	}

	public long islSetV() {
		if (isSetV())
			return 1;
		else
			return 0;
	}

	/**
	 * @param setV
	 *            true if we want the stream volume set on connect
	 */
	public void setSetV(boolean setV) {
		this.setV = setV;
	}

	/**
	 * @param sV
	 *            is an integer 0 = no, 1 = yes This function is used to set the
	 *            boolean setV variable using an integer
	 */
	public void setSetV(int sV) {
		if (sV > 0)
			this.setV = true;
		else
			this.setV = false;
	}

	/**
	 * @return the defVol
	 */
	public int getDefVol() {
		return defVol;
	}

	/**
	 * @param defVol
	 *            the defVol to set
	 */
	public void setDefVol(int defVol) {
		this.defVol = defVol;
	}

	/**
	 * @return the icon
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * @param icon
	 *            the icon to set
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * @return the smsdelay
	 */
	public int getSmsdelay() {
		return smsdelay;
	}

	/**
	 * @param smsdelay
	 *            the smsdelay to set
	 */
	public void setSmsdelay(int smsdelay) {
		this.smsdelay = smsdelay;
	}

	/**
	 * @return the smsstream
	 */
	public int getSmsstream() {
		return smsstream;
	}

	/**
	 * @param smsstream
	 *            the smsstream to set
	 */
	public void setSmsstream(int smsstream) {
		this.smsstream = smsstream;
	}

	/**
	 * @return the voldelay
	 */
	public int getVoldelay() {
		return voldelay;
	}

	/**
	 * @param voldelay
	 *            the voldelay to set
	 */
	public void setVoldelay(int voldelay) {
		this.voldelay = voldelay;
	}

	/**
	 * @return the volramp
	 */
	public boolean isVolramp() {
		return volramp;
	}

	/**
	 * @param volramp
	 *            the volramp to set
	 */
	public void setVolramp(boolean volramp) {
		this.volramp = volramp;
	}

	public long lVolramp() {
		if (isVolramp())
			return 1;
		else
			return 0;
	}

	public void setVolramp(int ramp) {
		if (ramp > 0)
			volramp = true;
		else
			volramp = false;

	}

	/**
	 * @return the autovol
	 */
	public boolean isAutovol() {
		return autovol;
	}

	/**
	 * @param autovol
	 *            the autovol to set
	 */
	public void setAutovol(boolean autovol) {
		this.autovol = autovol;
	}

	public void setAutovol(int autovol) {
		if (autovol > 0)
			this.autovol = true;
		else
			this.autovol = false;

	}

	public long lautovol() {
		if (autovol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return the silent
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * @param silent
	 *            the silent to set
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}
	
	public void setSilent(int silent) {
		if(silent > 0)
			this.silent = true;
		else
			this.silent = false;
	}
	
	public long lsilent() {
		if (silent)
			return 1;
		else
			return 0;
	}

	/**
	 * @param btd
	 *            is the bluetooth device
	 * @param name
	 *            is the friendly name typed in by the user to describe this
	 *            device
	 * @param vol
	 *            is the volume to set to when this device is connected This is
	 *            the main function to use to create a new btDevice object
	 */
	public void setBluetoothDevice(BluetoothDevice btd, String name, int vol) {
		this.desc1 = btd.getName();
		this.desc2 = FileNameCleaner.cleanFileName(name);
		this.mac = btd.getAddress();
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
		this.pname = "";
		this.bdevice = "";
		this.wifi = false;
		this.appaction = "";
		this.appdata = "";
		this.apptype = "";
		this.apprestart = false;
		this.appkill = true;
		this.enableTTS = false;
		this.phonev = 10;
		this.setpv = false;
		this.setIcon(R.drawable.car2);
		this.autovol = false;
		this.smsdelay = 6;
		this.volramp = false;
		this.voldelay = 6;
		this.silent = false;
		this.carmode = false;
		this.sleep = false;
	}

	/**
	 * @param s1
	 *            is the default device name
	 * @param s2
	 *            is the user entered friendly name of this device
	 * @param mac
	 *            is the mac address of this device
	 * @param vol
	 *            is the default volume when this device is connected This
	 *            function is really only used to create dummy devices for
	 *            testing
	 */
	public void setBluetoothDevice(String s1, String s2, String mac, int vol) {
		this.desc1 = s1;
		this.desc2 = FileNameCleaner.cleanFileName(s2);
		this.mac = mac;
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
		this.pname = "";
		this.bdevice = "";
		this.wifi = false;
		this.appaction = "";
		this.appdata = "";
		this.apptype = "";
		this.apprestart = false;
		this.appkill = true;
		this.enableTTS = false;
		this.phonev = 10;
		this.setpv = false;
		this.setIcon(R.drawable.car2);
		this.autovol = false;
		this.smsdelay = 6;
		this.volramp = false;
		this.voldelay = 6;
		this.silent = false;
		this.carmode = false;
		this.sleep = false;
	}

	public boolean hasIntent() {
		if (this.pname == null || this.appdata == null)
			return false;
		if (this.pname.length() < 3 && this.appdata.length() < 3)
			return false;
		if (this.pname.equalsIgnoreCase("Custom") && this.appdata.length() < 3)
			return false;
		return true;

	}

	/**
	 * @return the sleep
	 */
	public boolean isSleep() {
		return sleep;
	}

	public long lsleep() {
		if (sleep)
			return 1;
		else
			return 0;
	}
	/**
	 * @param sleep the sleep to set
	 */
	public void setSleep(boolean sleep) {
		this.sleep = sleep;
	}

	public void setSleep(int sleep1) {
		if(sleep1 > 0)
			this.sleep = true;
		else
			this.sleep = false;
	}
	/**
	 * @return the carmode
	 */
	public boolean isCarmode() {
		return carmode;
	}

	public long lcarmode() {
		if (carmode)
			return 1;
		else
			return 0;
	}
	/**
	 * @param carmode the carmode to set
	 */
	public void setCarmode(boolean carmode) {
		this.carmode = carmode;
	}

	public void setCarmode(int cm) {
		if(cm > 0)
			this.carmode = true;
		else
			this.carmode = false;
	}
}
