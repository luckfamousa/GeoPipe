package de.stereotypez.geopipe.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Ap implements BaseColumns 
{  
	/**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "created ASC";

    /**
     * The ssid of the AP
     * <P>Type: TEXT</P>
     */
    public static final String SSID = "ssid";
    
    /**
     * The bssid of the AP
     * <P>Type: TEXT</P>
     */
    public static final String BSSID = "bssid";
    
    /**
     * The longitude coordinate of the AP
     * <P>Type: REAL (double from Location.getLongitude())</P>
     */
    public static final String LONGITUDE = "longitude";
    
    /**
     * The latitude coordinate of the AP
     * <P>Type: REAL (double from Location.getLatitude())</P>
     */
    public static final String LATITUDE = "latitude";
    
    /**
     * The signal level of the AP
     * <P>Type: INTEGER (int from ScanResult.level)</P>
     */
    public static final String LEVEL = "level";
    
    /**
     * The timestamp for when the ap was created
     * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
     */
    public static final String CREATED = "created";

    // This class cannot be instantiated
	private Ap() 
	{ 
	}
}
