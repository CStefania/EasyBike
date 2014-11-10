package com.example.easybike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.StorageDBHelper;
import com.example.easybike.model.Tour;


public class SplashScreenActivity extends ActionBarActivity {

	private static final String TAG = SplashScreenActivity.class.getSimpleName();
	
	public static final String PREFERENCES = "PREF";
	public static final String CURRENT_TOUR = "CURR_TOUR_ID";
	
	private int currentTourId;
	private Tour currentTour;
	private DBManagement dbManag;
	
	private TextView currTourNameView;
	private Button continueCurrBtn, reviewCurrBtn, startNewBtn, planNewBtn, reviewAllBtn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate called");
        
        setContentView(R.layout.activity_splash_screen);
        
        dbManag = DBManagement.getInstance(getApplicationContext());
        
        // Get views
        currTourNameView = (TextView) findViewById(R.id.currentTourName);
        continueCurrBtn = (Button) findViewById(R.id.continueBtn);
        reviewCurrBtn = (Button) findViewById(R.id.reviewBtn);
        startNewBtn = (Button) findViewById(R.id.startNewBtn);
        planNewBtn = (Button) findViewById(R.id.planNewBtn);
        reviewAllBtn = (Button) findViewById(R.id.reviewAllBtn);

        refreshCurrentTourViews();
        
        startNewBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Click on Start Another Tour");
				Intent intent = new Intent(getApplicationContext(), ToursReviewActivity.class);
				intent.putExtra(StorageDBHelper.STATUS, Status.TO_START.name());
				startActivity(intent);
			}
		});
        
        planNewBtn.setOnClickListener(new OnClickListener() {
			
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
        
        reviewAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Click on Review all Tours");
				Intent intent = new Intent(getApplicationContext(), ToursReviewActivity.class);
				startActivity(intent);
			}
		});
    }


    private void refreshCurrentTourViews() {
    	
    	Log.d(TAG, "refreshCurrentTourViews called");
    	
    	SharedPreferences pref = getSharedPreferences(PREFERENCES, 0);
        currentTourId = pref.getInt(CURRENT_TOUR, 0);
        
        Log.d(TAG, "Current tour id: " + currentTourId);
        
        currentTour = dbManag.getTourFromId(currentTourId);
        
    	// Check if there's a current tour
        if (currentTour != null) {
        	       	
        	// Set current tour name
        	currTourNameView.setText(currentTour.getName());
        	
        	continueCurrBtn.setVisibility(View.VISIBLE);
        	reviewCurrBtn.setVisibility(View.VISIBLE);
        	
        	Log.d(TAG, "Set currently showed tour: " + currentTour);
        	
        	// Set buttons behaviours
        	continueCurrBtn.setOnClickListener(new OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			Log.d(TAG, "Click on Continue Tour");
        			// TODO set continue tour behavior
        			Intent intent = new Intent(getApplicationContext(), TourDetailsActivity.class);
        			intent.putExtra(StorageDBHelper.TOUR_ID, currentTourId);
        			intent.putExtra("NAVIGATION", true);
        			startActivity(intent);
        		}
        	});

        	reviewCurrBtn.setOnClickListener(new OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			Log.d(TAG, "Click on Review Tour");
        			Intent intent = new Intent(getApplicationContext(), TourDetailsActivity.class);
        			intent.putExtra(StorageDBHelper.TOUR_ID, currentTourId);
        			startActivity(intent);
        		}
        	});
        } else {
        	
        	Log.d(TAG, "No current tour");
        	
        	continueCurrBtn.setVisibility(View.GONE);
        	reviewCurrBtn.setVisibility(View.GONE);
        	
        	// Set current tour name to default
        	currTourNameView.setText(R.string.current_tour_default_text);
        }
		
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


	@Override
	protected void onRestart() {
		super.onRestart();
		
		Log.d(TAG, "onRestart called");
		refreshCurrentTourViews();
		Log.d(TAG, "onRestart finished");
	}
       
    
}
