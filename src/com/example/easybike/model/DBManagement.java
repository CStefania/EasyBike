package com.example.easybike.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.support.DirectionsDownloadListener;
import com.example.support.DirectionsDownloadTask;

public class DBManagement {

	private final static String TAG = DBManagement.class.getSimpleName();
	private static DBManagement dbManag = null;

	/*
	 * private static final String WHERE_DATE_IS_BETWEEN =
	 * StorageEntryList.DATE_TIME + ">=? AND " + StorageEntryList.DATE_TIME +
	 * "<=?"; private static final String ORDER_BY_DATE =
	 * StorageEntryList.DATE_TIME;
	 * 
	 * private static final String WHERE_DATE_IS_BEFORE =
	 * StorageEntryList.DATE_TIME + "<?"; private static final String
	 * WHERE_DATE_IS_AFTER = StorageEntryList.DATE_TIME + ">?"; private static
	 * final String AND_ID_IS_NOT_EQUAL = " AND " + StorageEntryList.ID + "<>?";
	 * private static final String WHERE_YEAR_IS_EQUAL = StorageEntryList.YEAR +
	 * "=?";
	 */

	private static final String WHERE_ID_IS_EQUAL = StorageDBHelper.ID + "=?";
	private static final String WHERE_ID_IS_NOT_EQUAL = StorageDBHelper.ID
			+ "<>?";
	private static final String WHERE_STATUS_IS_EQUAL = StorageDBHelper.STATUS
			+ "=?";
	private static final String WHERE_TOUR_ID_IS_EQUAL = StorageDBHelper.TOUR_ID
			+ "=?";
	private static final String WHERE_ORDER_IN_TOUR_MORE_THAN = StorageDBHelper.ORDER_IN_TOUR
			+ ">?";
	private static final String WHERE_ORDER_IN_TOUR_EQUAL_TO = StorageDBHelper.ORDER_IN_TOUR
			+ "=?";

	private SQLiteDatabase database;
	private StorageDBHelper dbHelper;
	private final Context context;

	public static DBManagement getInstance(Context context) {
		if (dbManag == null)
			dbManag = new DBManagement(context);

		return dbManag;
	}

