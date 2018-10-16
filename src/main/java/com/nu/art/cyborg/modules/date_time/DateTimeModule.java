package com.nu.art.cyborg.modules.date_time;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * Created by matankoby on 6/25/18.
 */

public class DateTimeModule
	extends CyborgModule {

	@Override
	protected void init() {}

	final void onDateTimeChanged() {
		dispatchGlobalEvent("Date-Time changed", DateTimeChangedListener.class, new Processor<DateTimeChangedListener>() {
			@Override
			public void process(DateTimeChangedListener dateTimeChangedListener) {
				dateTimeChangedListener.onDateTimeChanged();
			}
		});
	}

	public interface DateTimeChangedListener {

		void onDateTimeChanged();
	}
}
