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

import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.JavascriptInterface;

import com.nu.art.cyborg.annotations.JavascriptBridgeMethod;

public final class WebViewAPI
		extends JavascriptAPI {

	private CyborgWebView webView;

	public WebViewAPI(CyborgWebView webView) {
		super("webviewAPI");
		this.webView = webView;
	}

	WebViewAPI() {
		super("webviewAPI");
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "onWindowLoaded")
	public void onWindowLoaded(String url) {
		if (CyborgWebView.DEBUG)
			JavascriptStack.getInstance().logInfo("DEBUG-LOG: onWindowLoaded (url: " + url + ")");

		webView.onPageFinished(webView, url);
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "clickOnElement")
	public void clickOnElement(final int x, final int y) {
		if (CyborgWebView.DEBUG)
			JavascriptStack.getInstance().logInfo("DEBUG-LOG: clickOnElement (point: [" + x + ", " + y + "])");

		final long downTime = SystemClock.uptimeMillis();
		JavascriptStack.uiHandler.post(new Runnable() {

			@Override
			public void run() {
				int metaState = 0;
				float size = 0.4f;
				float pressure = 1f;
				MotionEvent motionEventDown = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_DOWN, x, y, pressure, size, metaState, 0.1f, 0.1f, 1, 0);
				// MotionEvent motionEventDown = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_MOVE,
				// x, y, metaState); // onPageFinished(PageWebView.this,
				webView.dispatchTouchEvent(motionEventDown);
			}
		});
		JavascriptStack.uiHandler.post(new Runnable() {

			@Override
			public void run() {
				int metaState = 0;
				float size = 0.4f;
				float pressure = 1f;
				MotionEvent motionEventUp = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_UP, x, y, pressure, size, metaState, 0.1f, 0.1f, 1, 0);
				// MotionEvent motionEventUp = MotionEvent.obtain(downTime, downTime + 160, MotionEvent.ACTION_UP, x, y,
				// metaState); // onPageFinished(PageWebView.this,
				webView.dispatchTouchEvent(motionEventUp);
			}
		});
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "getWidth")
	public int getWidth() {
		return webView.getWidth();
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "getHeight")
	public int getHeight() {
		return webView.getHeight();
	}

	@JavascriptInterface
	@JavascriptBridgeMethod(methodName = "onScriptErrorListener")
	public void onScriptErrorListener(String err) {
		if (CyborgWebView.DEBUG)
			JavascriptStack.getInstance().logInfo("DEBUG-LOG: onScriptErrorListener (err: " + err + ")");

		webView.onScriptErrorListener(err);
	}
}
