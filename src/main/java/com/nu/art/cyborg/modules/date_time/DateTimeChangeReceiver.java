package com.nu.art.cyborg.modules.date_time;

import android.content.Intent;

import com.nu.art.cyborg.core.CyborgReceiver;

import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;

/**
 * Created by matankoby on 6/25/18.
 */

public class DateTimeChangeReceiver
	extends CyborgReceiver<DateTimeModule> {

	protected DateTimeChangeReceiver() {
		super(DateTimeModule.class, ACTION_TIME_CHANGED, ACTION_TIMEZONE_CHANGED, ACTION_DATE_CHANGED);
	}

	@Override
	protected void onReceive(Intent intent, DateTimeModule module) {
		final String action = intent.getAction();

		if (action == null) {
			logWarning("Action was null... ");
			return;
		}
		switch (action) {
			case ACTION_DATE_CHANGED:
			case ACTION_TIME_CHANGED:
			case ACTION_TIMEZONE_CHANGED:
				module.onDateTimeChanged();
				break;
		}
	}
}
