package com.example.easybike;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.Stop;
import com.example.easybike.model.Tour;
import com.example.support.DirectionsDownloadListener;
import com.google.android.gms.drive.internal.ac;

public class StopsAdapter extends BaseAdapter {

	private final static String TAG = StopsAdapter.class.getSimpleName();

	private List<Stop> stops;
	private LayoutInflater inflater;
	private TourDetailsActivity activity;
	private Tour tour;
	private DBManagement dbManag;
	private DirectionsDownloadListener directionsListener;

	public StopsAdapter(TourDetailsActivity activity, List<Stop> stops,
			Tour tour, DirectionsDownloadListener directionsListener) {
		this.stops = stops;
		this.activity = activity;
		this.tour = tour;
		this.directionsListener = directionsListener;
		dbManag = DBManagement.getInstance(activity.getApplicationContext());
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Log.d(TAG, "StopsAdapter created");
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount executed");
		return stops.size();
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem executed with position " + position);
		return stops.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId executed with position " + position);
		return stops.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Log.d(TAG, "getView called with position " + position);
		
		View view = convertView;
		Stop stop = stops.get(position);

		if (view == null)
			view = inflater.inflate(R.layout.stops_list_cell, parent, false);

		// Gather views
		LinearLayout moveBtnLayout = (LinearLayout) view
				.findViewById(R.id.move_up_down_btns);

		TextView order = (TextView) view.findViewById(R.id.stop_order);
		TextView address = (TextView) view.findViewById(R.id.stop_address);
		//TextView coord = (TextView) view.findViewById(R.id.stop_coord);
		TextView time = (TextView) view.findViewById(R.id.time_value);
		TextView distance = (TextView) view.findViewById(R.id.distance_value);

		ImageButton completedBtn = (ImageButton) view
				.findViewById(R.id.completed_btn);

		ImageButton moveUpBtn = (ImageButton) view
				.findViewById(R.id.move_up_btn);
		ImageButton moveDownBtn = (ImageButton) view
				.findViewById(R.id.move_down_btn);
		ImageButton removeBtn = (ImageButton) view
				.findViewById(R.id.remove_btn);

		order.setText(Integer.toString(stop.getOrderInTour()));
		address.setText(stop.getAddress());
		//coord.setText(stop.getGpsCoordLat() + ", " + stop.getGpsCoordLng());
		time.setText(stop.getFormattedTimeFromPreviousStop());
		distance.setText(stop.getFormattedDistanceFromPreviousStop());

		// Distinction between tour to start or in progress or completed

		if (tour.getStatus().equals(Status.TO_START)) {
			
			Log.d(TAG, "Configuration for to start status with position " + position);
			
			moveBtnLayout.setVisibility(View.VISIBLE);
			removeBtn.setVisibility(View.VISIBLE);
			completedBtn.setVisibility(View.GONE);

			removeBtn.setOnClickListener(new RemoveOnClickListener(stop));

			// Hide move up button for first element
			if (position == 0)
				moveUpBtn.setVisibility(View.INVISIBLE);
			else {
				moveUpBtn.setVisibility(View.VISIBLE);
				moveUpBtn.setOnClickListener(new MoveUpOnClickListener(stop));
			}

			// Hide move down button for last element
			if (position == stops.size() - 1)
				moveDownBtn.setVisibility(View.INVISIBLE);
			else {
				moveDownBtn.setVisibility(View.VISIBLE);
				moveDownBtn
						.setOnClickListener(new MoveDownOnClickListener(stop));
			}

		} else if (tour.getStatus().equals(Status.IN_PROGRESS)) {
			
			Log.d(TAG, "Configuration for in progress status with position " + position);

			moveBtnLayout.setVisibility(View.GONE);
			removeBtn.setVisibility(View.GONE);
			completedBtn.setVisibility(View.VISIBLE);

			if (stop.isCompleted())
				completedBtn.setImageResource(R.drawable.ic_sign_check_signed);
			else
				completedBtn
						.setImageResource(R.drawable.ic_sign_check_unsigned);

			completedBtn.setOnClickListener(new CompletedOnClickListener(stop));
			
		} else if (tour.getStatus().equals(Status.COMPLETED)) {
			
			Log.d(TAG, "Configuration for completed status with position " + position);

			moveBtnLayout.setVisibility(View.GONE);
			removeBtn.setVisibility(View.GONE);
			completedBtn.setVisibility(View.GONE);
			
		} else
			Log.e(TAG, "Tour status is invalid: " + tour.getStatus().name());

		Log.d(TAG, "getView finished with position " + position);
		
		return view;
	}

	private class CompletedOnClickListener implements OnClickListener {

		private Stop stop;

		public CompletedOnClickListener(Stop stop) {
			super();
			this.stop = stop;
		}

