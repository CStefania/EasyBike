package com.example.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.example.easybike.model.Stop;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

// Fetches data from url passed
public class DirectionsDownloadTask extends
		AsyncTask<Stop, Void, List<List<HashMap<String, String>>>> {

	private final static String TAG = DirectionsDownloadTask.class.getSimpleName();

	private DirectionsDownloadListener listener;
	private Stop origin, destination;

	public DirectionsDownloadTask(DirectionsDownloadListener listener) {
		super();
		this.listener = listener;
	}

	@Override
	protected List<List<HashMap<String, String>>> doInBackground(Stop... stop) {

		// For storing data from web service
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		
		origin = stop[0];
		destination = stop[1];

		// Fetching the data from web service
		try {
			URL url = new URL(getDirectionsUrl(origin.getLatLng(), destination.getLatLng()));
			
			Log.d(TAG, "Getting directions on url: " + url.toString());

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d(TAG, "Background Task failed", e);
		} finally {
			try {
				if (iStream != null)
					iStream.close();
				
				if (urlConnection != null)
					urlConnection.disconnect();
			} catch (Exception e) {
				Log.e(TAG, "Closing the download stream exception", e);
			}		
		}

		if (data== null || isCancelled())
			return null;

		// parse json results
		JSONObject jObject;
		List<List<HashMap<String, String>>> routes = null;

		try {
			jObject = new JSONObject(data);

			// Starts parsing data
			routes = parse(jObject);
		} catch (Exception e) {
			Log.e(TAG, "Parsing downloaded json data failed", e);
		}

		return routes;
	}
	
	/**
	 * Receives a JSONObject and returns a list of lists containing latitude and
	 * longitude
	 */
	private List<List<HashMap<String, String>>> parse(JSONObject jObject) {
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;
		JSONObject jDistance = null;
		JSONObject jDuration = null;

		try {
			jRoutes = jObject.getJSONArray("routes");
			/** Traversing all routes */
			for (int i = 0; i < jRoutes.length(); i++) {
				
				if (isCancelled())
					return null;
				
				jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
				/** Traversing all legs */
				for (int j = 0; j < jLegs.length(); j++) {
					
					if (isCancelled())
						return null;
					
					/** Getting distance from the json data */
					jDistance = ((JSONObject) jLegs.get(j))
							.getJSONObject("distance");
					HashMap<String, String> hmDistance = new HashMap<String, String>();
					hmDistance.put("distance", jDistance.getString("value"));

					/** Getting duration from the json data */
					jDuration = ((JSONObject) jLegs.get(j))
							.getJSONObject("duration");
					HashMap<String, String> hmDuration = new HashMap<String, String>();
					hmDuration.put("duration", jDuration.getString("value"));
					
					// TODO check if google gives also altitude
					
					/** Adding distance object to the path */
					path.add(hmDistance);

					/** Adding duration object to the path */
					path.add(hmDuration);

					jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

					/** Traversing all steps */
					for (int k = 0; k < jSteps.length(); k++) {
						
						if (isCancelled())
							return null;
						
						String polyline = "";
						polyline = (String) ((JSONObject) ((JSONObject) jSteps
								.get(k)).get("polyline")).get("points");
						List<LatLng> list = this.decodePoly(polyline);

						/** Traversing all points */
						for (int l = 0; l < list.size(); l++) {
							
							if (isCancelled())
								return null;
							
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat",
									Double.toString((list.get(l)).latitude));
							hm.put("lng",
									Double.toString((list.get(l)).longitude));
							path.add(hm);
						}
					}
				}
				routes.add(path);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}

		return routes;
	}

	@Override
	protected void onPostExecute(List<List<HashMap<String, String>>> result) {
		super.onPostExecute(result);

		ArrayList<LatLng> points = null;
		PolylineOptions lineOptions = null;
		MarkerOptions markerOptions = new MarkerOptions();
		long distance = 0, duration = 0, altitude = 0;

		if (result == null || result.size() < 1) {
			listener.onDirectionsDownloadFailure("No points found");
			return;
		}

		// Traversing through all the routes TODO try only use first route
		for (int i = 0; i < result.size(); i++) {
			points = new ArrayList<LatLng>();
			lineOptions = new PolylineOptions();

			// Fetching i-th route
			List<HashMap<String, String>> path = result.get(i);

			// Fetching all the points in i-th route
			for (int j = 0; j < path.size(); j++) {
				HashMap<String, String> point = path.get(j);

				if (j == 0) { // Get distance from the list
					distance = Long.parseLong(point.get("distance"));
					
					continue;
				} else if (j == 1) { // Get duration from the list
					duration = Long.parseLong(point.get("duration"));
					continue;
				}
				double lat = Double.parseDouble(point.get("lat"));
				double lng = Double.parseDouble(point.get("lng"));
				LatLng position = new LatLng(lat, lng);
				points.add(position);
			}

			// Adding all the points in the route to LineOptions
			lineOptions.addAll(points);
		}

		listener.onDirectionsDownloadSuccess(origin, destination, 
				distance, duration, altitude, lineOptions);
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {
		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}
	
	/**
	 * Method to decode polyline points Courtesy :
	 * jeffreysambells.com/2010/05/27
	 * /decoding-polylines-from-google-maps-direction-api-with-java
	 * */
	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng(((lat / 1E5)), ((lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}
}
