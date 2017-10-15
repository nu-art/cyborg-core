package com.nu.art.cyborg.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tacb0ss on 15/10/2017.
 */

public class DateTimePicker {

	public interface OnDateTimeChangedListener {

		void onDateTimeChanged();
	}

	private OnDateTimeChangedListener listener;
	private final Calendar calendar = Calendar.getInstance();

	private Activity activity;

	public DateTimePicker(Activity activity, long timestamp, OnDateTimeChangedListener listener) {
		this.activity = activity;
		this.listener = listener;
		calendar.setTime(new Date(timestamp));
	}

	public final long getTimestamp() {
		return calendar.getTime().getTime();
	}

	public void dateAndTimePicker() {
		datePicker(true);
	}

	public void datePicker() {
		datePicker(false);
	}

	private void datePicker(final boolean alsoTimePicker) {
		DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				calendar.set(year, monthOfYear, dayOfMonth);

				if (alsoTimePicker)
					timePicker();
				else
					listener.onDateTimeChanged();
			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		datePickerDialog.show();
	}

	public void timePicker() {
		TimePickerDialog timePickerDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
				listener.onDateTimeChanged();
			}
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

		timePickerDialog.show();
	}
}
