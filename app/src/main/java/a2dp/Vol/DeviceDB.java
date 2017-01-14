/* use this link for help
 * http://www.screaming-penguin.com/node/7742
 */

package a2dp.Vol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class DeviceDB {

	private static final String DATABASE_NAME = "btdevices.db";
	private static final int DATABASE_VERSION = 13;
	private static final String TABLE_NAME = "devices";
	private static Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into " + TABLE_NAME
			+ "(desc1, desc2, mac, maxv, setv, getl, pname, bdevice, wifi, appaction, appdata, apptype, apprestart, tts," +
					" setpv, phonev, appkill, enablegps, icon, smsdelay, smsstream, voldelay, volramp, autovol, silent, sleep, carmode) " +
					"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public DeviceDB(Context context) {
		DeviceDB.context = context;
		OpenHelper openHelper = new OpenHelper(DeviceDB.context);
		
			this.db = openHelper.getWritableDatabase();	
			
			this.insertStmt = this.db.compileStatement(INSERT);
		
	}

	/**
	 * @param bt
	 *            is the bluetooth btDevice device to update in the database
	 */
	public void update(btDevice bt) {
		ContentValues vals = new ContentValues();
		vals.put("desc2", bt.getDesc2());
		vals.put("maxv", (long) bt.getDefVol());
		vals.put("setv", bt.islSetV());
		vals.put("getl", bt.islGetLoc());
		vals.put("pname", bt.getPname());
		vals.put("bdevice", bt.getBdevice());
		vals.put("wifi", bt.islWifi());
		vals.put("appaction", bt.getAppaction());
		vals.put("appdata", bt.getAppdata());
		vals.put("apptype", bt.getApptype());
		vals.put("apprestart", bt.lApprestart());
		vals.put("tts", bt.islEnableTTS());
		vals.put("setpv", bt.islSetpv());
		vals.put("phonev", (long) bt.getPhonev());
		vals.put("appkill", bt.lAppkill());
		vals.put("enablegps", bt.lenablegps());
		vals.put("icon", (long) bt.getIcon());
		vals.put("smsdelay", (long) bt.getSmsdelay());
		vals.put("smsstream", (long) bt.getSmsstream());
		vals.put("voldelay", (long) bt.getVoldelay());
		vals.put("volramp", bt.lVolramp());
		vals.put("autovol", bt.lautovol());
		vals.put("silent", bt.lsilent());
		vals.put("sleep", bt.isSleep());
		vals.put("carmode", bt.isCarmode());
		this.db.update(TABLE_NAME, vals, "mac='" + bt.mac + "'", null);
		vals = null;
	}

	/**
	 * @param bt
	 *            is the bluetooth btDevice to remove from the database
	 */
	public void delete(btDevice bt) {
		this.db.delete(TABLE_NAME, "mac='" + bt.mac + "'", null);
	}

	/**
	 * @param btd
	 *            is the bluetooth btDevice to add to the database
	 * @return the row ID of the last row inserted, if this insert is
	 *         successful. -1 otherwise.
	 */
	public long insert(btDevice btd) {
		String temp1 = btd.desc1;
		long rtn;
		if(temp1 == null) temp1 = "Unknown Device";  // make sure string1 is not null
		this.insertStmt.bindString(1, temp1);
		String temp2 = btd.desc2;
		if(temp2 == null)temp2 = temp1; // make sure string2 is not null
		this.insertStmt.bindString(2, temp2);
		if(btd.mac == null)return -1; // if we have no mac address, bail out
		this.insertStmt.bindString(3, btd.mac);
		this.insertStmt.bindLong(4, (long) btd.getDefVol());
		this.insertStmt.bindLong(5, btd.islSetV());
		this.insertStmt.bindLong(6, btd.islGetLoc());
		this.insertStmt.bindString(7, btd.getPname());
		this.insertStmt.bindString(8, btd.getBdevice());
		this.insertStmt.bindLong(9, btd.islWifi());
		this.insertStmt.bindString(10, btd.getAppaction());
		this.insertStmt.bindString(11, btd.getAppdata());
		this.insertStmt.bindString(12, btd.getApptype());
		this.insertStmt.bindLong(13, btd.lApprestart());
		this.insertStmt.bindLong(14, btd.islEnableTTS());
		this.insertStmt.bindLong(15, btd.islSetpv());
		this.insertStmt.bindLong(16, (long) btd.getPhonev());
		this.insertStmt.bindLong(17, btd.lAppkill());
		this.insertStmt.bindLong(18, btd.lenablegps());
		this.insertStmt.bindLong(19, (long) btd.getIcon());
		this.insertStmt.bindLong(20, (long) btd.getSmsdelay());
		this.insertStmt.bindLong(21, (long) btd.getSmsstream());
		this.insertStmt.bindLong(22, (long) btd.getVoldelay());
		this.insertStmt.bindLong(23, btd.lVolramp());
		this.insertStmt.bindLong(24, btd.lautovol());
		this.insertStmt.bindLong(25, btd.lsilent());
		this.insertStmt.bindLong(26, btd.lsleep());
		this.insertStmt.bindLong(27, btd.lcarmode());
		try {
			rtn = this.insertStmt.executeInsert();
		} catch (Exception e) {
			rtn = 0;
			e.printStackTrace();
		}
		//this.insertStmt.close();
		return rtn;
	}

	/**
	 * @param imac
	 *            is the mac address of the bluetooth btDevice you want to
	 *            retrieve from the database
	 * @return the btDevice from the database
	 */
	public btDevice getBTD(String imac) {
		btDevice bt = new btDevice();
		Cursor cs = this.db.query(TABLE_NAME, null, "mac = ?",
				new String[] { imac }, null, null, null, null);
		try {
			
			if (cs.moveToFirst()) {
				bt.setDesc1(cs.getString(0));
				bt.setDesc2(cs.getString(1));
				bt.setMac(cs.getString(2));
				bt.setDefVol(cs.getInt(3));
				bt.setSetV(cs.getInt(4));
				bt.setGetLoc(cs.getInt(5));
				bt.setPname(cs.getString(6));
				bt.setBdevice(cs.getString(7));
				bt.setWifi(cs.getInt(8));
				bt.setAppaction(cs.getString(9));
				bt.setAppdata(cs.getString(10));
				bt.setApptype(cs.getString(11));
				bt.setApprestart(cs.getInt(12));
				bt.setEnableTTS(cs.getInt(13));
				bt.setSetpv(cs.getInt(14));
				bt.setPhonev(cs.getInt(15));
				bt.setAppkill(cs.getInt(16));
				bt.setEnablegps(cs.getInt(17));
				bt.setIcon(cs.getInt(18));
				bt.setSmsdelay(cs.getInt(19));
				bt.setSmsstream(cs.getInt(20));
				bt.setVoldelay(cs.getInt(21));
				bt.setVolramp(cs.getInt(22));
				bt.setAutovol(cs.getInt(23));
				bt.setSilent(cs.getInt(24));
				bt.setSleep(cs.getInt(25));
				bt.setCarmode(cs.getInt(26));
			}
		} catch (Exception e) {
			bt.mac = null;
			
			// e.printStackTrace();
		}
		if(cs != null && !cs.isClosed()) cs.close();
		return bt;
	}

	/**
	 * Removes the data table from the database that stores all the btDevices.
	 */
	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);	
	}

	public SQLiteDatabase getDb() {
		return this.db;
	}

	/**
	 * @return the number of rows in the btDevice data table as an integer
	 */
	public int getLength() {
		return selectAll().size();
	}

	/**
	 * @return a List of the database btDevice descriptions sorted by the user
	 *         entered description. The user entered description defaults to the
	 *         device name if blank.
	 */
	public List<String> selectAll() {
		List<String> list = new ArrayList<String>();
		if(!this.db.isOpen())return null;
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "desc1",
				"desc2" }, null, null, null, null, "desc2");
		try {
			if (cursor.moveToFirst()) {
				do {
					String t = cursor.getString(1);
					if (t.length() < 2)
						list.add(cursor.getString(0));
					else
						list.add(t);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Toast.makeText(context, "Database corrupt, delete and recreate database", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	/**
	 * @return a vector of btDevices for all the data in the btDevice table.
	 */
	public Vector<btDevice> selectAlldb() {
		Vector<btDevice> list = new Vector<btDevice>();
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "desc1",
				"desc2", "mac", "maxv", "setv", "getl", "pname" , "bdevice", "wifi", "appaction", "appdata", "apptype", 
				"apprestart", "tts", "setpv", "phonev" , "appkill" , "enablegps", "icon", "smsdelay", "smsstream", "voldelay", 
				"volramp", "autovol", "silent" , "sleep", "carmode"}, null, null, null,
				null, "desc2");
		if (cursor.moveToFirst()) {
			do {
				btDevice bt = new btDevice();
				bt.setDesc1(cursor.getString(0));
				bt.setDesc2(cursor.getString(1));
				bt.setMac(cursor.getString(2));
				bt.setSetV(cursor.getInt(4));
				bt.setDefVol(cursor.getInt(3));
				bt.setGetLoc(cursor.getInt(5));
				bt.setPname(cursor.getString(6));
				bt.setBdevice(cursor.getString(7));
				bt.setWifi(cursor.getInt(8));
				bt.setAppaction(cursor.getString(9));
				bt.setAppdata(cursor.getString(10));
				bt.setApptype(cursor.getString(11));
				bt.setApprestart(cursor.getInt(12));
				bt.setEnableTTS(cursor.getInt(13));
				bt.setSetpv(cursor.getInt(14));
				bt.setPhonev(cursor.getInt(15));
				bt.setAppkill(cursor.getInt(16));
				bt.setEnablegps(cursor.getInt(17));
				bt.setIcon(cursor.getInt(18));
				bt.setSmsdelay(cursor.getInt(19));
				bt.setSmsstream(cursor.getInt(20));
				bt.setVoldelay(cursor.getInt(21));
				bt.setVolramp(cursor.getInt(22));
				bt.setAutovol(cursor.getInt(23));
				bt.setSilent(cursor.getInt(24));
				bt.setSleep(cursor.getInt(25));
				bt.setCarmode(cursor.getInt(26));
				list.add(bt);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "
					+ TABLE_NAME
					+ "(desc1 TEXT, desc2 TEXT, mac TEXT PRIMARY KEY, maxv INTEGER, setv INTEGER DEFAULT 1, getl INTEGER DEFAULT 1, pname TEXT, " +
							"bdevice TEXT, wifi INTEGER DEFAULT 0, appaction TEXT, appdata TEXT, apptype TEXT, apprestart INTEGER DEFAULT 0, " +
							"tts INTEGER DEFAULT 0, setpv INTEGER DEFAULT 0, phonev INTEGER DEFAULT 10, appkill INTEGER DEFAULT 1, enablegps INTEGER DEFAULT 0" +
							", icon INTEGER, smsdelay DEFAULT 3, smsstream DEFAULT 1, voldelay DEFAULT 5, volramp DEFAULT 0, autovol DEFAULT 0, silent DEFAULT 0," +
							" sleep DEFAULT 0, carmode DEFAULT 0)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
			Toast.makeText(context, "Upgrading database....", Toast.LENGTH_LONG).show();
			if ((newVersion < 4 && oldVersion < 4) || (oldVersion > DATABASE_VERSION || newVersion > DATABASE_VERSION ) ) {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
				onCreate(db);
				Toast.makeText(context, "Database replaced", Toast.LENGTH_LONG).show();
				return;
			} 

			if(newVersion >= 5)
			{
								
				try {

					List<String> columns = GetColumns(db);
					db.execSQL("ALTER table " + TABLE_NAME + " RENAME TO 'temp_" + TABLE_NAME + "'");
					onCreate(db);
					columns.retainAll(GetColumns(db));
					String cols = join(columns);
					db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from temp_%s", TABLE_NAME, cols, cols, TABLE_NAME));
					db.execSQL("DROP table 'temp_" + TABLE_NAME + "'");
					Toast.makeText(context, "Database upgraded succesfully", Toast.LENGTH_LONG).show();
					return;
					
				} catch (SQLException e) {
					// if anything goes wrong, just start over
					e.printStackTrace();
					db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
					Toast.makeText(context, "Upgrade failed, replaced database", Toast.LENGTH_LONG).show();
					onCreate(db);
				}
				
			}

			
		}
		
		public static List<String> GetColumns(SQLiteDatabase db) {
		    List<String> ar = null;
		    Cursor c = null;
		    try {
		        c = db.rawQuery("select * from " + DeviceDB.TABLE_NAME + " limit 1", null);
		        if (c != null) {
		            ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
		        }
		    } catch (Exception e) {
		        Log.v(DeviceDB.TABLE_NAME, e.getMessage(), e);
		        e.printStackTrace();
		    } finally {
		        if (c != null)
		            c.close();
		    }
		    return ar;
		}

		public static String join(List<String> list) {
		    StringBuilder buf = new StringBuilder();
		    int num = list.size();
		    for (int i = 0; i < num; i++) {
		        if (i != 0)
		            buf.append(",");
		        buf.append(list.get(i));
		    }
		    return buf.toString();
		}

	}

}
