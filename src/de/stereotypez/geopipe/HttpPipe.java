package de.stereotypez.geopipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HttpPipe 
{
	HttpClient client;
	
	public HttpPipe()
	{
		client = new DefaultHttpClient();
	}
	
	public boolean addNetwork(String ssid, String bssid, double latitude, double longitude) throws ClientProtocolException, 
	                                                                                               IOException
	{
		// create a new post method against the geomena REST interface		
		HttpPost post = new HttpPost("http://geomena.org/ap/" + bssid.replace(":", ""));
		
		// add post variables  
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
		nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));  
		nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));  
		nameValuePairs.add(new BasicNameValuePair("essid", ssid));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		HttpResponse response = client.execute(post);			
		if (response != null & response.getStatusLine().getStatusCode() == 200)
		{ 
			//Log.e("", EntityUtils.toString(response.getEntity()));			
			
			// release resources
			response.getEntity().consumeContent();
			
			return true;
		}
		
		return false;
	}
}
