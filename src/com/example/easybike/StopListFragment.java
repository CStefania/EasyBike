package com.example.easybike;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.Stop;
import com.example.easybike.model.Tour;
import com.example.support.DirectionsDownloadListener;
import com.google.android.gms.maps.model.PolylineOptions;

public class StopListFragment extends Fragment implements DirectionsDownloadListener {

	private static final String TAG = StopListFragment.class.getSimpleName();

	private Tour tour;
	private TourDetailsActivityInterface activity;
	private DBManagement dbManag;
	
	private DirectionsDownloadListener directionsListener = this;

	private ListView stopsList;
	private List<Stop> stops;
	private StopsAdapter adapter;

	private TextView stopsNum;
	private Button addStopBtn, startTourBtn, endTourBtn, continueTourBtn;
	private LinearLayout buttonsLine;

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

		View rootView = inflater.inflate(
				R.layout.fragment_tour_details_stops_list, container, false);

		adapter = new StopsAdapter((TourDetailsActivity) getActivity(), stops,
				tour, directionsListener);

		// Gather all views
		stopsNum = (TextView) rootView.findViewById(R.id.stopnum_value);
		addStopBtn = (Button) rootView.findViewById(R.id.add_stop_btn);
		startTourBtn = (Button) rootView.findViewById(R.id.start_tour_btn);
		endTourBtn = (Button) rootView.findViewById(R.id.end_tour_btn);
		continueTourBtn = (Button) rootView
				.findViewById(R.id.continue_tour_btn);
		buttonsLine = (LinearLayout) rootView.findViewById(R.id.buttonsLine);

		stopsList = (ListView) rootView.findViewById(R.id.stops_list);
		stopsList.setAdapter(adapter);
		
		refreshViewData();
		
		Log.d(TAG, "onCreateView finished");

		return rootView;
	}

	public void refreshViewData() {
		
		Log.d(TAG, "refreshViewData called");
		
		stopsNum.setText(Integer.toString(stops.size()));

		// Change view according to tour status
		if (tour.getStatus().equals(Status.TO_START)) {
			
			Log.d(TAG, "Configuration for status to start");
			
			buttonsLine.setVisibility(View.VISIBLE);

			addStopBtn.setVisibility(View.VISIBLE);
			startTourBtn.setVisibility(View.VISIBLE);

			endTourBtn.setVisibility(View.GONE);
			continueTourBtn.setVisibility(View.GONE);

			addStopBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click on Add Stop");
					AddStopDialogFragment dialog = new AddStopDialogFragment(
							tour, directionsListener);
					dialog.show(getFragmentManager(), null);
				}
			});

			startTourBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click on Start Tour, showing are you sure pop up" +
							"");
					// are you sure popup								
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.start_tour_popup)
					.setPositiveButton(R.string.start_tour, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Start Tour");
							activity.setCurrentTour(tour);
							Log.d(TAG, "Are you sure pop up: calling refreshData " +
									"after setting as current tour: " + tour);
							activity.refreshData();							
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Cancel, dismissing dialog");
							dialog.cancel();
						}
					}).show();
			
				}
			});

		} else if (tour.getStatus().equals(Status.IN_PROGRESS)) {
			
			Log.d(TAG, "Configuration for status in progress");

			buttonsLine.setVisibility(View.VISIBLE);
			
			addStopBtn.setVisibility(View.GONE);
			startTourBtn.setVisibility(View.GONE);

			endTourBtn.setVisibility(View.VISIBLE);
			continueTourBtn.setVisibility(View.VISIBLE);

			endTourBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					Log.d(TAG, "Click on End Tour, showing are you sure pop up");

					// are you sure popup
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.end_tour_popup)
					.setPositiveButton(R.string.end_tour, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on End Tour");
							activity.setCompletedTour(tour);
							Log.d(TAG, "Are you sure pop up: calling refreshData after completing" +
									"tour: " + tour);
							activity.refreshData();							
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							Log.d(TAG, "Are you sure pop up: Click on Cancel, dismissing dialog");
							dialog.cancel();
						}
					}).show();
							
				}
			});

			continueTourBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(TAG, "Click on Continue Tour");
					// TODO set continue tour btn behavior
					activity.openNavigation();
				}
			});

		} else if (tour.getStatus().equals(Status.COMPLETED)) {
			
			Log.d(TAG, "Configuration for status completed");

			buttonsLine.setVisibility(View.GONE);
		}

		Log.d(TAG, "refreshViewData finished");
	}
	
	// Called by the directions asynch task when time, distance, altitude
	// data are received
	@Override
	public void onDirectionsDownloadSuccess(Stop origin, Stop destination,
			long distance, long duration, long altitude, PolylineOptions line) {
		
		Log.d(TAG, "onDirectionsDownloadSuccess called with origin: " + origin.getAddress()
				+ " and dest: " + destination.getAddress());
		
		destination.setTimeFromPrevStop(duration);
		destination.setDistanceFromPrevStop(distance);
		destination.setAltitudeFromPrevStop(altitude);
		
		dbManag.updateStop(destination);
		
		Log.d(TAG, "Calling refreshData after updating stop: " + destination);
		
		activity.refreshData();		
		
		Log.d(TAG, "onDirectionsDownloadSuccess finished with origin: " + origin.getAddress()
				+ " and dest: " + destination.getAddress());
	}

	// Called by the directions asynch task when time, distance, altitude
	// data are received but there's an error
	@Override
	public void onDirectionsDownloadFailure(String message) {
		Log.d(TAG, "onDirectionsDownloadFailure executed with error message: "
				+ message);
		// TODO manage error onDirectionsDownloadFailure
		
	}

	public void refreshData() {
		Log.d(TAG, "refreshData called");
		tour = activity.getDisplayedTour();
		stops = activity.getStopsOfDisplayedTour();
		adapter.refreshData();
		refreshViewData();
		Log.d(TAG, "refreshData finished");
	}

	public StopListFragment() {

	}

}
