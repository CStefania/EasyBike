package com.example.easybike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.StorageDBHelper;
import com.example.easybike.model.Tour;

public class ToursReviewActivity extends ActionBarActivity {

	private final static String TAG = ToursReviewActivity.class.getSimpleName();
	
	private final static String SHOWED_STATUS = "showed_status";
	private DBManagement dbManag;
	
	private TextView titleText, errorText;
	private Button createTourBtn;
	private GridView gridView;
	private ToursAdapter toursAdapterCurrent, toursAdapterNew,
			toursAdapterCompleted;
	private Status showedStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate called");
		
		setContentView(R.layout.activity_tours_review);

		dbManag = DBManagement.getInstance(getApplicationContext());
		toursAdapterCurrent = new ToursAdapter(this,
				dbManag.getTours(Status.IN_PROGRESS));
		toursAdapterNew = new ToursAdapter(this,
				dbManag.getTours(Status.TO_START));
		toursAdapterCompleted = new ToursAdapter(this,
				dbManag.getTours(Status.COMPLETED));
		
		// Set back button in action bar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Gather views
		titleText = (TextView) findViewById(R.id.title);
		errorText = (TextView) findViewById(R.id.errorText);
		gridView = (GridView) findViewById(R.id.gridView);
		createTourBtn = (Button) findViewById(R.id.planNewBtn);
		
		errorText.setVisibility(View.GONE);
		
		createTourBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Click on Create New Tour");
				Intent intent = new Intent(getApplicationContext(), TourDetailsActivity.class);
				Tour tour = new Tour();
				tour.setStatus(Status.TO_START);
				int id = DBManagement.getInstance(getApplicationContext()).addOrUpdateTour(tour, null);
				intent.putExtra(StorageDBHelper.TOUR_ID, id);
				startActivity(intent);				
			}
		});
		
		SharedPreferences pref = getSharedPreferences("PREF", 0);
		showedStatus = Status.valueOf(pref.getString(SHOWED_STATUS, Status.IN_PROGRESS.toString()));
		
		Intent intent = getIntent();
		String showedStatusStr = intent.getStringExtra(StorageDBHelper.STATUS);
		if (showedStatusStr != null) {
			Log.d(TAG, "Intent set requested status " + showedStatusStr);
			showedStatus = Status.valueOf(showedStatusStr);
		}

		showStatus(showedStatus);
		
		Log.d(TAG, "onCreate finished");
		
		// TODO if to_start section is empty, popup to create a new tour
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tours_review, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			super.onBackPressed();
	        return true;
		case R.id.current_status:
			showStatus(Status.IN_PROGRESS);
			return true;
		case R.id.to_start_status:
			showStatus(Status.TO_START);
			return true;
		case R.id.completed_status:
			showStatus(Status.COMPLETED);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showStatus (Status status) {
		
		Log.d(TAG, "showStatus executed with status " + status);
		
		errorText.setVisibility(View.GONE);
		gridView.setVisibility(View.VISIBLE);
		showedStatus = status;
		
		switch (status) {
		case IN_PROGRESS:
			gridView.setAdapter(toursAdapterCurrent);
			titleText.setText(R.string.current_status_title);
			
			// Check if no in progress tours are present
			if (toursAdapterCurrent.getCount() < 1)
				setErrorText(R.string.no_current_tour);			
			return;
			
		case TO_START:
			gridView.setAdapter(toursAdapterNew);
			titleText.setText(R.string.to_start_status_title);

			// Check if no in progress tours are present
			if (toursAdapterNew.getCount() < 1)
				setErrorText(R.string.no_to_start_tour);
			return;
			
		case COMPLETED:
			gridView.setAdapter(toursAdapterCompleted);
			titleText.setText(R.string.completed_status_title);
			
			// Check if no in progress tours are present
			if (toursAdapterCompleted.getCount() < 1)
				setErrorText(R.string.no_completed_tour);			
			return;
		}
	}
	
	private void setErrorText (int resId) {
		Log.d(TAG, "setErrorText executed");
		errorText.setText(resId);
		errorText.setVisibility(View.VISIBLE);
		gridView.setVisibility(View.GONE);
	}
	
	private void refreshData () {
		Log.d(TAG, "refreshData called");
		toursAdapterCurrent.setData(dbManag.getTours(Status.IN_PROGRESS));
		toursAdapterCurrent.notifyDataSetChanged();
		toursAdapterNew.setData(dbManag.getTours(Status.TO_START));
		toursAdapterNew.notifyDataSetChanged();
		toursAdapterCompleted.setData(dbManag.getTours(Status.COMPLETED));
		toursAdapterCompleted.notifyDataSetChanged();
		Log.d(TAG, "refreshData finished");
	}

	
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause executed, saving currently shown status " + showedStatus.name());
		SharedPreferences pref = getSharedPreferences("PREF", 0);
		pref.edit().putString(SHOWED_STATUS, showedStatus.toString()).commit();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart executed, refreshing data");
		refreshData();
	}	
	
}
