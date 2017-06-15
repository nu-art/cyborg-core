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

import android.os.Handler;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.core.utils.PoolQueue;
import com.nu.art.cyborg.core.abs.Cyborg;

public final class ActivityStack
		extends Logger {

	public interface ActivityStackAction {

		void execute(CyborgActivityBridge activity);
	}

	private final Handler uiHandler;

	private final Object screenSyncObject = new Object();

	private volatile CyborgActivityBridge activity;

	public ActivityStack(Cyborg cyborg) {
		queue.createThreads("Screen UI Action Executor");
		uiHandler = cyborg.getUI_Handler();
		setBeLogged(cyborg.getModule(BeLogged.class));
	}

	public final void addItem(ActivityStackAction item) {
		queue.addItem(item);
	}

	public final boolean removeItem(ActivityStackAction item) {
		return queue.removeItem(item);
	}

	final void setActivityBridge(CyborgActivityBridge activity) {
		synchronized (screenSyncObject) {
			this.activity = activity;
			if (activity != null)
				screenSyncObject.notify();
		}
	}

	private PoolQueue<ActivityStackAction> queue = new PoolQueue<ActivityStackAction>() {
		@Override
		protected void executeAction(final ActivityStackAction action)
				throws Exception {
			synchronized (screenSyncObject) {
				if (activity == null)
					screenSyncObject.wait();

				executeStackImpl(action);
			}
		}

		private void executeStackImpl(final ActivityStackAction action) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					synchronized (screenSyncObject) {
						if (activity == null) {
							addItem(action);
							return;
						}
						action.execute(activity);
					}
				}
			};
			uiHandler.post(r);
		}

		@Override
		protected void onExecutionError(ActivityStackAction item, Throwable e) {
			logError("Error while executing action: " + item, e);
		}
	};
}
