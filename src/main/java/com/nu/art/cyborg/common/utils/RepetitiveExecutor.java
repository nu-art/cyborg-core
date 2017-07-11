package com.nu.art.cyborg.common.utils;

import android.os.Handler;

/**
 * Created by tacb0ss on 11/07/2017.
 */

public class RepetitiveExecutor
		implements Runnable {

	private final Handler handler;

	private final int interval;

	private Runnable toExecute;

	private boolean running = false;

	public RepetitiveExecutor(Handler handler, int interval) {
		this.handler = handler;
		this.interval = interval;
	}

	public final void start(Runnable toExecute) {
		if (running)
			return;

		this.toExecute = toExecute;
		handler.postDelayed(this, interval);
	}

	public final void stop() {
		running = false;
	}

	@Override
	public void run() {
		toExecute.run();
		if (!running)
			return;

		handler.postDelayed(this, interval);
	}
}

