/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

