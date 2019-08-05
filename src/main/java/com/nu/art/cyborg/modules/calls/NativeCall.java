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

import android.telephony.TelephonyManager;

public final class NativeCall {

	private String phoneNumber;

	private CallState state;

	private CallDirection direction;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	final void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public CallState getState() {
		return state;
	}

	final void setState(CallState state) {
		this.state = state;
	}

	public CallDirection getDirection() {
		return direction;
	}

	final void setDirection(CallDirection direction) {
		this.direction = direction;
	}

	@Override
	public final String toString() {
		return (direction == CallDirection.Incoming ? "Incoming" : "Outgoing") + " phone: " + phoneNumber + " [" + state + "]";
	}

	public enum CallDirection {
		Outgoing,
		Incoming
	}

	public enum CallState {
		Ringing(TelephonyManager.CALL_STATE_RINGING, TelephonyManager.EXTRA_STATE_RINGING),
		Dialing(-1, "Dialing"),
		InProgress(TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.EXTRA_STATE_OFFHOOK),
		Idle(TelephonyManager.CALL_STATE_IDLE, TelephonyManager.EXTRA_STATE_IDLE),
		;

		private int state;

		private String asString;

		CallState(int state, String asString) {
			this.state = state;
			this.asString = asString;
		}

		public synchronized static CallState getState(int state) {
			for (CallState callState : values()) {
				if (callState.state == state)
					return callState;
			}
			throw new EnumConstantNotPresentException(CallState.class, "For value = " + state);
		}

		public synchronized static CallState getState(String state) {
			for (CallState callState : values()) {
				if (callState.asString.equals(state))
					return callState;
			}
			throw new EnumConstantNotPresentException(CallState.class, "For value = " + state);
		}
	}
}
