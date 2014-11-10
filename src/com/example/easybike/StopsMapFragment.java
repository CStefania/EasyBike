package com.example.easybike;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.Stop;
import com.example.easybike.model.Tour;
import com.example.support.DirectionsDownloadListener;
import com.example.support.DirectionsDownloadTask;
import com.google.android.gms.drive.internal.ac;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class StopsMapFragment extends SupportMapFragment implements
		LocationListener {

	private static final String TAG = StopsMapFragment.class.getSimpleName();

	private Tour tour;
	private List<Stop> stops;
	private TourDetailsActivityInterface activity;
	private DBManagement dbManag;
	private GoogleMap map;
	private List<DirectionsDownloadTask> tasks = new ArrayList<DirectionsDownloadTask>();
	private Stop nextUncompletedStop;
	private DirectionsDownloadTask navigationTask;
	private boolean drawnPolylines = false;

	private LocationManager locationManager;
	private static final long MIN_TIME = 5000;
	private static final float MIN_DISTANCE = 1000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView called");

		try {
			activity = (TourDetailsActivityInterface) getActivity();
		} catch (ClassCastException e) {
			Log.e(TAG, "Activity must implement " + TourDetailsActivityInterface.class.getSimpleName(), e);
			return null;
        }
		
		tour = activity.getDisplayedTour();
		stops = activity.getStopsOfDisplayedTour();
		dbManag = DBManagement.getInstance(getActivity()
				.getApplicationContext());

		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);
		
		for (Stop stop: stops) {
			if (!stop.isCompleted()) {
				nextUncompletedStop = stop;
				break;
			}
		}

		// Get a handle to the Map Fragment
		map = getMap();

		if (!drawnPolylines)
			drawTourPolylines();
		
		Log.d(TAG, "onCreateView finished");

		return rootView;
	}

	private void drawTourPolylines() {
		
		Log.d(TAG, "drawTourPolylines called");
		
		Stop prevStop = null;

		for (Stop stop : stops) {

			// Add markers for each stop
			map.addMarker(new MarkerOptions()
					.title(stop.getAddress())
					.snippet(
							getActivity().getString(R.string.stop_number) + " "
									+ stop.getOrderInTour())
					.position(stop.getLatLng()));

			Log.d(TAG, "Added a marker for stop: " + stop);

			// Center map camera on first stop of the tour
			if (stop.getOrderInTour() == 1) {
				map.setMyLocationEnabled(true);
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						stop.getLatLng(), 13));
				Log.d(TAG, "Set camera focus on stop: " + stop);

				// Set as origin for next drawn polyline
				prevStop = stop;
				continue;
			}

			// Request polyline for each couple of stops (except the first)
			Stop[] params = new Stop[2];
			params[0] = prevStop;
			params[1] = stop;

			Log.d(TAG, "Launching directions task for polyline from " 
					+ prevStop.getAddress() + " to " + stop.getAddress());
			
			DirectionsDownloadTask task = new DirectionsDownloadTask(listener);
			task.execute(params);

			// Save task to be able to interrupt it if necessary
			tasks.add(task);

			// Update origin for next drawn polyline
			prevStop = stop;
		}
		
		drawnPolylines = true;
		Log.d(TAG, "drawTourPolylines finished");
	}

	private DirectionsDownloadListener listener = new DirectionsDownloadListener() {

		@Override
		public void onDirectionsDownloadSuccess(Stop origin, Stop destination,
				long distance, long duration, long altitude,
				PolylineOptions line) {
			
			Log.d(TAG, "onDirectionsDownloadSuccess called for origin: " + origin.getAddress()
					+ " and dest: " + destination.getAddress());
			// TODO manage distance and duration

			// Setting color and width and adding new polyline
			if (destination.isCompleted())
				line.color(Color.BLUE);
			else
				line.color(Color.RED);
			// line.width(5);

			map.addPolyline(line);
			Log.d(TAG, "onDirectionsDownloadSuccess finished, drawn polyline between " 
					+ origin.getAddress() + " and " + destination.getAddress());
		}

		@Override
		public void onDirectionsDownloadFailure(String message) {
			// TODO manage error onDirectionsDownloadFailure
			Log.e(TAG, "onDirectionsDownloadFailure called with error: " + message);
		}
	};

	public void refreshData() {
		Log.d(TAG, "refreshData called");
		tour = activity.getDisplayedTour();
		stops = activity.getStopsOfDisplayedTour();
		
		for (Stop stop: stops) {
			if (!stop.isCompleted()) {
				nextUncompletedStop = stop;
				break;
			}
		}
		
		Log.d(TAG, "refreshData clearing map and calling drawTourPolylines");
		map.clear();
		drawTourPolylines();
		Log.d(TAG, "refreshData finished");
	}

	@Override
	public void onDestroy() {
		
		Log.d(TAG, "onDestroy executed");

		for (DirectionsDownloadTask task : tasks)
			task.cancel(true);
		
		if (navigationTask != null)
			navigationTask.cancel(true);

		super.onDestroy();
	}

	public void startNavigation () {
		Log.d(TAG, "startNavigation executed");
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	}
	
	public void stopNavigation () {
		Log.d(TAG, "stopNavigation executed");
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged executed");
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				map.getCameraPosition().zoom);
		map.animateCamera(cameraUpdate);

		// Request polyline for navigation
		Stop[] params = new Stop[2];
		params[0] = new Stop(0, 0, "Current location", location.getLatitude(),
				location.getLongitude(), false, 0, 0, 0);
		params[1] = nextUncompletedStop;

		Log.d(TAG, "Launching directions task for polyline from current " +
				"location to " + nextUncompletedStop.getAddress());

		navigationTask = new DirectionsDownloadTask(listener);
		navigationTask.execute(params);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	
	

	@Override
	public void onPause() {
		if (tour.getStatus().equals(Status.IN_PROGRESS))
			stopNavigation();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (tour.getStatus().equals(Status.IN_PROGRESS))
			startNavigation();
	}

	public StopsMapFragment() {

	}
}
