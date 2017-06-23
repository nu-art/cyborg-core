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

package com.nu.art.cyborg.core.modules;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

import java.util.HashMap;
import java.util.Map;

@ModuleDescriptor(usesPermissions = {})
public class ThreadsModule
		extends CyborgModule {

	/**
	 * This is only for a quick one time action... if you have a bulk of action to perform, or multiple individual
	 * method, then create your own damn thread key! <br>
	 * <br>
	 * <b>Use this wisely</b>
	 */
	public static final String Secondary = "secondary";

	public static final String MainThread = "main";

	private final Map<String, Looper> loopers = new HashMap<>();

	private final Map<String, Handler> defaultHandlers = new HashMap<>();

	@Override
	protected void init() {
		loopers.put(MainThread, Looper.getMainLooper());
	}

	public final Handler getDefaultHandler(String threadName) {
		Handler handler = defaultHandlers.get(threadName);
		if (handler == null || !handler.getLooper().getThread().isAlive()) {
			handler = new Handler(getLooper(threadName));
			defaultHandlers.put(threadName, handler);
		}
		return handler;
	}

	private Looper getLooper(String threadName) {
		Looper looper = loopers.get(threadName);
		if (looper == null || !looper.getThread().isAlive()) {
			HandlerThread thread = new HandlerThread(threadName);
			thread.start();
			looper = thread.getLooper();
			loopers.put(threadName, looper);
		}
		return looper;
	}
}
