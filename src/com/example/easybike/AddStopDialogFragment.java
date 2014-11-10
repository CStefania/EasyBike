package com.example.easybike;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Stop;
import com.example.easybike.model.Tour;
import com.example.support.DirectionsDownloadListener;
import com.example.support.SearchAddressListener;
import com.example.support.SearchAddressTask;
import com.google.android.gms.internal.db;
import com.google.android.gms.maps.model.PolylineOptions;

public class AddStopDialogFragment extends DialogFragment implements
		SearchAddressListener {

	private final static String TAG = AddStopDialogFragment.class
			.getSimpleName();

	private Tour tour;
	private List<Stop> stops;
	private DBManagement dbManag;
	
	private LinearLayout dialogView;
	private EditText searchText;
	private ImageButton searchBtn;
	private TextView errorText;
	private ListView resultsList;
	private ProgressBar progressBar;
	private SearchAddressTask searchTask;
	private Stop newStop;
	private SearchAddressListener listener = this;
	private DirectionsDownloadListener directionsListener;
	private AddressListAdapter resultsAdapter;
	private TourDetailsActivityInterface activity;

	public AddStopDialogFragment(Tour tour, DirectionsDownloadListener directionsListener) {
		this.tour = tour;
		this.directionsListener = directionsListener;
		Log.d(TAG, "AddStopDialogFragment created");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateDialog called");
		
		dbManag = DBManagement.getInstance(getActivity().getApplicationContext());
		try {
			activity = (TourDetailsActivityInterface) getActivity();
		} catch (ClassCastException e) {
			Log.e(TAG, "Activity must implement " + TourDetailsActivityInterface.class.getSimpleName(), e);
			return null;
        }
			
		stops = activity.getStopsOfDisplayedTour();			
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_add_stop,
				null);

		// Get all view elements
		searchText = (EditText) dialogView.findViewById(R.id.searchText);
		searchBtn = (ImageButton) dialogView.findViewById(R.id.searchBtn);
		errorText = (TextView) dialogView.findViewById(R.id.errorText);
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		resultsList = (ListView) dialogView.findViewById(R.id.listResults);

		if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
				|| !Geocoder.isPresent()) {
			Log.e(TAG, "Backend service for Geocoder is not present");
			searchText.setVisibility(View.GONE);
			searchBtn.setVisibility(View.GONE);
			resultsList.setVisibility(View.GONE);
			errorText.setText(R.string.no_geolocation);
			errorText.setVisibility(View.VISIBLE);
		}

		// set search button behavior
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				String address = searchText.getText().toString();
				
				Log.d(TAG, "Click on search button with address: " + address);
				
				resultsList.setVisibility(View.GONE);
				errorText.setVisibility(View.GONE);

				if (searchTask != null)
					searchTask.cancel(true);

				searchTask = new SearchAddressTask(getActivity()
						.getApplicationContext(), listener);
				
				Log.d(TAG, "Launching search task on address: " + address);
				
				searchTask.execute(address);
				progressBar.setVisibility(View.VISIBLE);

			}
		});

		resultsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(dialogView)
				// Add action button
				.setPositiveButton(R.string.add_stop,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
								// Get selected item
								int position = resultsList.getCheckedItemPosition();
								
								Log.d(TAG, "Click on Add Stop with position: " + position);
								
								if (position != AbsListView.INVALID_POSITION) {
									Address selectedAddr = (Address) resultsAdapter.getItem(position);
									
									String address = String.format(
							                "%s, %s, %s",
							                // If there's a street address, add it
							                selectedAddr.getMaxAddressLineIndex() > 0 ?
							                		selectedAddr.getAddressLine(0) : "",
							                // Locality is usually a city
							                		selectedAddr.getLocality(),
							                // The country of the address
							                		selectedAddr.getCountryName());
									double gpsCoordLat = selectedAddr.getLatitude();
									double gpsCoordLng = selectedAddr.getLongitude();
									
									// Create and add to the database the new stop
									Stop stop = new Stop(tour.getId(), stops.size()+1, 
											address, gpsCoordLat, gpsCoordLng, false,
											0, 0 , 0);
									
									Log.d(TAG, "Adding Stop with address: " + selectedAddr);
									
									dbManag.addStopToTour(stop, directionsListener);
									
									Log.d(TAG, "Calling refresh data after adding stop: " + stop);
									
									activity.refreshData();									
								}
														
								AddStopDialogFragment.this.getDialog().cancel();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
								Log.d(TAG, "Click on Cancel, dismissing dialog");
								
								// stop the asynch task
								if (searchTask != null)
									searchTask.cancel(true);
								AddStopDialogFragment.this.getDialog().cancel();
							}
						});
		return builder.create();
	}

	// Called by the search address asynch task when 
	// the results are received, if correct
	@Override
	public void searchAddressCorrectResult(List<Address> results) {
		Log.d(TAG, "searchAddressCorrectResult called with " + results.size() + " results");
		
		errorText.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		resultsAdapter = new AddressListAdapter(results, getActivity());
		resultsList.setAdapter(resultsAdapter);
		resultsList.setVisibility(View.VISIBLE);
		resultsList.setItemChecked(0, true);
		
		Log.d(TAG, "searchAddressCorrectResult finished showing " + results.size() + " results");
	}

	// Called by the search address  asynch task when
	// the results are received, if wrong
	@Override
	public void searchAddressWrongResult(String message) {		
		Log.d(TAG, "searchAddressWrongResult executed with error message: " + message);
		
		progressBar.setVisibility(View.GONE);
		errorText.setText(R.string.no_result);
		errorText.setVisibility(View.VISIBLE);
	}

}
