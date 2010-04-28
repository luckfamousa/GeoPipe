package de.stereotypez.geopipe;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import de.stereotypez.geopipe.provider.Ap;
import de.stereotypez.geopipe.provider.GeoProvider;

public class WifiScanReceiver extends BroadcastReceiver
{
	private WifiManager mainWifi;
	private LocationReceiver locReceiver;
	private ContentResolver cr;
	
	private boolean isScanning;
	
	/*
	 * This class would make a nice usecase for DI.
	 * Is Guice available for Android?
	 */
	public WifiScanReceiver(WifiManager mainWifi,
							LocationReceiver locReceiver,
			                ContentResolver cr)
	{
		this.mainWifi = mainWifi;
		this.locReceiver = locReceiver;
		this.cr = cr;
	}
	
	public void start()
	{ 
		isScanning = true;
		
		// enable wifi if necessary
		if (!mainWifi.isWifiEnabled())
        {
        	mainWifi.setWifiEnabled(true);
        }
		
		mainWifi.startScan();				
	}
	
	public void stop()
	{
		isScanning = false;
	}
	
	public boolean isScanning()
	{
		return isScanning;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{ 
		for (ScanResult sr : mainWifi.getScanResults())
		{					
			try
			{
				Location l = locReceiver.getLocation();
				
				// insert data into a contentprovider
				ContentValues cv = new ContentValues();					
				cv.put(Ap.SSID, sr.SSID);
				cv.put(Ap.BSSID, sr.BSSID);
				cv.put(Ap.LONGITUDE, l.getLongitude());
				cv.put(Ap.LATITUDE, l.getLatitude());
				cv.put(Ap.LEVEL, sr.level);
				cv.put(Ap.CREATED, Long.valueOf(System.currentTimeMillis()));					
				cr.insert(GeoProvider.CONTENT_URI, cv);			
			}
			catch (Exception e)
			{
				e.printStackTrace();				
				//logger.log(e.getMessage());
			}			
		}
		
		// if we're still in scanning mode rescan in N seconds
		if (isScanning())
		{			
			new Timer().schedule(new TimerTask()
			{
				@Override
				public void run() 
				{
					mainWifi.startScan();
				}
			}, Geopipe.WIFI_SCAN_DELAY * 1000);
		}
	}
}
