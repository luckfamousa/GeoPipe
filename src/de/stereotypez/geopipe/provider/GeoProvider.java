package de.stereotypez.geopipe.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class GeoProvider extends ContentProvider 
{
	public static final String AUTHORITY = "de.stereotypez.geopipe.provider.geoprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private static final int _ID   = 1;
	private static final int SSID  = 2;
    private static final int BSSID = 3;
    private static final int ALL   = 4;
    
    private static HashMap<String, String> sNotesProjectionMap;
    private static final UriMatcher sUriMatcher;
    static 
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "_id/#", _ID);
        sUriMatcher.addURI(AUTHORITY, "ssid/*", SSID);
        sUriMatcher.addURI(AUTHORITY, "bssid/*", BSSID);
        sUriMatcher.addURI(AUTHORITY, "all", ALL);
        
        sNotesProjectionMap = new HashMap<String, String>();        
        sNotesProjectionMap.put(Ap._ID, Ap._ID);
        sNotesProjectionMap.put(Ap.SSID, Ap.SSID);
        sNotesProjectionMap.put(Ap.BSSID, Ap.BSSID);
        sNotesProjectionMap.put(Ap.LONGITUDE, Ap.LONGITUDE);
        sNotesProjectionMap.put(Ap.LATITUDE, Ap.LATITUDE);
        sNotesProjectionMap.put(Ap.LEVEL, Ap.LEVEL);
        sNotesProjectionMap.put(Ap.CREATED, Ap.CREATED);
    }

    private DatabaseHelper mOpenHelper;

	public GeoProvider() 
	{ 
	}

	@Override
	public boolean onCreate() 
	{
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) 
	{
		String sqlWhere = !TextUtils.isEmpty(where) ? " AND (" + where + ')' : "";
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {  
	        case _ID:
	            count = db.delete(DatabaseHelper.APS_TABLE_NAME, 
	            		          Ap._ID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
	            		          whereArgs);
	            break;
        
	        case SSID:
	        	count = db.delete(DatabaseHelper.APS_TABLE_NAME, 
	            		          Ap.SSID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
	            		          whereArgs);
	            break;
	
	        case BSSID:
	            count = db.delete(DatabaseHelper.APS_TABLE_NAME, 
	            		          Ap.BSSID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
	            		          whereArgs);
	            break;
	
	        case ALL:
	            count = db.delete(DatabaseHelper.APS_TABLE_NAME, 
	            		          sqlWhere, 
	            		          whereArgs);
	            break;     
	            
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) 
	{
		switch (sUriMatcher.match(uri)) 
        {
	        case _ID: 
	        	return ContentResolver.CURSOR_ITEM_BASE_TYPE;
	        case SSID:
	        case BSSID:	
	        case ALL:
	        	return ContentResolver.CURSOR_DIR_BASE_TYPE;
        }
		
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
    public Uri insert(Uri uri, ContentValues initialValues) 
	{
        // TODO Validate the requested uri
		/*
        if (sUriMatcher.match(uri) != ...) 
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
		*/

        ContentValues values = initialValues != null 
                             ? new ContentValues(initialValues) : new ContentValues();
        
        // TODO make sure that the fields are all set                          
        if (!values.containsKey(Ap.CREATED)) 
        {
            values.put(Ap.CREATED, Long.valueOf(System.currentTimeMillis()));
        }
        
        // derive primary key from bssid
        if (!values.containsKey(Ap._ID)) 
        {
	        Long _id = Long.parseLong(values.getAsString(Ap.BSSID).replace(":",""), 16);
	        values.put(Ap._ID, _id);        
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(DatabaseHelper.APS_TABLE_NAME, Ap.SSID, values);
        if (rowId > -1) 
        {
        	// notify item query (useful? item got just created)
            Uri apUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(apUri, null);
            
            // notify ALL queries
            Uri allAPs = Uri.withAppendedPath(CONTENT_URI, "all");
            getContext().getContentResolver().notifyChange(allAPs, null);
            
            return apUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }


	@Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseHelper.APS_TABLE_NAME);
        qb.setProjectionMap(sNotesProjectionMap);
        
        switch (sUriMatcher.match(uri)) 
        {
	        case _ID:
	        	qb.appendWhere(Ap._ID + "=" + "'" + uri.getPathSegments().get(1) + "'");	            
	            break;
	    
	        case SSID:
	        	qb.appendWhere(Ap.SSID + "=" + "'" + uri.getPathSegments().get(1) + "'");	            
	            break;
	
	        case BSSID:
	        	qb.appendWhere(Ap.BSSID + "=" + "'" + uri.getPathSegments().get(1) + "'");	            
	            break;
	            
	        case ALL: break;    
	            
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // if no sort order is specified use the default
        String orderBy = TextUtils.isEmpty(sortOrder)
                       ? Ap.DEFAULT_SORT_ORDER : sortOrder;

        // get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


	@Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
	{
		String sqlWhere = !TextUtils.isEmpty(where) ? " AND (" + where + ')' : "";
                   
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {
	        case _ID:
	        	count = db.update(DatabaseHelper.APS_TABLE_NAME, 
			      		          values, 
			      		          Ap._ID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
			      		          whereArgs);	            
	            break;
	    
	        case SSID:
	        	count = db.update(DatabaseHelper.APS_TABLE_NAME, 
			      		          values, 
			      		          Ap.SSID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
			      		          whereArgs);	            
	            break;
	
	        case BSSID:
	            count = db.update(DatabaseHelper.APS_TABLE_NAME, 
	            		          values, 
	            		          Ap.BSSID + "=" + "'" + uri.getPathSegments().get(1) + "'" + sqlWhere, 
	            		          whereArgs);
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
