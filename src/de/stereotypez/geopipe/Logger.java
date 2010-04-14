package de.stereotypez.geopipe;

import android.widget.TextView;

public class Logger 
{
	private TextView log;
	
	public Logger(TextView log)
	{
		this.log = log;
	}
	
	public void log(String txt)
    {
    	log.append(txt + "\n");
    }
}
