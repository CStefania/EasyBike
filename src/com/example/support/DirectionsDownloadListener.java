package com.example.support;

import com.example.easybike.model.Stop;
import com.google.android.gms.maps.model.PolylineOptions;

public interface DirectionsDownloadListener {
	
	public void onDirectionsDownloadSuccess (Stop origin, Stop destination, 
			long distance, long duration, long altitude, PolylineOptions line);
	
	public void onDirectionsDownloadFailure (String message);

}
