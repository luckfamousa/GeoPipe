package de.stereotypez.geopipe.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper 
{
	private static final String DATABASE_NAME = "geopipe.db";
    private static final int    DATABASE_VERSION = 1;
    public static final String APS_TABLE_NAME = "aps";
	
	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
    public void onCreate(SQLiteDatabase db) 
	{ 
        db.execSQL("CREATE TABLE " + APS_TABLE_NAME + " ("
                + Ap._ID + " INTEGER PRIMARY KEY,"
                + Ap.SSID + " TEXT,"
                + Ap.BSSID + " TEXT,"
                + Ap.LONGITUDE + " REAL,"
                + Ap.LATITUDE + " REAL,"
                + Ap.LEVEL + " INTEGER,"
                + Ap.CREATED + " INTEGER"
                + ");");
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{ 
		Log.w(getClass().getName(), "Upgrading database from version " + oldVersion + " to "
                                    + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + APS_TABLE_NAME);
        onCreate(db);
	}
}
