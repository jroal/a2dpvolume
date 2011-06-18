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
	public String pname; // package name to auto launch
	public boolean setV; // whether to adjust volume on this device or not
	public int defVol; // default volume to set to when connected. Normally this
	// is just max (15)
	public boolean getLoc; // this sets whether to capture location info for

	// this device

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
		this.desc2 = desc2;
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
	 *            the setV to set
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
		this.desc2 = name;
		this.mac = btd.getAddress();
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
		this.pname = null;
	}

	/**
	 * @return the pname
	 */
	public String getPname() {
		return pname;
	}

	/**
	 * @param pname the pname to set
	 */
	public void setPname(String pname) {
		this.pname = pname;
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
		this.desc2 = s2;
		this.mac = mac;
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
		this.pname = null;
	}
}