	private DBManagement(Context context) {
		this.context = context;
		dbHelper = new StorageDBHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	// Returns new tour id
	public int addOrUpdateTour(Tour tour, int id) {
		return addOrUpdateTour(tour, Integer.toString(id));
	}

	// Returns new tour id
	public int addOrUpdateTour(Tour tour, String id) {

		int newId;

		Log.d(TAG, "Adding tour to db, setting all values: " + tour);

		open();

		ContentValues values = new ContentValues();
		values.put(StorageDBHelper.NAME, tour.getName());
		values.put(StorageDBHelper.STATUS, tour.getStatus().name());
		values.put(StorageDBHelper.TOT_M, tour.getTotalDistance());
		values.put(StorageDBHelper.TOT_TIME, tour.getTotalTime());
		values.put(StorageDBHelper.N_STOPS, tour.getTotalStops());
		values.put(StorageDBHelper.LEFT_M, tour.getRemainingDistance());
		values.put(StorageDBHelper.LEFT_TIME, tour.getRemainingTime());
		values.put(StorageDBHelper.LEFT_STOPS, tour.getRemainingStops());

		if (tour.getStartTime() != null)
			values.put(StorageDBHelper.START_TIME, tour.getStartTime()
					.getTime());

		if (tour.getCompletionTime() != null)
			values.put(StorageDBHelper.COMPLETION_TIME, tour
					.getCompletionTime().getTime());

		Log.d(TAG, "All values setted, inserting in database");

		// If "id" arg is setted then the user is updating, if id=null is
		// inserting
		if (id != null) {
			newId = Integer.parseInt(id);

			String[] whereArgs = { id };
			if (database.update(StorageDBHelper.TABLE_TOUR, values,
					WHERE_ID_IS_EQUAL, whereArgs) < 0)
				Log.d(TAG, "Couldn't update tour id=" + id);
			else
				Log.d(TAG, "Updated tour id=" + id);
		} else {
			newId = Integer.valueOf((int) database.insert(
					StorageDBHelper.TABLE_TOUR, null, values));

			if (newId < 0)
				Log.d(TAG, "Couldn't insert tour");
			else
				Log.d(TAG, "New tour inserted in database with id: " + newId);
		}

		close();

		// Check if the modification was to set the status to completed
		// also all the stops should be set as completed
		if (tour.getStatus().equals(Status.COMPLETED)) {
			List<Stop> stops = getStopsOfTour(tour);

			for (Stop stop : stops) {

				if (!stop.isCompleted()) {
					stop.setCompleted(true);
					updateStop(stop);
				}
			}
		}

		return newId;

	}


	public List<Tour> getTours(Status status) {
		List<Tour> tours = new ArrayList<Tour>();
		Cursor cursor;

		open();

		if (status != null) {
			String[] selectionArgs = { status.toString() };
			cursor = database.query(StorageDBHelper.TABLE_TOUR, null,
					WHERE_STATUS_IS_EQUAL, selectionArgs, null, null,
					StorageDBHelper.NAME);
		} else
			cursor = database.query(StorageDBHelper.TABLE_TOUR, null, null,
					null, null, null, StorageDBHelper.NAME);

		// Check if database is empty
		if (!cursor.moveToFirst()) {
			Log.d(TAG, "No tours in database for status " + status);
			return tours;
		}

		while (!cursor.isAfterLast()) {
			Tour tour = cursorToTour(cursor);
			tours.add(tour);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return tours;
	}

	public Tour getTourFromId(int id) {

		Log.d(TAG, "Requested tour with id: " + id);

		Tour tour;
		open();
		Cursor cursor = database.query(StorageDBHelper.TABLE_TOUR, null,
				StorageDBHelper.ID + " = " + id, null, null, null, null);
		if (cursor.moveToFirst())
			tour = cursorToTour(cursor);
		else
			tour = null;

		cursor.close();
		close();

		Log.d(TAG, "Retrieved tour: " + tour);

		return tour;
	}

	private void updateTourStats(Tour tour) {

		Log.d(TAG, "Updating stats of tour: " + tour);

		List<Stop> stops = getStopsOfTour(tour);

		long totalTime = 0;
		long totalDistance = 0;
		long remainingTime = 0;
		long remainingDistance = 0;
		int remainingStops = 0;

		for (Stop stop : stops) {
			totalTime += stop.getTimeFromPrevStop();
			totalDistance += stop.getDistanceFromPrevStop();
			
			if (!stop.isCompleted()) {
				remainingTime += stop.getTimeFromPrevStop();
				remainingDistance += stop.getDistanceFromPrevStop();
				remainingStops++;
			}				
		}

		tour.setTotalStops(stops.size());
		tour.setTotalTime(totalTime);
		tour.setTotalDistance(totalDistance);
		tour.setRemainingTime(remainingTime);
		tour.setRemainingDistance(remainingDistance);
		tour.setRemainingStops(remainingStops);

		addOrUpdateTour(tour, tour.getId());

		Log.d(TAG, "Updated stats of tour: " + tour);

	}

	private Tour cursorToTour(Cursor cursor) {

		int id = cursor.getInt(cursor.getColumnIndex(StorageDBHelper.ID));
		String name = cursor.getString(cursor
				.getColumnIndex(StorageDBHelper.NAME));
		Status status = Status.valueOf(cursor.getString(cursor
				.getColumnIndex(StorageDBHelper.STATUS)));
		long totalDistance = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.TOT_M));
		long totalTime = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.TOT_TIME));
		int totalStops = cursor.getInt(cursor
				.getColumnIndex(StorageDBHelper.N_STOPS));
		long remainingDistance = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.LEFT_M));
		long remainingTime = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.LEFT_TIME));
		int remainingStops = cursor.getInt(cursor
				.getColumnIndex(StorageDBHelper.LEFT_STOPS));
		Date startTime = new Date(cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.START_TIME)));
		Date completionTime = new Date(cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.COMPLETION_TIME)));

		Tour tour = new Tour(name, status, totalDistance, totalTime,
				totalStops, startTime, completionTime, remainingTime,
				remainingDistance, remainingStops);
		tour.setId(id);

		return tour;
	}

	public List<Stop> getStopsOfTour(Tour tour) {
		List<Stop> stops = new ArrayList<Stop>();
		Cursor cursor;

		if (tour == null)
			return null;

		open();

		String[] selectionArgs = { Integer.toString(tour.getId()) };
		cursor = database.query(StorageDBHelper.TABLE_STOP, null,
				WHERE_TOUR_ID_IS_EQUAL, selectionArgs, null, null,
				StorageDBHelper.ORDER_IN_TOUR);

		// Check if database is empty
		if (!cursor.moveToFirst()) {
			Log.d(TAG, "No stops in database for tour " + tour);
			return stops;
		}

		while (!cursor.isAfterLast()) {
			Stop stop = cursorToStop(cursor);
			stops.add(stop);
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return stops;
	}

	private Stop cursorToStop(Cursor cursor) {

		int id = cursor.getInt(cursor.getColumnIndex(StorageDBHelper.ID));
		int tourId = cursor.getInt(cursor
				.getColumnIndex(StorageDBHelper.TOUR_ID));
		int orderInTour = cursor.getInt(cursor
				.getColumnIndex(StorageDBHelper.ORDER_IN_TOUR));
		String address = cursor.getString(cursor
				.getColumnIndex(StorageDBHelper.ADDRESS));
		double gpsCoordLat = cursor.getDouble(cursor
				.getColumnIndex(StorageDBHelper.GPS_COORD_LAT));
		double gpsCoordLng = cursor.getDouble(cursor
				.getColumnIndex(StorageDBHelper.GPS_COORD_LNG));
		boolean completed = false;
		if (cursor.getInt(cursor.getColumnIndex(StorageDBHelper.COMPLETED)) > 0)
			completed = true;
		long timeFromPrevStop = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.TIME_PREV_STOP));
		long distanceFromPrevStop = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.DISTANCE_PREV_STOP));
		long altitudeFromPrevStop = cursor.getLong(cursor
				.getColumnIndex(StorageDBHelper.ALTITUTE_PREV_STOP));

		Stop stop = new Stop(tourId, orderInTour, address, gpsCoordLat,
				gpsCoordLng, completed, timeFromPrevStop, distanceFromPrevStop,
				altitudeFromPrevStop);
		stop.setId(id);

		Log.d(TAG, "Retrieved from db stop:" + stop);

		return stop;
	}

	public void updateStop(Stop stop) {
		open();
		privateUpdateStop(stop);
		close();

		updateTourStats(getTourFromId(stop.getTourId()));
	}

	private void privateUpdateStop(Stop stop) {

		Log.d(TAG, "Updating stop id: " + stop.getId());

		String[] selectionArgs = { Integer.toString(stop.getId()) };
		ContentValues values = new ContentValues();
		values.put(StorageDBHelper.ADDRESS, stop.getAddress());
		values.put(StorageDBHelper.GPS_COORD_LAT, stop.getGpsCoordLat());
		values.put(StorageDBHelper.GPS_COORD_LNG, stop.getGpsCoordLng());
		values.put(StorageDBHelper.ID, stop.getId());
		values.put(StorageDBHelper.ORDER_IN_TOUR, stop.getOrderInTour());
		values.put(StorageDBHelper.TOUR_ID, stop.getTourId());
		values.put(StorageDBHelper.COMPLETED, stop.isCompleted());
		values.put(StorageDBHelper.TIME_PREV_STOP, stop.getTimeFromPrevStop());
		values.put(StorageDBHelper.DISTANCE_PREV_STOP,
				stop.getDistanceFromPrevStop());
		values.put(StorageDBHelper.ALTITUTE_PREV_STOP,
				stop.getAltitudeFromPrevStop());

		database.update(StorageDBHelper.TABLE_STOP, values, WHERE_ID_IS_EQUAL,
				selectionArgs);

		Log.d(TAG, "Updated stop id: " + stop.getId() + " to values: " + stop);
	}

	public void addStopToTour(Stop stop, DirectionsDownloadListener listener) {

		Log.d(TAG, "Adding stop to db, setting all values: " + stop);

		ContentValues values = new ContentValues();
		values.put(StorageDBHelper.TOUR_ID, stop.getTourId());
		values.put(StorageDBHelper.ORDER_IN_TOUR, stop.getOrderInTour());
		values.put(StorageDBHelper.ADDRESS, stop.getAddress());
		values.put(StorageDBHelper.GPS_COORD_LAT, stop.getGpsCoordLat());
		values.put(StorageDBHelper.GPS_COORD_LNG, stop.getGpsCoordLng());
		values.put(StorageDBHelper.COMPLETED, stop.isCompleted());
		values.put(StorageDBHelper.TIME_PREV_STOP, stop.getTimeFromPrevStop());
		values.put(StorageDBHelper.DISTANCE_PREV_STOP,
				stop.getDistanceFromPrevStop());
		values.put(StorageDBHelper.ALTITUTE_PREV_STOP,
				stop.getAltitudeFromPrevStop());

		Log.d(TAG, "All values setted, inserting in database");

		open();

		// If "id" arg is setted then the user is updating, if id=null is
		// inserting
		int newStopId = (int) database.insert(StorageDBHelper.TABLE_STOP, null,
				values);
		if (newStopId < 0)
			Log.d(TAG, "Couldn't insert stop");
		else
			Log.d(TAG, "New stop inserted in database");
		
		// if there's a previous stop, lauch asynch task for time,
		// distance and altitude data between prev and current stop
		stop.setId(newStopId);
		if (stop.getOrderInTour() > 1) {
			Log.d(TAG, "Stop has order " + stop.getOrderInTour()
					+ " launching asynch task for directions");
			Stop[] params = new Stop[2];
			params[0] = getPrivatePreviousStop(stop);
			params[1] = stop;
			(new DirectionsDownloadTask(listener)).execute(params);
		}

		close();

		updateTourStats(getTourFromId(stop.getTourId()));
	}

	private Stop getPrivatePreviousStop(Stop stop) {

		Log.d(TAG, "Requested stop previous of stop: " + stop);

		Stop prevStop;

		String[] selectionArgs = { Integer.toString(stop.getTourId()),
				Integer.toString(stop.getOrderInTour() - 1) };

		Cursor cursor = database
				.query(StorageDBHelper.TABLE_STOP, null, WHERE_TOUR_ID_IS_EQUAL
						+ " AND " + WHERE_ORDER_IN_TOUR_EQUAL_TO,
						selectionArgs, null, null, null);

		if (cursor.moveToFirst())
			prevStop = cursorToStop(cursor);
		else
			prevStop = null;

		cursor.close();

		Log.d(TAG, "Retrieved previous stop: " + prevStop);

		return prevStop;
	}

	private Stop getPrivateNextStop(Stop stop) {

		Log.d(TAG, "Requested stop next of stop: " + stop);

		Stop nextStop;

		String[] selectionArgs = { Integer.toString(stop.getTourId()),
				Integer.toString(stop.getOrderInTour() + 1) };

		Cursor cursor = database
				.query(StorageDBHelper.TABLE_STOP, null, WHERE_TOUR_ID_IS_EQUAL
						+ " AND " + WHERE_ORDER_IN_TOUR_EQUAL_TO,
						selectionArgs, null, null, null);

		if (cursor.moveToFirst())
			nextStop = cursorToStop(cursor);
		else
			nextStop = null;

		cursor.close();

		Log.d(TAG, "Retrieved next stop: " + nextStop);

		return nextStop;
	}

	public void removeStopFromTour(Stop stop, DirectionsDownloadListener listener) {
		String[] selectionArgs = { Integer.toString(stop.getId()) };

		Log.d(TAG, "Removing stop id: " + stop.getId() + " from tour id: "
				+ stop.getTourId());

		open();
		
		Stop previousStop = getPrivatePreviousStop(stop);

		database.delete(StorageDBHelper.TABLE_STOP, WHERE_ID_IS_EQUAL,
				selectionArgs);

		Log.d(TAG, "Removed stop id: " + stop.getId() + " from tour id: "
				+ stop.getTourId());
		Log.d(TAG, "removeStopFromTour: Updating next stops order in tour");

		// Reorder remaining stops
		String[] selectionArgs2 = { Integer.toString(stop.getTourId()),
				Integer.toString(stop.getOrderInTour()) };

		// Get all affected rows (after the removed one in order)
		Cursor cursor = database.query(StorageDBHelper.TABLE_STOP, null,
				WHERE_TOUR_ID_IS_EQUAL + " AND "
						+ WHERE_ORDER_IN_TOUR_MORE_THAN, selectionArgs2, null,
				null, StorageDBHelper.ORDER_IN_TOUR);

		// Check if database is empty
		if (!cursor.moveToFirst()) {
			Log.d(TAG,
					"removeStopFromTour: No more stops in database for tour id: "
							+ stop.getTourId());
		} else {
			
			// Update each entry with its order in tour minus one
			while (!cursor.isAfterLast()) {
				Stop stop2 = cursorToStop(cursor);
				stop2.setOrderInTour(stop2.getOrderInTour() - 1);
				
				// if it's the new first stop, the directions data
				// are simply resetted to 0
				if (stop2.getOrderInTour() == 1) {
					stop2.setTimeFromPrevStop(0);
					stop2.setDistanceFromPrevStop(0);
				}
					
				privateUpdateStop(stop2);
				
				// if not the first stop, launch asynch task for time,
				// distance and altitude data between prev and current stop
				if (stop2.getOrderInTour() > 1) {
					Log.d(TAG, "removeStopFromTour: Stop has order " + stop2.getOrderInTour()
							+ " launching asynch task for directions");
					Stop[] params = new Stop[2];
					params[0] = previousStop;
					params[1] = stop2;
					(new DirectionsDownloadTask(listener)).execute(params);
				} 
				
				previousStop = stop2;
				
				cursor.moveToNext();
			}
		}

		cursor.close();

		close();

		Log.d(TAG, "removeStopFromTour: Removed stop id: " + stop.getId() + " from tour id: "
				+ stop.getTourId());

		updateTourStats(getTourFromId(stop.getTourId()));
	}

	public void moveUpStop(Stop stop, DirectionsDownloadListener listener) {

		Log.d(TAG, "Moving up stop: " + stop);

		moveStop(stop, -1, listener);

		Log.d(TAG, "Moved up stop: " + stop);
	}

	public void moveDownStop(Stop stop, DirectionsDownloadListener listener) {

		Log.d(TAG, "Moving down stop: " + stop);

		moveStop(stop, +1, listener);

		Log.d(TAG, "Moved down stop: " + stop);
	}

	private void moveStop(Stop stop, int move, DirectionsDownloadListener listener) {

		int movingStopOrderUpdatingCenter = 0;
		if (move < 0) // move Up
			movingStopOrderUpdatingCenter = stop.getOrderInTour();
		else if (move > 0) // move Down
			movingStopOrderUpdatingCenter = stop.getOrderInTour() + 1;
		
		open();

		// Update stop's order
		stop.setOrderInTour(stop.getOrderInTour() + move);
		privateUpdateStop(stop);

		// Exchange value with the other stop that now has same order
		String[] selectionArgs = { Integer.toString(stop.getTourId()),
				Integer.toString(stop.getOrderInTour()),
				Integer.toString(stop.getId()) };

		// Get all stops with same order in affected tour
		Cursor cursor = database.query(StorageDBHelper.TABLE_STOP, null,
				WHERE_TOUR_ID_IS_EQUAL + " AND " + WHERE_ORDER_IN_TOUR_EQUAL_TO
						+ " AND " + WHERE_ID_IS_NOT_EQUAL, selectionArgs, null,
				null, StorageDBHelper.ORDER_IN_TOUR);

		// Check if database is empty
		if (!cursor.moveToFirst()) {
			Log.d(TAG, "Error in moving of " + move + " stop: " + stop);
			return;
		} else {
			// Update the affected entry with its new order in tour
			Stop stop2 = cursorToStop(cursor);
			stop2.setOrderInTour(stop2.getOrderInTour() - move);
			privateUpdateStop(stop2);
			cursor.moveToNext();
		}

		cursor.close();
		
		close();
		open();
		
		// launch asynch task for time and distance for the moved stop
		// and the prev and next one if existing
		List<Stop> affectedStops = new ArrayList<Stop>();
		
		Log.d(TAG, "Retrieving affected stops near to movingOrder " + movingStopOrderUpdatingCenter);
		
		String[] selectionArgs2 = { Integer.toString(stop.getTourId()),
				 Integer.toString(movingStopOrderUpdatingCenter), Integer.toString(movingStopOrderUpdatingCenter+1),
				 Integer.toString(movingStopOrderUpdatingCenter-1)};

		// Get all stops with same order in affected tour
		Cursor cursor2 = database.query(StorageDBHelper.TABLE_STOP, null,
				WHERE_TOUR_ID_IS_EQUAL + " AND (" + WHERE_ORDER_IN_TOUR_EQUAL_TO
						+ " OR " + WHERE_ORDER_IN_TOUR_EQUAL_TO + " OR " +
						WHERE_ORDER_IN_TOUR_EQUAL_TO + ")", selectionArgs2, null,
				null, StorageDBHelper.ORDER_IN_TOUR);

		// Check if database is empty
		if (!cursor2.moveToFirst()) {
			Log.d(TAG, "Error in updating distance data of moving " + move + " stop: " + stop);
			return;
		} else {
			while (!cursor2.isAfterLast()) {
				// Add the affected stop
				Stop stop2 = cursorToStop(cursor2);
				affectedStops.add(stop2);
				cursor2.moveToNext();
			}
		}

		cursor2.close();
		
		Log.d(TAG, "Affected stops: " + affectedStops);

		for (Stop affStop: affectedStops) {

			// check that it's not the first stop of the tour
			if (affStop.getOrderInTour() > 1) {
				Log.d(TAG, "Stop has order " + affStop.getOrderInTour()
						+ " launching asynch task for directions");
				Stop[] params = new Stop[2];
				params[0] = getPrivatePreviousStop(affStop);
				params[1] = affStop;
				(new DirectionsDownloadTask(listener)).execute(params);
			} else {
				// if it's the first stop of the tour
				// simply reset time and distance to 0
				affStop.setTimeFromPrevStop(0);
				affStop.setDistanceFromPrevStop(0);
				privateUpdateStop(affStop);
			}
		}
		
		close();
		
		updateTourStats(getTourFromId(stop.getTourId()));
	}

	public static String formatTime(long time) {
		
		if (time == 0)
			return "-";

		long days, hours, minutes;

		days = time / (3600 * 24);
		hours = time / 3600;
		minutes = (long) (Math.ceil((time / 60) - (hours * 60)));

		StringBuffer timeStr = new StringBuffer();
		
		if (days == 1)
			timeStr.append(days + " day ");
		else if (days > 1)
			timeStr.append(days + " days ");
		
		if (hours == 1)
			timeStr.append(hours + " hour ");
		else if (hours > 1 || timeStr.length() > 0)
			timeStr.append(hours + " hours ");
		
		timeStr.append(minutes + " min");
		
		return timeStr.toString();
	}

	public static String formatDistance(long distance) {

		if (distance < 1)
			return "-";

		long km, m;

		km = distance / 1000;
		m = distance - (km * 1000);

		if (km < 1)
			return m + " m";
		else
			return km + " km " + m + " m";
	}

}
