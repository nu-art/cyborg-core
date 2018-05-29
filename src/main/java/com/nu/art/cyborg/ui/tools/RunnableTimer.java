/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
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

package com.nu.art.cyborg.ui.tools;

/**
 * A convenient wrapper for runnable to be called on the main thread, without knowing which thread is going to run it.
 */
public abstract class RunnableTimer
	implements Runnable {

	@Override
	public final void run() {
		long started = System.currentTimeMillis();
		execute();

		postExecute(System.currentTimeMillis() - started);
	}

	protected abstract void execute();

	protected abstract void postExecute(long duration);
}
