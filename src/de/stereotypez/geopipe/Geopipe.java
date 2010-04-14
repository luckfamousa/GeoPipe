package de.stereotypez.geopipe;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.stereotypez.geopipe.provider.Ap;
import de.stereotypez.geopipe.provider.GeoProvider;


public class Geopipe extends Activity 
{
	private WifiManager mainWifi;
	private LocationManager mainLoc;
	
	private WifiScanReceiver wifiScanReceiver;
	
	private Menu menu;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Uri allAPs = Uri.parse(GeoProvider.CONTENT_URI + "/all");
        Cursor c = managedQuery(allAPs, null, null, null, "_id desc");

        ListView aplist = (ListView)findViewById(R.id.aplist);        
        SimpleCursorAdapter sca = new SimpleCursorAdapter(this, 
        		                                          R.layout.aplist_item, 
        		                                          c, 
        		                                          new String[]{Ap.SSID}, 
        		                                          new int[]{R.id.apitem});
        //sca.runQueryOnBackgroundThread(null);
        aplist.setAdapter(sca);
      
        
        // get location manager
        mainLoc = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        // scan for wifi hotspots
        mainWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);        
        
        
        
        // this instance will detect and store new APs
        wifiScanReceiver = new WifiScanReceiver(mainWifi, 
        		                                new LocationReceiver(mainLoc), 
        		                                getContentResolver()); 
        
        // thus it needs to be informed of new APs in our area
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));		
        wifiScanReceiver.start();
    }
    
    public void startScanning()
    {
    	menu.getItem(0).setTitle("Stop Scanning");
    	wifiScanReceiver.start();
    }
    
    public void stopScanning()
    {
    	menu.getItem(0).setTitle("Start Scanning");
    	wifiScanReceiver.stop();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	this.menu = menu;
    	
        menu.add(0, 0, 0, "Stop Scanning");
        menu.add(0, 1, 0, "Clear");
        menu.add(0, 2, 0, "Post & Clear");
        menu.add(0, 3, 0, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) 
    {
    	if (item.getItemId() == 0)
    	{ 
    		if (wifiScanReceiver.isScanning())
    		{
    			stopScanning();    			
    		}
    		else
    		{
    			startScanning();
    		}
    	}
    	else if (item.getItemId() == 1)
    	{
    		// clear database 
            getContentResolver().delete(Uri.parse(GeoProvider.CONTENT_URI + "/all"), null, null);
    	}
    	else if (item.getItemId() == 2)
    	{
    		// stop scannning for data transmission
    		stopScanning();
    		
    		// transfer items to geomena 
            // this operation is going to be expensive 
    		// so we're running it outside the UI thread
    		Thread t = new Thread()
    		{
				@Override
				public void run() 
				{
					// TODO show progress http://developer.android.com/reference/android/widget/ProgressBar.html
					
					// query our local AP database
		    		Cursor c = getContentResolver().query(Uri.parse(GeoProvider.CONTENT_URI + "/all"), 
		    				                              null, null, null, "created ASC");
		    		if (c.moveToFirst())
		    		{
		    			HttpPipe http = new HttpPipe();
		    			
		    			do
		        		{ 
		    				try
		    				{
			        			// post AP to geomena    				
			    				if (http.addNetwork(c.getString(c.getColumnIndex(Ap.SSID)), 
			    									c.getString(c.getColumnIndex(Ap.BSSID)), 
			    									c.getDouble(c.getColumnIndex(Ap.LATITUDE)), 
			    									c.getDouble(c.getColumnIndex(Ap.LONGITUDE))))
			    				{
			    					Log.i(getClass().getName(), c.getString(c.getColumnIndex(Ap.SSID)) + " - Success");
			    					
			    					// remove local AP record
			    					Uri uri = Uri.parse(GeoProvider.CONTENT_URI + "/_id/" + c.getLong(c.getColumnIndex(Ap._ID)));
			    		            getContentResolver().delete(uri, null, null);
			    		            
			    		            // really ugly!
			    		            ((ListView)findViewById(R.id.aplist)).postInvalidate();
			    				}
		    				}
		    				catch (Exception any)
		    				{
		    					Log.e(getClass().getName(), "Adding Network to Geomena failed.", any);
		    				}
		        		}
		    			while (c.moveToNext());
		    		}    		
				}
			};
    		t.start();						
    	}
    	else if (item.getItemId() == 3)
    	{
    		System.runFinalization();
    		System.exit(0);
    	}
    	
        return super.onMenuItemSelected(featureId, item);   
    }

}