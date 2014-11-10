package com.example.support;

import java.util.List;

import android.location.Address;
import android.location.Location;

public interface SearchAddressListener {

	public void searchAddressCorrectResult(List<Address> result);
	
	public void searchAddressWrongResult(String message);
}
