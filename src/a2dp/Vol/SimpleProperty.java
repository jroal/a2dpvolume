package a2dp.Vol;

public class SimpleProperty {
	public static final int TYPE_INT = 1;
	public static final int TYPE_BOOL = 2;
	public static final int TYPE_TEXT = 3;

	private String mKey;
	private int mType;
	private Object mValue;
	
	public SimpleProperty(String key, int val) {
		mKey = key;
		mType = TYPE_INT;
		mValue = new Integer(val);
	}
	public SimpleProperty(String key, boolean val) {
		mKey = key;
		mType = TYPE_BOOL;
		mValue = new Boolean(val);
	}
	public SimpleProperty(String key, String val) {
		mKey = key;
		mType = TYPE_TEXT;
		mValue = new String(val);
	}
	public SimpleProperty(String key, int type, int nullflag) {
		mKey = key;
		mType = type;
		mValue = null;
	}
	public SimpleProperty(SimpleProperty sp) {
		mKey = sp.getKey();
		mType = sp.getType();
		switch(mType) {
		case TYPE_INT:
			set(sp.getInt());
			break;
		case TYPE_BOOL:
			set(sp.getBool());
			break;
		case TYPE_TEXT:
			set(sp.getString());
			break;
		}
	}
	
	public SimpleProperty(String dataFileInput) {
		String data = dataFileInput.trim();
		int step = data.indexOf(":");
		mKey = data.substring(0, step);
		String sub = data.substring(step+1);
		step = sub.indexOf(":");
		String type = sub.substring(0, step);
		String val = sub.substring(step+1);
		
		if (type.equals("int")) {
			mType = TYPE_INT;
			mValue = new Integer(Integer.parseInt(val));
		} else if (type.equals("bool")) {
			mType = TYPE_BOOL;
			mValue = new Boolean(val.equals("1"));
		} else {
			mType = TYPE_TEXT;
			mValue = val;
		}
	}
	
	
	public void set(int val) {
		if (mType == TYPE_INT) {
			mValue = new Integer(val);
		} else {
			try {
				throw new Exception("Tried to set SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void set(boolean val) {
		if (mType == TYPE_BOOL) {
			mValue = new Boolean(val);
		} else {
			try {
				throw new Exception("Tried to set SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void set(String val) {
		if (mType == TYPE_TEXT) {
			mValue = new String(val);
		} else {
			try {
				throw new Exception("Tried to set SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public int getInt() {
		if (mType != TYPE_INT) {
			try {
				throw new Exception("Tried to get SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ((Integer)mValue).intValue();
	}
	public boolean getBool() {
		if (mType != TYPE_BOOL) {
			try {
				throw new Exception("Tried to get SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ((Boolean)mValue).booleanValue();
	}
	public String getString() {
		if (mType != TYPE_TEXT) {
			try {
				throw new Exception("Tried to get SimpleProperty of wrong type. Key: " + mKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mValue.toString();
	}
	public String getKey() {
		return mKey;
	}
	public int getType() {
		return mType;
	}
	
	public String toString() {
		String str = mKey + ":";
		switch (mType) {
		case TYPE_INT:
			str += "int:" + getInt();
			break;
		case TYPE_BOOL:
			str += "bool:" + (getBool() ? "1" : "0");
			break;
		case TYPE_TEXT:
			str += "text:" + getString();
			break;
		}
		return str;
	}
}
