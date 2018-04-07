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

package com.nu.art.cyborg.web.api;

import android.net.http.SslError;
import android.os.Message;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

public interface WebViewRequestHandler {

	void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

	void onReceivedLoginRequest(WebView view, String realm, String account, String args);

	void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

	void onFormResubmission(WebView view, Message dontResend, Message resend);

	void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg);

	WebResourceResponse shouldInterceptRequest(WebView view, String url);
}
