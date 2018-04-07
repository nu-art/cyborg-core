/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	/**
	 * Create a date time picker
	 *
	 * @param activity  The owner activity for the picker
	 * @param timestamp The initial timestamp to display for the date and time
	 * @param listener  The listener to be notified on when process completed.
	 */
	public DateTimePicker(Activity activity, long timestamp, OnDateTimeChangedListener listener) {
		this.activity = activity;
		this.listener = listener;
		calendar.setTime(new Date(timestamp));
	}

	public final long getTimestamp() {
		return calendar.getTime().getTime();
	}

	/**
	 * Show a date and time pickers
	 */
	public void dateAndTimePicker() {
		datePicker(true);
	}

	/**
	 * Show only a date picker
	 */
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

	/**
	 * Show only a time picker
	 */
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
