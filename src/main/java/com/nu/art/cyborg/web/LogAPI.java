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

package com.nu.art.cyborg.web;

import android.webkit.JavascriptInterface;

import com.nu.art.cyborg.annotations.JavascriptBridgeMethod;

public final class LogAPI
	extends JavascriptAPI {

	private static final String API_Name = "logAPI";

	public LogAPI() {
		super(API_Name);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "logV")
	public final void logV(String logMessage) {
		JavascriptStack.getInstance().logVerbose(logMessage);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "logD")
	public final void logD(String logMessage) {
		JavascriptStack.getInstance().logDebug(logMessage);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "logI")
	public final void logI(String logMessage) {
		JavascriptStack.getInstance().logInfo(logMessage);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "logW")
	public final void logW(String logMessage) {
		JavascriptStack.getInstance().logWarning(logMessage);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "logE")
	public final void logE(String logMessage) {
		JavascriptStack.getInstance().logError(logMessage);
	}
}
