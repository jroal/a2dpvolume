package a2dp.Vol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SimplePropertyCollection {
	private HashMap<String, SimpleProperty> mProperties;
	
	public SimplePropertyCollection(SimpleProperty[] defaults) {
		mProperties = new HashMap<String, SimpleProperty>(defaults.length);
		for (int i = 0; i<defaults.length; i++) {
			mProperties.put(defaults[i].getKey(), new SimpleProperty(defaults[i]));
		}
	}
	
	public SimplePropertyCollection(SimpleProperty[] defaults, Cursor cur) {
		mProperties = new HashMap<String, SimpleProperty>(defaults.length);
		SimpleProperty sp = null;
		String key = null;
		for (int i = 0; i<defaults.length; i++) {
			sp = defaults[i];
			key = sp.getKey();
			
			switch(sp.getType()) {
			case SimpleProperty.TYPE_INT:
				mProperties.put(key, new SimpleProperty(key, cur.getInt(cur.getColumnIndexOrThrow(key))));
				break;
			case SimpleProperty.TYPE_BOOL:
				mProperties.put(key, new SimpleProperty(key, (cur.getInt(cur.getColumnIndexOrThrow(key)))==1));
				break;
			case SimpleProperty.TYPE_TEXT:
				mProperties.put(key, new SimpleProperty(key, cur.getString(cur.getColumnIndexOrThrow(key))));
				break;
			}
		}
	}
	
	public SimplePropertyCollection(SimpleProperty[] defaults, BufferedReader reader, boolean skipId, String idKey) throws Exception {
		String nextLine = reader.readLine();
		mProperties = new HashMap<String, SimpleProperty>();
		
		
		while(!nextLine.equals("enditem")) {
			nextLine = nextLine.trim();
			if (!nextLine.equals("") && !nextLine.equals("startitem")) {
				SimpleProperty sp = new SimpleProperty(nextLine);
				if ((!skipId) || (!sp.getKey().equals(idKey))) {
					mProperties.put(sp.getKey(), sp);
				}
			}
			nextLine = reader.readLine();
		}
		
		for (int i = 0; i < defaults.length; i++) {
			if (!mProperties.containsKey(defaults[i].getKey())) {
				mProperties.put(defaults[i].getKey(), new SimpleProperty(defaults[i]));
				Log.d("AppAlarm", "Had to add key: " + defaults[i].toString());
			}
		}
	}
	
	//Returns true if write was successful
	public boolean writeToFileWriter(BufferedWriter bw) {
		try {
			bw.write("startitem\n");
			
			Object[] props = mProperties.values().toArray();
			SimpleProperty sp = null;
			
			for (int i = 0; i<props.length; i++) {
				sp = (SimpleProperty)props[i];
				bw.write(sp.toString() + "\n");
			}
			
			bw.write("enditem\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public String toString() {
		String rtr = "startitem\n";
		
		Object[] props = mProperties.values().toArray();
		SimpleProperty sp = null;
		
		for (int i = 0; i<props.length; i++) {
			sp = (SimpleProperty)props[i];
			rtr += sp.toString() + "\n";
		}
		
		rtr += "enditem\n";
		return rtr;
	}
	
	public void saveItem(SQLiteDatabase db, String tableName, String rowId) {
		ContentValues cv = new ContentValues();
		Object[] props = mProperties.values().toArray();
		int id = 0;
		String key = null;
		int type = 0;
		
		for (int i = 0; i<props.length; i++) {
			SimpleProperty prop = (SimpleProperty) props[i];
			key = prop.getKey();
			type = prop.getType();
			if (key.equals(rowId)) {
				id = prop.getInt();
			} else {
				switch(type) {
				case SimpleProperty.TYPE_INT:
					cv.put(key, prop.getInt());
					break;
				case SimpleProperty.TYPE_BOOL:
					cv.put(key, prop.getBool());
					break;
				case SimpleProperty.TYPE_TEXT:
					cv.put(key, prop.getString());
					break;
				}
			}
		}
		
		
		if (id > 0) {
			db.update(tableName, cv, rowId + "=" + id, null);
		} else {
			if (mProperties.containsKey(rowId)) {
				set(rowId, (int)db.insert(tableName, null, cv));
			} else {
				SimpleProperty sp = new SimpleProperty(rowId, (int)db.insert(tableName, null, cv));
				mProperties.put(rowId, sp);
			}
		}
	}
	
	public void set(String key, int val) {
		mProperties.get(key).set(val);
	}
	public void set(String key, boolean val) {
		mProperties.get(key).set(val);
	}
	public void set(String key, String val) {
		mProperties.get(key).set(val);
	}
	public void set(SimpleProperty sp) {
		String key = sp.getKey();
		
		switch(sp.getType()) {
		case SimpleProperty.TYPE_INT:
			set(key, sp.getInt());
			break;
		case SimpleProperty.TYPE_BOOL:
			set(key, sp.getBool());
			break;
		case SimpleProperty.TYPE_TEXT:
			set(key, sp.getString());
			break;
		}
	}
	
	public boolean hasProperty(String key) {
		return mProperties.containsKey(key);
	}
	
	public int getInt(String key) {
		return mProperties.get(key).getInt();
	}
	public boolean getBool(String key) {
		return mProperties.get(key).getBool();
	}
	public String getString(String key) {
		return mProperties.get(key).getString();
	}
	
	public SimpleProperty get(String key) {
		return mProperties.get(key);
	}
	
	
	
	
	public static String[] getKeyArray(SimpleProperty[] props) {
		String[] keys = new String[props.length];
		for (int i = 0; i<props.length; i++) {
			keys[i] = props[i].getKey();
		}
		return keys;
	}
	
	
	public static String getCreateTableStatement(SimpleProperty[] props, String tableName) {
		String rtr = "CREATE TABLE " + tableName + " (";
		for (int i = 0; i<props.length; i++) {
			SimpleProperty p = props[i];
			rtr += (i > 0 ? ", " : "") + p.getKey();
			switch(p.getType()) {
			case SimpleProperty.TYPE_INT:
				rtr += " INTEGER" + (p.getKey().equals("_id") ? " PRIMARY KEY AUTOINCREMENT" : "");
				break;
			case SimpleProperty.TYPE_BOOL:
				rtr += " BOOLEAN";
				break;
			case SimpleProperty.TYPE_TEXT:
				rtr += " TEXT";
				break;
			}
		}
		rtr += ")";
		return rtr;
	}
}
