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

package com.nu.art.cyborg.modules.notifications;

import android.content.Intent;
import android.os.Handler;

import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.core.modules.ThreadsModule;

public final class NotificationReceiver
	extends CyborgReceiver<NotificationsModule> {

	public NotificationReceiver() {
		super(NotificationsModule.class);
	}

	@Override
	protected void onReceive(final Intent intent, final NotificationsModule module) {
		ThreadsModule threadsModule = getModule(ThreadsModule.class);
		Handler tempHandler = threadsModule.getDefaultHandler(ThreadsModule.Secondary);
		tempHandler.post(new Runnable() {

			@Override
			public void run() {
				module.processNotification(intent);
			}
		});
	}
}