		@Override
		public void onClick(View v) {
			
			Log.d(TAG, "Click on Complete Stop for stop #" + stop.getOrderInTour());

			final ImageButton btn = (ImageButton) v;
			
			// Check if the selected stop is being completed
			if (!stop.isCompleted()) {
				
				// If the previous one aren't completed
				if (stop.getOrderInTour() > 1
						&& !stops.get(stop.getOrderInTour() - 2).isCompleted()) {
					
					Log.d(TAG, "Showing are you sure pop up for completing more than one stop all together");
					
					// are you sure popup informs that also all precedent stops will be completed							
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.complete_precedent_stops_popup)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Ok");
							// Complete this stop and all previous ones
							for (int i = 0; i < stop.getOrderInTour(); i++) {
								Stop prevStop = stops.get(i);
								if (!prevStop.isCompleted()) {
									prevStop.setCompleted(true);
									dbManag.updateStop(prevStop);
								}
							}
							
							Log.d(TAG, "Are you sure pop up: calling refreshData after updating all stops");
							
							// Refresh whole view to manage all newly completed stops
							activity.refreshData();			
							popupIfAllStopsAreCompleted();
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Cancel");
							dialog.cancel();
						}
					}).show();		
					
				} else {
					// If it's the next stop to be naturally complete
					
					// are you sure popup with no special message
					/*AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.complete_stop_popup)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {*/
							stop.setCompleted(true);
							btn.setImageResource(R.drawable.ic_sign_check_signed);
							dbManag.updateStop(stop);
							
							Log.d(TAG, "Calling refreshData after setting as completed stop: " + stop);
							
							activity.refreshData();
							popupIfAllStopsAreCompleted();
						/*}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();*/
									
				}
					
				
			} else {
				// This stop is being uncompleted
				
				// If the next one is completed
				if (stop.getOrderInTour() < stops.size()
						&& stops.get(stop.getOrderInTour()).isCompleted()) {
					
					Log.d(TAG, "Showing are you sure pop up for uncompleting more than one stop all together");
					
					// are you sure popup informs that also all next stops will be uncompleted
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.uncomplete_next_stops_popup)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Ok");
							// Uncomplete this stop and all next ones
							for (int i = stop.getOrderInTour()-1; i < stops.size(); i++) {
								Stop nextStop = stops.get(i);
								if (nextStop.isCompleted()) {
									nextStop.setCompleted(false);
									dbManag.updateStop(nextStop);
								}
							}
							
							Log.d(TAG, "Are you sure pop up: calling refreshData after updating all stops");
							
							// Refresh whole view to manage all newly uncompleted stops
							activity.refreshData();					
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: Click on Cancel");
							dialog.cancel();
						}
					}).show();
					
				} else {
					// If it was the last stop to be completed
					
					// are you sure popup with no special message
					/*AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.uncomplete_stop_popup)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {*/
							stop.setCompleted(false);
							btn.setImageResource(R.drawable.ic_sign_check_unsigned);
							dbManag.updateStop(stop);		
							
							Log.d(TAG, "Calling refreshData after setting as uncompleted stop: " + stop);
							
							activity.refreshData();
							/*
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();*/
					
				}
			}
			
		}
	}

	private class RemoveOnClickListener implements OnClickListener {

		private Stop stop;

		public RemoveOnClickListener(Stop stop) {
			super();
			this.stop = stop;
		}

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Click on Remove for stop #" + stop.getOrderInTour() 
					+ " showing are you sure pop up");
			
			// are you sure popup
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			builder.setTitle(R.string.are_you_sure)
			.setMessage(R.string.remove_stop_popup)
			.setPositiveButton(R.string.remove_stop, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Are you sure pop up: Click on Remove Stop");
					dbManag.removeStopFromTour(stop, directionsListener);
					
					Log.d(TAG, "Are you sure pop up: calling refreshData after removing stop");
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
	}

	private class MoveUpOnClickListener implements OnClickListener {

		private Stop stop;

		public MoveUpOnClickListener(Stop stop) {
			super();
			this.stop = stop;
		}

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Click on Move Up for stop #" + stop.getOrderInTour());
			
			dbManag.moveUpStop(stop, directionsListener);
			
			Log.d(TAG, "Calling refreshData after moving up stop: " + stop);
			activity.refreshData();
		}
	}

	private class MoveDownOnClickListener implements OnClickListener {

		private Stop stop;

		public MoveDownOnClickListener(Stop stop) {
			super();
			this.stop = stop;
		}

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Click on Move Down for stop #" + stop.getOrderInTour());
			dbManag.moveDownStop(stop, directionsListener);
			
			Log.d(TAG, "Calling refreshData after moving down stop: " + stop);
			activity.refreshData();
		}
	}

	public void refreshData() {
		Log.d(TAG, "refreshData called");
		tour = activity.getDisplayedTour();
		stops = activity.getStopsOfDisplayedTour();
		Log.d(TAG, "refreshData calling notifyDataSetChanged");
		this.notifyDataSetChanged();
		Log.d(TAG, "refreshData finished");
	}
	
	private void popupIfAllStopsAreCompleted () {
		
		Log.d(TAG, "popupIfAllStopsAreCompleted called");
		
		// check if all stops have been completed, if do show pop up 
		// asking if the entire tour should be completed	
		if (stops.get(stops.size()-1).isCompleted()) {
			
			Log.d(TAG, "All stops have been completed, showing complete tour pop up");

			// popup asks if the tour should be completed
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			builder.setTitle(R.string.complete_tour)
			.setMessage(R.string.complete_tour_popup)
			.setPositiveButton(R.string.end_tour, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					Log.d(TAG, "Complete tour pop up: click on End Tour, showing are you sure pop up");
					
					// are you sure popup
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);

					builder.setTitle(R.string.are_you_sure)
					.setMessage(R.string.end_tour_popup)
					.setPositiveButton(R.string.end_tour, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: click on End Tour");
							activity.setCompletedTour(tour);
							
							Log.d(TAG, "Are you sure pop up: calling refreshData after" +
									" setting as completed tour: " + tour);
							activity.refreshData();							
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Are you sure pop up: click on Cancel, dismissing pop up");
							dialog.cancel();
						}
					}).show();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Complete tour pop up: click on Cancel, dismissing dialog");
					dialog.cancel();
				}
			}).show();
		}		
		
		Log.d(TAG, "popupIfAllStopsAreCompleted finished");
	}

}
