package com.py.player.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbTool {

	private final String TAG = "DbTool";

	private static DbTool mDbTool;
	
	private DbOpenHelper helper = null;

	public static final String table_video = "video";

	private DbTool() {
	}

	public synchronized static DbTool getInstance(Context context) {
		if (mDbTool == null)
			mDbTool = new DbTool();
		mDbTool.helper = new DbOpenHelper(context);
		return mDbTool;
	}


	public boolean insert(String table, ContentValues values) {
		// TODO Auto-generated method stub
		boolean flag = false;
		SQLiteDatabase database = null;
		long id = -1;
		try {
			database = helper.getWritableDatabase();
			id = database.insert(table, null, values);
			flag = (id != -1 ? true : false);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public int delete(String table, String whereClause, String[] whereArgs) {
		SQLiteDatabase database = null;
		int count = 0;
		try {
			database = helper.getWritableDatabase();
			count = database.delete(table, whereClause, whereArgs);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return count;
	}


	public boolean isExist(String table, String selection,
			String[] selectionArgs) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.query(true, table, null, selection,
					selectionArgs, null, null, null, null);
			flag = cursor.moveToNext() == false ? false : true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}


	public boolean update(String table, ContentValues values,
			String whereClause, String[] whereArgs) {
		boolean flag = false;
		SQLiteDatabase database = null;
		int count = 0;
		try {
			database = helper.getWritableDatabase();
			count = database.update(table, values, whereClause, whereArgs);
			flag = (count > 0 ? true : false);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public Map<String, String> list(String table, String selection,
			String[] selectionArgs) {
		
		SQLiteDatabase database = null;
		Cursor cursor = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			database = helper.getReadableDatabase();
			cursor = database.query(true, table, null, selection,
					selectionArgs, null, null, null, null);
			int cols_len = cursor.getColumnCount();
			while (cursor.moveToNext()) {
				for (int i = 0; i < cols_len; i++) {
					String cols_name = cursor.getColumnName(i);
					String cols_value = cursor.getString(cursor
							.getColumnIndex(cols_name));
					if (cols_value == null) {
						cols_value = "";
					}
					map.put(cols_name, cols_value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return map;
	}


	public List<Map<String, String>> listAll(String table, String selection,
			String[] selectionArgs, String orderBy) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = helper.getReadableDatabase();
			cursor = database.query(false, table, null, selection,
					selectionArgs, null, null, orderBy, null);
			int cols_len = cursor.getColumnCount();
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = cursor.getColumnName(i);
					String cols_value = cursor.getString(cursor
							.getColumnIndex(cols_name));
					if (cols_value == null) {
						cols_value = "";
					}
					map.put(cols_name, cols_value);
				}
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return list;
	}
}
