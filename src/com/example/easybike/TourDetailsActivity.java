package com.example.easybike;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Status;
import com.example.easybike.model.Stop;
import com.example.easybike.model.StorageDBHelper;
import com.example.easybike.model.Tour;

public class TourDetailsActivity extends ActionBarActivity implements
		ActionBar.TabListener, TourDetailsActivityInterface {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static final String TAG = TourDetailsActivity.class.getSimpleName();

	private int tourId = 0;
	private boolean openNavigation = false;
	private Tour tour;
	private List<Stop> stops;
	private DBManagement dbManag;

	private ActionBar actionBar;

	// Keep a reference to all managed tabs fragments
	private GeneralInfoFragment generalInfoFrag = null;
	private StopListFragment stopListFrag = null;
	private StopsMapFragment stopsMapFrag = null;
	private DiaryFragment diaryFrag = null;
	
	private boolean refreshDataRequestedTab1, refreshDataRequestedTab2, refreshDataRequestedTab3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate called");
		
		setContentView(R.layout.activity_tour_details);
		
		refreshDataRequestedTab1 = false;
		refreshDataRequestedTab2 = false;
		refreshDataRequestedTab3 = true;

		Intent intent = getIntent();
		if (intent != null) {
			tourId = (int) intent.getIntExtra(StorageDBHelper.TOUR_ID, 0);
			openNavigation = intent.getBooleanExtra("NAVIGATION", false);
		}

		dbManag = DBManagement.getInstance(getApplicationContext());
		tour = dbManag.getTourFromId(tourId);
		stops = dbManag.getStopsOfTour(tour);

		Log.d(TAG, "Currently showing details of tour: " + tour);

		// Set up the action bar.
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		if (tour.getName() != null)
			actionBar.setTitle(tour.getName() + " " + getString(R.string.tour));
		else
			actionBar.setTitle("... " + getString(R.string.tour));

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.selectTab(actionBar.getTabAt(position));
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setIcon(mSectionsPagerAdapter.getPageIcon(i))
					.setTabListener(this));
		}
		
		// Set back button in action bar
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		if (openNavigation)
			openNavigation();
		
		Log.d(TAG, "onCreate finished");
	}

	public void openNavigation() {
		Log.d(TAG, "openNavigation called");
		refreshDataRequestedTab3 = true;
		actionBar.selectTab(actionBar.getTabAt(2));	
		refreshSelectedTab(actionBar.getSelectedTab().getPosition());
		Log.d(TAG, "openNavigation finished");
	}

	public Tour getDisplayedTour() {
		Log.d(TAG, "getDisplayedTour executed");
		return tour;
	}

	public List<Stop> getStopsOfDisplayedTour() {
		Log.d(TAG, "getStopsOfDisplayedTour executed");
		return stops;
	}

	public void refreshData() {

		Log.d(TAG, "refreshData called");

		tour = dbManag.getTourFromId(tour.getId());
		actionBar.setTitle(tour.getName() + " " + getString(R.string.tour));
		stops = dbManag.getStopsOfTour(tour);
		
		refreshDataRequestedTab1 = true;
		refreshDataRequestedTab2 = true;
		refreshDataRequestedTab3 = true;
		
		refreshSelectedTab(actionBar.getSelectedTab().getPosition());
		
		Log.d(TAG, "refreshData finished");

	}
	
	private void refreshSelectedTab (int tabPosition) {
		
		Log.d(TAG, "refreshSelectedTab called");
		
		switch (tabPosition) {
		case 0: {
			if (stopsMapFrag != null && tour.getStatus().equals(Status.IN_PROGRESS))
				stopsMapFrag.stopNavigation();
			
			if (generalInfoFrag != null && refreshDataRequestedTab1) {
				Log.d(TAG, "refreshSelectedTab calling refreshData on generalInfoFrag fragment");
				generalInfoFrag.refreshData();
				refreshDataRequestedTab1 = false;
			}
			break;
		}
		case 1: {
			if (stopsMapFrag != null && tour.getStatus().equals(Status.IN_PROGRESS))
				stopsMapFrag.stopNavigation();
			
			if (stopListFrag != null && refreshDataRequestedTab2) {
				Log.d(TAG, "refreshSelectedTab calling refreshData on stops list fragment");
				stopListFrag.refreshData();
				refreshDataRequestedTab2 = false;
			}
			break;
		}
		case 2: {
			if (stopsMapFrag != null) {
				if (tour.getStatus().equals(Status.IN_PROGRESS))
					stopsMapFrag.startNavigation();
				
				if (refreshDataRequestedTab3) {
					Log.d(TAG, "refreshSelectedTab calling refreshData on map fragment");
					stopsMapFrag.refreshData();
					refreshDataRequestedTab3 = false;
				}
			}
			break;
		}
		}
		
		Log.d(TAG, "refreshSelectedTab finished");
	}

	public void setCurrentTour(Tour tour) {
		
		Log.d(TAG, "setCurrentTour called with tour: " + tour);
		
		SharedPreferences pref = getSharedPreferences(
				SplashScreenActivity.PREFERENCES, 0);

		// Get current tour if existing and change it to completed
		int currentTourId = pref.getInt(SplashScreenActivity.CURRENT_TOUR, 0);
		Tour currentTour = dbManag.getTourFromId(currentTourId);

		if (currentTour != null) {
			Log.d(TAG, "Setting as completed current tour: " + currentTour);
			currentTour.setStatus(Status.COMPLETED);
			currentTour.setCompletionTime(new Date());
			dbManag.addOrUpdateTour(currentTour, currentTourId);
		}

		// Set the new current tour
		tour.setStatus(Status.IN_PROGRESS);
		tour.setStartTime(new Date());
		dbManag.addOrUpdateTour(tour, tour.getId());
		pref.edit().putInt(SplashScreenActivity.CURRENT_TOUR, tour.getId())
				.apply();
		
		Log.d(TAG, "setCurrentTour finished, set as current tour: " + tour);
	}

	public void setCompletedTour(Tour tour) {
		
		Log.d(TAG, "setCompletedTour called with tour: " + tour);
		
		SharedPreferences pref = getSharedPreferences(
				SplashScreenActivity.PREFERENCES, 0);

		// Get current tour if existing and change it to completed
		int currentTourId = pref.getInt(SplashScreenActivity.CURRENT_TOUR, 0);

		if (currentTourId == tour.getId()) {
			Tour currentTour = dbManag.getTourFromId(currentTourId);
			currentTour.setStatus(Status.COMPLETED);
			currentTour.setCompletionTime(new Date());
			dbManag.addOrUpdateTour(currentTour, currentTourId);
			Log.d(TAG, "Setted as completed tour: " + tour);
		}

		// Set the new current tour to none
		pref.edit().putInt(SplashScreenActivity.CURRENT_TOUR, 0).apply();
		Log.d(TAG, "setCompletedTour finished with tour: " + tour);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tour_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			super.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		Log.d(TAG, "onTabSelected called with tab " + tab.getPosition());
		mViewPager.setCurrentItem(tab.getPosition());
		
		refreshSelectedTab(tab.getPosition());

		Log.d(TAG, "onTabSelected finished with tab " + tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			switch (position) {
			case 0: {
				if (generalInfoFrag == null)
					generalInfoFrag = new GeneralInfoFragment();
				return generalInfoFrag;
			}
			case 1: {
				if (stopListFrag == null)
					stopListFrag = new StopListFragment();
				return stopListFrag;
			}
			case 2: {
				if (stopsMapFrag == null)
					stopsMapFrag = new StopsMapFragment();

				return stopsMapFrag;
			}
			/*case 3: {
				if (diaryFrag == null)
					diaryFrag = new DiaryFragment();
				return diaryFrag;
			}*/
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			/*case 3:
				return getString(R.string.title_section4).toUpperCase(l);*/
			}
			return null;
		}

		public int getPageIcon(int position) {
			switch (position) {
			case 0:
				return R.drawable.ic_home;
			case 1:
				return R.drawable.ic_list;
			case 2:
				return R.drawable.ic_map_marker;
			/*case 3:
				return R.drawable.ic_book;*/
			}
			return 0;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Object fragment = super.instantiateItem(container, position);
			Log.d("POSITION", "position " + position);
			switch (position) {
			case 0:
				generalInfoFrag = (GeneralInfoFragment) fragment;
				break;
			case 1:
				stopListFrag = (StopListFragment) fragment;
				break;
			case 2:
				stopsMapFrag = (StopsMapFragment) fragment;
				break;
			/*case 3:
				diaryFrag = (DiaryFragment) fragment;
				break;*/
			}
			return fragment;
		}

	}

}
