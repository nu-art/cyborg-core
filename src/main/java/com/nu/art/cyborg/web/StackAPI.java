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

public final class StackAPI
		extends JavascriptAPI {

	private static final String API_Name = "stackAPI";

	public StackAPI() {
		super(API_Name);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "delay")
	public final void delay(int ms) {
		try {
			Thread.sleep(ms);
			JavascriptStack.getInstance().logInfo("Delay script... " + ms + "ms");
		} catch (InterruptedException e) {
			JavascriptStack.getInstance().logError(e);
		}
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "onScriptCompleted")
	public final void onScriptCompleted(String actionName) {
		JavascriptStack.getInstance().logDebug("JavascriptStack - Completed: " + actionName);
		JavascriptStack.getInstance().releaseJavaScriptExecutionThread();
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "sendEvent")
	public final void sendEvent(String category, String action, String label, long count) {
		JavascriptStack.getInstance().sendEvent(category, action, label, count);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "onScriptError")
	public final void onScriptError(String actionName, String err) {
		JavascriptStack.getInstance().logDebug("JavascriptStack - Completed: " + actionName + ", Error: " + err);
		JavascriptStack.getInstance().releaseJavaScriptExecutionThread();
	}
}
