package com.example.easybike;

import java.util.List;

import com.example.easybike.model.Stop;
import com.example.easybike.model.Tour;

public interface TourDetailsActivityInterface {

	public Tour getDisplayedTour();

	public List<Stop> getStopsOfDisplayedTour();
	
	public void refreshData();
	
	public void setCurrentTour(Tour tour);
	
	public void setCompletedTour(Tour tour);
	
	public void openNavigation();
}
