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

import android.Manifest.permission;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.calls.NativeCall.CallDirection;
import com.nu.art.cyborg.modules.calls.NativeCall.CallState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.nu.art.cyborg.modules.calls.NativeCall.CallDirection.Incoming;
import static com.nu.art.cyborg.modules.calls.NativeCall.CallDirection.Outgoing;
import static com.nu.art.cyborg.modules.calls.NativeCall.CallState.Dialing;
import static com.nu.art.cyborg.modules.calls.NativeCall.CallState.InProgress;
import static com.nu.art.cyborg.modules.calls.NativeCall.CallState.Ringing;

@SuppressWarnings("unused")
@ModuleDescriptor(usesPermissions = {
	permission.PROCESS_OUTGOING_CALLS,
	permission.READ_PHONE_STATE
})
public final class NativeCallsModule
	extends CyborgModule {

	public interface NativeCallsListener {

		void onCallsStateChanged();
	}

	private boolean enabled;

	private final ArrayList<NativeCall> calls = new ArrayList<>();

	@Override
	protected void init() {}

	@RequiresPermission(allOf = {
		permission.PROCESS_OUTGOING_CALLS,
		permission.READ_PHONE_STATE
	})
	public final void enable() {
		if (enabled)
			return;

		enabled = true;
		registerReceiver(NativeCallsReceiver.class);
	}

	public final void disable() {
		if (!enabled)
			return;

		unregisterReceiver(NativeCallsReceiver.class);
		enabled = false;
	}

	public Collection<NativeCall> getCalls() {
		return Collections.unmodifiableList(calls);
	}

	public final void testCallsState() {
		IntentFilter callsStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		Intent callsState = getApplicationContext().registerReceiver(null, callsStateFilter);
		if (callsState == null)
			return;

		String state = callsState.getStringExtra(TelephonyManager.EXTRA_STATE);
		String number = callsState.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		CallState callState = CallState.getState(state);

		logInfo("Phone State Changed Intent: [State, Number] - [" + callState + ", " + number + "]");
	}

	final void onCallStateChanged(String number, CallState callState) {
		logInfo("onCallStateChanged number: " + number + " [ " + callState + " ]");

		NativeCall[] items = ArrayTools.asArray(calls, NativeCall.class);
		switch (callState) {
			case Ringing:
				createNewCall(number, Incoming, Ringing);
				break;

			case Dialing:
				createNewCall(number, Outgoing, Dialing);
				break;

			case InProgress:
				if (number == null) {
					logWarning("number was null what to do here??");
					break;
				}

				NativeCall call = getCallByNumber(number);
				if (call == null) {
					logWarning("Could not find call with number: " + number);
					break;
				}

				call.setState(InProgress);
				break;

			case Idle:
				calls.clear();
				break;

			default:
				throw new MUST_NeverHappenException("Unhandled state: " + callState);
		}

		logInfo("Calls:" + ArrayTools.printGenericArray("", 1, items));
		dispatchGlobalEvent("Calls State changed", NativeCallsListener.class, new Processor<NativeCallsListener>() {
			@Override
			public void process(NativeCallsListener listener) {
				listener.onCallsStateChanged();
			}
		});
	}

	private NativeCall getCallByNumber(String number) {
		for (NativeCall call : calls) {
			if (call.getPhoneNumber().equals(number))
				return call;
		}

		return null;
	}

	private void createNewCall(String phoneNumber, CallDirection direction, CallState state) {
		NativeCall call = getCallByNumber(phoneNumber);
		if (call == null) {
			call = new NativeCall();
			call.setPhoneNumber(phoneNumber);
		}

		call.setDirection(direction);
		call.setState(state);
		calls.add(call);
	}

	private void setCallState(NativeCall call, CallState newState) {
		call.setState(newState);
	}
}
