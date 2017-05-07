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

import com.nu.art.belogged.BeLogged;
import com.nu.art.belogged.Logger;
import com.nu.art.software.core.utils.PoolQueue;
import com.nu.art.cyborg.core.abs.Cyborg;

public final class ActivityStack
		extends Logger {

	public interface ActivityStackAction {

		void execute(CyborgActivityBridge activity);
	}

	@SuppressWarnings("unused")
	private Thread runnableExecutor;

	private PoolQueue<ActivityStackAction> queue = new PoolQueue<ActivityStackAction>() {

		@Override
		protected void executeAction(final ActivityStackAction action)
				throws Exception {
			synchronized (screenSyncObject) {
				if (activity == null)
					screenSyncObject.wait();
				action.execute(activity);
			}
		}

		@Override
		protected void onExecutionError(ActivityStackAction action, Throwable e) {
			logError("Error while executing action: " + action, e);
		}
	};

	public ActivityStack(Cyborg cyborg) {
		queue.createThreads("Screen UI Action Executer");
		setBeLogged(cyborg.getModule(BeLogged.class));
	}

	public final void addItem(ActivityStackAction item) {
		queue.addItem(item);
	}

	public final boolean removeItem(ActivityStackAction item) {
		return queue.removeItem(item);
	}

	private final Object screenSyncObject = new Object();

	private volatile CyborgActivityBridge activity;

	final void setActivityBridge(CyborgActivityBridge activity) {
		synchronized (screenSyncObject) {
			this.activity = activity;
			if (activity != null)
				screenSyncObject.notify();
		}
	}
}
