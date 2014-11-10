package com.example.easybike;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.easybike.model.StorageDBHelper;
import com.example.easybike.model.Tour;

public class ToursAdapter extends BaseAdapter {
	
	private final static String TAG = ToursAdapter.class.getSimpleName();

	private List<Tour> tours;
	private LayoutInflater inflater;
	private Activity activity;
	
	public ToursAdapter (Activity activity, List<Tour> tours) {
		this.tours = tours;
		if (this.tours == null)
			this.tours = new ArrayList<Tour>();
		
		this.activity = activity;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Log.d(TAG, "ToursAdapter created");
	}
	
	@Override
	public int getCount() {
		Log.d(TAG, "getCount executed");
		return tours.size();
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem executed with position "+ position);
		return tours.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId executed with position "+ position);
		return tours.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.d(TAG, "getView called with position " + position);
		
		View view = convertView;
		final Tour tour = tours.get(position);
		
		if (view == null)
			view = inflater.inflate(R.layout.tours_review_cell, parent, false);

		ImageView image = (ImageView) view.findViewById(R.id.tour_image);
		TextView name = (TextView) view.findViewById(R.id.tour_name);
		
		name.setText(tour.getName());
		
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Click on tour " + tour.getName() + " id: "+ tour.getId());
				Intent intent = new Intent(activity.getApplicationContext(), TourDetailsActivity.class);
				intent.putExtra(StorageDBHelper.TOUR_ID, tour.getId());
				activity.startActivity(intent);
			}
		});
		
		Log.d(TAG, "getView finished with position " + position);
		
		return view;
	}

	public void setData (List<Tour> tours) {
		Log.d(TAG, "setData executed");
		this.tours = tours;
	}
}
