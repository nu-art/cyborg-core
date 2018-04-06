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

import android.graphics.Bitmap;
import android.webkit.WebView;

public abstract class WebViewPageImplementor
	implements WebViewPageHandler {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return false;
	}

	@Override
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {}

	@Override
	public void onLoadResource(WebView view, String url) {}

	@Override
	public void onPageFinished(WebView view, String url) {}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {}

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {}

	@Override
	public boolean resolveNoneHttpUrl(WebView view, String url) {
		return false;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {}
}
