package com.example.easybike;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easybike.model.DBManagement;
import com.example.easybike.model.Tour;

public class ModifyTourDialogFragment extends DialogFragment {

	private final static String TAG = ModifyTourDialogFragment.class
			.getSimpleName();

	private LinearLayout dialogView;
	private EditText tourNameText;
	private TextView errorText;
	private Tour tour;
	private TourDetailsActivityInterface activity;

	public ModifyTourDialogFragment(Tour tour) {
		super();
		this.tour = tour;
		Log.d(TAG, "ModifyTourDialogFragment created");
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateDialog called");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		Log.d(TAG, "Currently showing modify pop up for tour: " + tour);

		try {
			activity = (TourDetailsActivityInterface) getActivity();
		} catch (ClassCastException e) {
			Log.e(TAG, "Activity must implement " + TourDetailsActivityInterface.class.getSimpleName(), e);
			return null;
        }

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		dialogView = (LinearLayout) inflater.inflate(
				R.layout.dialog_modify_tour, null);

		// Get all view elements
		tourNameText = (EditText) dialogView.findViewById(R.id.tourNameText);
		errorText = (TextView) dialogView.findViewById(R.id.errorText);

		errorText.setVisibility(View.GONE);

		// Prewrite title if existing
		if (tour.getName() != null) {
			
			Log.d(TAG, "Modifying tour name " + tour.getName());
			
			tourNameText.setText(tour.getName());

			// Set dialog buttons both positive and negative
			builder.setView(dialogView)
			.setPositiveButton(R.string.save,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// later overridden, otherwise it would
					// autodismiss
				}
			})
			.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					Log.d(TAG, "Click on cancel, dismissing dialog");
					dismiss();
				}
			});
		} else {
			
			Log.d(TAG, "Inserting tour name for the first time");
			
			// Set dialog buttons only positive because a title must be inserted
			// So the dialog can't be dismissed without saving
			builder.setView(dialogView)
			.setPositiveButton(R.string.save,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// later overridden, otherwise it would
					// autodismiss
				}
			});
			
		}

		Log.d(TAG, "onCreateDialog finished");
		
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart(); 
		
		Log.d(TAG, "onStart called");
		
		final AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			// Overwrite positive button listener to disable autodismiss
			Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String tourName = tourNameText.getText().toString();

					Log.d(TAG, "Click on Save with tour name: " + tourName + ", "
							+ " lenght: " + tourName.length());

					// Check if inserted name is valid
					if (!isTourNameValid(tourName)) {
						Log.d(TAG, "Tour name is invalid, dialog is not dismissed");
						return;
					} else {
						
						errorText.setVisibility(View.GONE);

						Log.d(TAG, "Tour name is valid, showing are you sure pop up");
						
						// are you sure popup
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

						builder.setTitle(R.string.are_you_sure)
							.setMessage(R.string.modify_tour_popup)
							.setPositiveButton(R.string.save,
									new DialogInterface.OnClickListener() {

										@Override
											public void onClick(DialogInterface dialog, int which) {
											
											Log.d(TAG, "Are you sure pop up: click on Save");
											
											tour.setName(tourNameText.getText().toString());
											DBManagement.getInstance(getActivity())
													.addOrUpdateTour(tour, tour.getId());
											
											Log.d(TAG, "Are you sure pop up: calling refreshData and dismissing after" +
													" updating tour: " + tour);
											
											activity.refreshData();
											d.dismiss();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											Log.d(TAG, "Are you sure pop up: click on Cancel");
											dialog.dismiss();
										}
									})
							.show();
					}

				}
			});
		}
		
		Log.d(TAG, "OnStart finished");
	}
	
	public boolean isTourNameValid (String tourName) {
		if (tourName == null || tourName.isEmpty()) {
			Log.d(TAG, "Tour name null!");
			errorText.setText(R.string.tour_name_is_null);
			errorText.setVisibility(View.VISIBLE);
			return false;
		}
		return true;
	}

}
