package com.example.easybike.model;

import com.google.android.gms.maps.model.LatLng;

public class Stop {

	private int id;
	private int tourId;
	private int orderInTour;
	private String address;
	private double gpsCoordLat;
	private double gpsCoordLng;
	private boolean completed;
	
	private long timeFromPrevStop;
	private long distanceFromPrevStop;
	private long altitudeFromPrevStop;
	
	public Stop() {
	};

	public Stop(int tourId, int orderInTour, String address,
			double gpsCoordLat, double gpsCoordLng, boolean completed,
			long timeFromPrevStop, long distanceFromPrevStop,
			long altitudeFromPrevStop) {
		super();
		this.tourId = tourId;
		this.orderInTour = orderInTour;
		this.address = address;
		this.gpsCoordLat = gpsCoordLat;
		this.gpsCoordLng = gpsCoordLng;
		this.completed = completed;
		this.timeFromPrevStop = timeFromPrevStop;
		this.distanceFromPrevStop = distanceFromPrevStop;
		this.altitudeFromPrevStop = altitudeFromPrevStop;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTourId() {
		return tourId;
	}

	public void setTourId(int tourId) {
		this.tourId = tourId;
	}

	public int getOrderInTour() {
		return orderInTour;
	}

	public void setOrderInTour(int orderInTour) {
		this.orderInTour = orderInTour;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getGpsCoordLat() {
		return gpsCoordLat;
	}

	public void setGpsCoordLat(double gpsCoordLat) {
		this.gpsCoordLat = gpsCoordLat;
	}

	public double getGpsCoordLng() {
		return gpsCoordLng;
	}

	public void setGpsCoordLng(double gpsCoordLng) {
		this.gpsCoordLng = gpsCoordLng;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public LatLng getLatLng () {
		return new LatLng(gpsCoordLat, gpsCoordLng);
	}

	public long getTimeFromPrevStop() {
		return timeFromPrevStop;
	}

	public void setTimeFromPrevStop(long timeFromPrevStop) {
		this.timeFromPrevStop = timeFromPrevStop;
	}

	public long getDistanceFromPrevStop() {
		return distanceFromPrevStop;
	}

	public void setDistanceFromPrevStop(long distanceFromPrevStop) {
		this.distanceFromPrevStop = distanceFromPrevStop;
	}

	public long getAltitudeFromPrevStop() {
		return altitudeFromPrevStop;
	}

	public void setAltitudeFromPrevStop(long altitudeFromPrevStop) {
		this.altitudeFromPrevStop = altitudeFromPrevStop;
	}
	
	public String getFormattedTimeFromPreviousStop () {
		return DBManagement.formatTime(timeFromPrevStop); 
	}

	public String getFormattedDistanceFromPreviousStop () {
		return DBManagement.formatDistance(distanceFromPrevStop); 
	}
	
	@Override
	public String toString() {
		return "Stop [id=" + id + ", tourId=" + tourId + ", orderInTour="
				+ orderInTour + ", address=" + address + ", gpsCoordLat="
				+ gpsCoordLat + ", gpsCoordLng=" + gpsCoordLng + ", completed="
				+ completed + ", timeFromPrevStop=" + timeFromPrevStop
				+ ", distanceFromPrevStop=" + distanceFromPrevStop
				+ ", altitudeFromPrevStop=" + altitudeFromPrevStop + "]";
	}

}
