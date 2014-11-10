package com.example.easybike.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tour {

	private int id;
	private String name;
	private Status status;
	private long totalDistance;
	private long totalTime;
	private int totalStops;
	private Date startTime;
	private Date completionTime;
	private long remainingTime;
	private long remainingDistance;
	private int remainingStops;
	
	public Tour() {
		this.status = Status.TO_START;
	}
	
	public Tour(String name, Status status, long totalDistance, long totalTime,
			int totalStops, Date startTime, Date completionTime,
			long remainingTime, long remainingDistance, int remainingStops) {
		super();
		this.name = name;
		this.status = status;
		this.totalDistance = totalDistance;
		this.totalTime = totalTime;
		this.totalStops = totalStops;
		this.startTime = startTime;
		this.completionTime = completionTime;
		this.remainingTime = remainingTime;
		this.remainingDistance = remainingDistance;
		this.remainingStops = remainingStops;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(long totalDistance) {
		this.totalDistance = totalDistance;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public int getTotalStops() {
		return totalStops;
	}

	public void setTotalStops(int totalStops) {
		this.totalStops = totalStops;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(Date completionTime) {
		this.completionTime = completionTime;
	}

	public long getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(long remainingTime) {
		this.remainingTime = remainingTime;
	}

	public long getRemainingDistance() {
		return remainingDistance;
	}

	public void setRemainingDistance(long remainingDistance) {
		this.remainingDistance = remainingDistance;
	}

	public int getRemainingStops() {
		return remainingStops;
	}

	public void setRemainingStops(int remainingStops) {
		this.remainingStops = remainingStops;
	}

	@Override
	public String toString() {
		return "Tour [id=" + id + ", name=" + name + ", status=" + status
				+ ", totalKm=" + totalDistance + ", totalTime=" + totalTime
				+ ", totalStops=" + totalStops + ", startTime=" + startTime
				+ ", completionTime=" + completionTime + "]";
	}

}
