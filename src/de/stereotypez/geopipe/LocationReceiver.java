package de.stereotypez.geopipe;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class LocationReceiver implements LocationListener  
{
	private LocationManager mainLoc;
	private Location location;
	
	public LocationReceiver(LocationManager mainLoc)
	{
		this.mainLoc = mainLoc;
		
		// start periodically location retrieval
		mainLoc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
						               30 * 1000, /* TODO 30 seconds, make configurable */
						               50,        /* TODO 50 meters, make configurable */
						               this);
	}
	
	public Location getLocation()
	{
		// return last known location if we did not get notified, yet
		// perhaps we should throw an exception instead as "last-known" might be in Timbuctu 
		if (location == null)
		{
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_FINE);		
			return mainLoc.getLastKnownLocation(mainLoc.getBestProvider(c, true));
		}
		
		return location;
	}

	@Override
	public void onLocationChanged(Location location) 
	{
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		// TODO or should we should re-enable it?
		this.location = null;
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		// log?
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// log?
	}
}
