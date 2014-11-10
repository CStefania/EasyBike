package com.example.easybike;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class AddressListAdapter extends BaseAdapter {
	
	private final static String TAG = AddressListAdapter.class.getSimpleName();
	
	private List<Address> addresses;
	private Activity activity;
	private LayoutInflater inflater;

	public AddressListAdapter(List<Address> addresses, Activity activity) {
		super();
		this.addresses = addresses;
		this.activity = activity;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Log.d(TAG, "AddressListAdapter created");
	}

	@Override
	public int getCount() {
		Log.d(TAG, "getCount executed");
		return addresses.size();
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem executed with position " + position);
		return addresses.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId executed with position " + position);
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.d(TAG, "getView called with position " + position);
		
		View rootView;
		
		if (convertView != null)
			rootView = convertView;
		else
			rootView = inflater.inflate(R.layout.address_list_cell, parent, false);
		
		Address currAddr = addresses.get(position);
		String addressStr = String.format(
                "%s, %s, %s",
                // If there's a street address, add it
                currAddr.getMaxAddressLineIndex() > 0 ?
                		currAddr.getAddressLine(0) : "",
                // Locality is usually a city
                		currAddr.getLocality(),
                // The country of the address
                		currAddr.getCountryName());
		
		CheckedTextView addressText = (CheckedTextView) rootView.findViewById(R.id.address);
		addressText.setText(addressStr);
		
		Log.d(TAG, "getView finished with position " + position);
				
		return rootView;
	}

}
