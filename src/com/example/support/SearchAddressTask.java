package com.example.support;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

public class SearchAddressTask extends AsyncTask<String, Void, List<Address>> {

	private final static String TAG = SearchAddressTask.class.getSimpleName();

	Context context;
	private SearchAddressListener listener;

	public SearchAddressTask(Context context, SearchAddressListener listener) {
		super();
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected List<Address> doInBackground(String... params) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());

		// Get the current location from the input parameter list

		// Create a list to contain the result address
		List<Address> addresses = null;

		// Get maximum 20 results
		try {
			Log.d(TAG, "Requesting address for name: " + params[0]);
			addresses = geocoder.getFromLocationName(params[0], 20);
		} catch (IOException e1) {
			Log.e(TAG, "IO Exception in getFromLocationName(), query name: " + params[0], e1);
			return null;
			
		} catch (IllegalArgumentException e2) {
			Log.e("LocationSampleActivity", "Illegal argument "
					+ params[0]
					+ " passed to address service", e2);
			return null;
		}
		
		// If the reverse geocode returned an address
		if (addresses != null && addresses.size() > 0) {
			Log.d(TAG, "Found " + addresses.size() + " results for query " + params[0]);
			return addresses;
		} else {
			Log.d(TAG, "No results for " + params[0]);
			return null;
		}

	}

	@Override
	protected void onPostExecute(List<Address> results) {
		super.onPostExecute(results);

		if (results != null)
			listener.searchAddressCorrectResult(results);
		else
			listener.searchAddressWrongResult(null);

	}

}
