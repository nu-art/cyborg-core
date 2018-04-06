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

package com.nu.art.cyborg.web.api;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

public abstract class WebViewJavaScriptImplementor
	implements WebViewJavaScriptHandler {

	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		return false;
	}

	@Override
	public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
		return false;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
		return false;
	}

	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
		return false;
	}
}
