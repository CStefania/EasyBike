package com.example.easybike.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StorageDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "entry_list.db";
	private static final int DATABASE_VERSION = 11;

	// Tour table
	public static final String TABLE_TOUR = "TOUR";
	public static final String ID = "ID";
	public static final String NAME = "NAME";
	public static final String STATUS = "STATUS";
	public static final String TOT_M = "TOT_M";
	public static final String TOT_TIME = "TOT_TIME";
	public static final String N_STOPS = "N_STOPS";
	public static final String LEFT_TIME = "LEFT_TIME";
	public static final String LEFT_M = "LEFT_M";
	public static final String LEFT_STOPS = "LEFT_STOPS";
	public static final String START_TIME = "START_TIME";
	public static final String COMPLETION_TIME = "COMPLETION_TIME";

	// Stops table
	public static final String TABLE_STOP = "STOP";
	public static final String TOUR_ID = "TOUR_ID";
	public static final String ORDER_IN_TOUR = "ORDER_IN_TOUR";
	public static final String ADDRESS = "ADDRESS";
	public static final String GPS_COORD_LAT = "GPS_COORD_LAT";
	public static final String GPS_COORD_LNG = "GPS_COORD_LNG";
	public static final String COMPLETED = "COMPLETED";
	public static final String TIME_PREV_STOP = "TIME_PREV_STOP";
	public static final String DISTANCE_PREV_STOP = "DISTANCE_PREV_STOP";
	public static final String ALTITUTE_PREV_STOP = "ALTITUTE_PREV_STOP";


	// Database creation sql statement
	// Tour table create query
	private static final String DATABASE_CREATE_TOUR = 
			"create table " + TABLE_TOUR + "(" 
					+ ID + " integer primary key autoincrement, " 
					+ NAME + " text, "
					+ STATUS + " text not null, "
					+ TOT_M + " double not null, " 
					+ TOT_TIME + " long not null, "
					+ N_STOPS + " integer not null, "
					+ LEFT_M + " long not null, "
					+ LEFT_TIME + " long not null, "
					+ LEFT_STOPS + " integer not null, "
					+ START_TIME + " long, "
					+ COMPLETION_TIME + " long);";

	// Stops table create query
	private static final String DATABASE_CREATE_STOP =
			"create table " + TABLE_STOP + "("
					+ ID + " integer primary key not null, "
					+ TOUR_ID + " integer not null, "
					+ ORDER_IN_TOUR + " integer not null, "
					+ ADDRESS + " text not null, "
					+ GPS_COORD_LAT + " real not null, "
					+ GPS_COORD_LNG + " real not null, "
					+ COMPLETED + " integer not null, " 
					+ TIME_PREV_STOP + " long not null, "
					+ DISTANCE_PREV_STOP + " long not null, "
					+ ALTITUTE_PREV_STOP + " long not null, "
					+ "FOREIGN KEY(" + TOUR_ID + ") REFERENCES " + TABLE_TOUR + "(" + ID + "));";

	public StorageDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d(getClass().getName(), "creating database with query:\n" + DATABASE_CREATE_TOUR);
		database.execSQL(DATABASE_CREATE_TOUR);
		Log.d(getClass().getName(), "creating database with query:\n" + DATABASE_CREATE_STOP);
		database.execSQL(DATABASE_CREATE_STOP);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(StorageDBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOP);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOUR);
		onCreate(db);		
	}



}
