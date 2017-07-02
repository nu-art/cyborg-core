package com.nu.art.cyborg.common.utils;

public class BusyState {

	private long timestamp;

	private static final int DelayBetweenClicksInterval = 400;

	private int busyInterval = DelayBetweenClicksInterval;

	public BusyState() {
	}

	public BusyState(int busyInterval) {
		this.busyInterval = busyInterval;
	}

	public boolean canExecute() {
		boolean canExecute = System.currentTimeMillis() - timestamp > busyInterval;
		if (canExecute)
			timestamp = System.currentTimeMillis();

		return canExecute;
	}
}
