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
package com.nu.art.cyborg.modules.calls;

import android.content.Intent;
import android.telephony.TelephonyManager;

import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.modules.calls.NativeCall.CallState;

import static com.nu.art.cyborg.modules.calls.NativeCall.CallState.Dialing;

public final class NativeCallsReceiver
	extends CyborgReceiver<NativeCallsModule> {

	private static final String[] actions = new String[]{
		Intent.ACTION_NEW_OUTGOING_CALL,
		TelephonyManager.ACTION_PHONE_STATE_CHANGED
	};

	public NativeCallsReceiver() {
		super(NativeCallsModule.class, actions);
	}

	@Override
	protected void onReceive(Intent intent, NativeCallsModule module) {
		String action = intent.getAction();
		if (action == null) {
			logWarning("Action was null... ");
			return;
		}

		switch (action) {
			case Intent.ACTION_NEW_OUTGOING_CALL:
				module.onCallStateChanged(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER), Dialing);
				return;

			case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
				String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				CallState callState = CallState.getState(state);
				module.onCallStateChanged(number, callState);
				return;
		}

		logWarning("Unhandled Broadcast receiver event action: " + action);
	}
}
