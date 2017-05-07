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

package com.nu.art.cyborg.core;

import android.content.Intent;

/**
 * I've been trying really really hard to understand why the hell services are needed in Android, except for the obvious reason of keeping the application
 * process up and running.<br>
 * Any action you would like to perform can be simply and even preferably be a <b>Runnable</b> executed on a thread pool, and NOT in a service! why so you can
 * perform that action independently(of what... everything including services)<br>
 * So use this service to keep your application alive, and do yourself a favor and create a module to handle any action you want to perform.
 */
public final class ApplicationService
		extends CyborgServiceBase {

	/**
	 * Class for clients to access. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder
			extends BaseBinder<ApplicationService> {

		@SuppressWarnings("UnusedDeclaration")
		public ApplicationService getService() {
			return ApplicationService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected BaseBinder createBinder() {
		return new LocalBinder();
	}
}
