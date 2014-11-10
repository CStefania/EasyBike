package com.example.easybike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.Tour;

public class GeneralInfoFragment extends Fragment {
	
	private final static String TAG = GeneralInfoFragment.class.getSimpleName();
	
	private Tour tour;
	private DBManagement dbManag;
	private TourDetailsActivityInterface activity;
	
	private TextView nameView, statusView, totDistanceView, totTimeView, stopsView,
		remainingDistanceView, remainingTimeView, remainingStopsView;
	private TableRow remainingDistanceLine, remainingTimeLine, remainingStopsLine;
	private Button modifyBtn, startBtn, endTourBtn, continueTourBtn;

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
		
		View rootView = inflater.inflate(R.layout.fragment_tour_details_general_info,
				container, false);
		
		dbManag = DBManagement.getInstance(getActivity().getApplicationContext());
		
		if (tour.getName() == null) {
			ModifyTourDialogFragment modifyDialog = new ModifyTourDialogFragment(tour);
			modifyDialog.show(getFragmentManager(), null);
		}
			
		
		// Get views
		nameView = (TextView) rootView.findViewById(R.id.name_value);
		statusView = (TextView) rootView.findViewById(R.id.status_value);
		totDistanceView = (TextView) rootView.findViewById(R.id.totkm_value);
		totTimeView = (TextView) rootView.findViewById(R.id.tottime_value);
		stopsView = (TextView) rootView.findViewById(R.id.stopnum_value);
		remainingDistanceView = (TextView) rootView.findViewById(R.id.left_distance_value);
		remainingTimeView = (TextView) rootView.findViewById(R.id.left_time_value);
		remainingStopsView = (TextView) rootView.findViewById(R.id.left_stopnum_value);
		
		remainingDistanceLine = (TableRow) rootView.findViewById(R.id.remainingDistanceLine);
		remainingTimeLine = (TableRow) rootView.findViewById(R.id.remainingTimeLine);
		remainingStopsLine = (TableRow) rootView.findViewById(R.id.remainingStopsLine);
		
		modifyBtn = (Button) rootView.findViewById(R.id.modify_tour_btn);
		startBtn = (Button) rootView.findViewById(R.id.start_tour_btn);
			
		endTourBtn = (Button) rootView.findViewById(R.id.end_tour_btn);
		continueTourBtn = (Button) rootView.findViewById(R.id.continue_tour_btn);
		
		refreshViewData();
		
		return rootView;
	}

	public void refreshViewData() {
		
		Log.d(TAG, "refreshViewData called");
		
		if (tour != null) {
			nameView.setText(tour.getName());
			totDistanceView.setText(DBManagement.formatDistance(tour.getTotalDistance()));
			totTimeView.setText(DBManagement.formatTime(tour.getTotalTime()));
			stopsView.setText(Integer.toString(tour.getTotalStops()));
			remainingDistanceView.setText(DBManagement.formatDistance(tour.getRemainingDistance()));
			remainingTimeView.setText(DBManagement.formatTime(tour.getRemainingTime()));
			remainingStopsView.setText(Integer.toString(tour.getRemainingStops()));
			
			switch (tour.getStatus()) {
			case IN_PROGRESS:
				statusView.setText(R.string.current_status);		
				break;			
			case TO_START:
				statusView.setText(R.string.to_start_status);
				break;
			case COMPLETED:
				statusView.setText(R.string.completed_status);
				break;
			}
		}

		// Change view according to tour status
		if (tour.getStatus().equals(Status.TO_START)) {
			
			Log.d(TAG, "To start configuration");

			modifyBtn.setVisibility(View.VISIBLE);
			startBtn.setVisibility(View.VISIBLE);
			
			endTourBtn.setVisibility(View.GONE);
			continueTourBtn.setVisibility(View.GONE);
			
			remainingDistanceLine.setVisibility(View.GONE);
			remainingTimeLine.setVisibility(View.GONE);
			remainingStopsLine.setVisibility(View.GONE);

			// Set modify tour button behaviour
			modifyBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ModifyTourDialogFragment modifyDialog = new ModifyTourDialogFragment(tour);
					modifyDialog.show(getFragmentManager(), null);
				}
			});

			// Set start tour button behaviour
			startBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// are you sure popup								
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.start_tour_popup)
					.setPositiveButton(R.string.start_tour, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.setCurrentTour(tour);
							activity.refreshData();							
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();

				}
			});
		} else if (tour.getStatus().equals(Status.IN_PROGRESS))	{
			
			Log.d(TAG, "In progress configuration");
			
			modifyBtn.setVisibility(View.GONE);
			startBtn.setVisibility(View.GONE);
			
			endTourBtn.setVisibility(View.VISIBLE);
			continueTourBtn.setVisibility(View.VISIBLE);
			
			remainingDistanceLine.setVisibility(View.VISIBLE);
			remainingTimeLine.setVisibility(View.VISIBLE);
			remainingStopsLine.setVisibility(View.VISIBLE);
			
			endTourBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// are you sure popup
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.end_tour_popup)
					.setPositiveButton(R.string.end_tour, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.setCompletedTour(tour);
							activity.refreshData();							
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							dialog.cancel();
						}
					}).show();
							
				}
			});

			continueTourBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO set continue tour btn behavior
					activity.openNavigation();
				}
			});
			
		} else if (tour.getStatus().equals(Status.COMPLETED))	{
			
			Log.d(TAG, "Completed configuration");
			
			modifyBtn.setVisibility(View.GONE);
			startBtn.setVisibility(View.GONE);
			
			endTourBtn.setVisibility(View.GONE);
			continueTourBtn.setVisibility(View.GONE);
			
			remainingDistanceLine.setVisibility(View.GONE);
			remainingTimeLine.setVisibility(View.GONE);
			remainingStopsLine.setVisibility(View.GONE);
			
		}
		
		Log.d(TAG, "refreshView finished");
	}
	
	public void refreshData() {
		Log.d(TAG, "refreshData called");
		tour = dbManag.getTourFromId(tour.getId());	
		refreshViewData();
		Log.d(TAG, "refreshData finished");
	}
	
	public GeneralInfoFragment () {
		
	}
}
