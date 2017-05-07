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

package com.nu.art.cyborg.modules;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager.OnKeyguardExitResult;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

@ModuleDescriptor(
		usesPermissions = {})
public class KeyguardModule
		extends CyborgModule {

	private static final String DEFAULT_LOCK_KEY = "Default Lock Key";

	private KeyguardManager keyguardManager;

	private KeyguardLock lock;

	@Override
	protected void init() {
		keyguardManager = getSystemService(KeyguardService);
	}

	public synchronized void disable() {
		disable(DEFAULT_LOCK_KEY);
	}

	public synchronized void disable(String lockKey) {
		if (!keyguardManager.inKeyguardRestrictedInputMode())
			return;

		lock = keyguardManager.newKeyguardLock(lockKey);
		lock.disableKeyguard();
	}

	public synchronized void enable() {
		if (lock == null)
			return;

		lock.reenableKeyguard();
		lock = null;
	}

	public synchronized void exitKeyguardSecurely(final OnKeyguardExitResult callback) {
		if (!keyguardManager.inKeyguardRestrictedInputMode()) {
			callback.onKeyguardExitResult(true);
			return;
		}
		keyguardManager.exitKeyguardSecurely(new OnKeyguardExitResult() {

			@Override
			public void onKeyguardExitResult(boolean success) {
				enable();
				callback.onKeyguardExitResult(success);
			}
		});
	}
}
