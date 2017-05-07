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

public interface WebViewPageHandler {

	/**
	 * If this method is called you can count on the fact that the url is http or https, to handle other url ,use
	 * {@link WebViewPageHandler}{@link #resolveNoneHttpUrl(WebView, String)}
	 *
	 * @param view
	 * @param url  the url.
	 *
	 * @return whether or not the url had been handled.
	 */
	boolean shouldOverrideUrlLoading(WebView view, String url);

	void doUpdateVisitedHistory(WebView view, String url, boolean isReload);

	void onLoadResource(WebView view, String url);

	void onPageFinished(WebView view, String url);

	void onPageStarted(WebView view, String url, Bitmap favicon);

	void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

	boolean resolveNoneHttpUrl(WebView view, String url);

	void onProgressChanged(WebView view, int newProgress);
}
