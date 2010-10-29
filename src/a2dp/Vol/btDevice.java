package a2dp.Vol;

import android.bluetooth.BluetoothDevice;


public class btDevice  {
	
		public String desc1; // Default device name as read via Bluetooth
		public String desc2; // Description as assigned by user
		public String mac; // mac address of the bluetooth device
		public boolean setV; // whether to adjust volume on this device or not
		public int defVol; // default volume to set to when connected.  Normally this is just max (15)
		public boolean getLoc; // this sets whether to capture location info for this device
		
	/**
		 * @return the getLoc
		 */
		public boolean isGetLoc() {
			return getLoc;
		}
		
		public long islGetLoc(){
			if(getLoc) return 1;
			else return 0;
		}

		/**
		 * @param getLoc the getLoc to set
		 */
		public void setGetLoc(boolean getLoc) {
			this.getLoc = getLoc;
		}

		public void setGetLoc(int g){
			if(g>=1) this.getLoc = true;
			else this.getLoc = false;
		}
		
	public btDevice() {
		// TODO Auto-generated constructor stub

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(desc2 == null)
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
	 * @param desc1 the desc1 to set
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
	 * @param desc2 the desc2 to set
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
	 * @param mac the mac to set
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
		if(isSetV())
			return 1;
		else
			return 0;
	}
	
	/**
	 * @param setV the setV to set
	 */
	public void setSetV(boolean setV) {
		this.setV = setV;
	}
	
	public void setSetV(int sV) {
		if(sV > 0) this.setV = true;
		else this.setV = false;
	}

	/**
	 * @return the defVol
	 */
	public int getDefVol() {
		return defVol;
	}

	/**
	 * @param defVol the defVol to set
	 */
	public void setDefVol(int defVol) {
		this.defVol = defVol;
	}

	public void setBluetoothDevice(BluetoothDevice btd, String name, int vol){
		this.desc1 = btd.getName();
		this.desc2 = name;
		this.mac = btd.getAddress();
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
	}

	public void setBluetoothDevice(String s1, String s2, String mac, int vol){
		this.desc1 = s1;
		this.desc2 = s2;
		this.mac = mac;
		this.setV = true;
		this.defVol = vol;
		this.getLoc = true;
	}
}
