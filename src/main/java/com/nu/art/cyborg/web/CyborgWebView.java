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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.interfaces.ProgressNotifier;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.cyborg.common.utils.Storage;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.web.JavascriptStack.JavascriptAction;
import com.nu.art.cyborg.web.api.OnLifeCycleListener;
import com.nu.art.cyborg.web.api.ScriptActionErrorHandler;
import com.nu.art.cyborg.web.api.WebViewCustomViewHandler;
import com.nu.art.cyborg.web.api.WebViewDownloadHandler;
import com.nu.art.cyborg.web.api.WebViewFileChooserHandler;
import com.nu.art.cyborg.web.api.WebViewGeoLocationHandler;
import com.nu.art.cyborg.web.api.WebViewGeoLocationHandler.GeoLocationResponse;
import com.nu.art.cyborg.web.api.WebViewJavaScriptHandler;
import com.nu.art.cyborg.web.api.WebViewPageDetailsHandler;
import com.nu.art.cyborg.web.api.WebViewPageHandler;
import com.nu.art.cyborg.web.api.WebViewRequestHandler;
import com.nu.art.cyborg.web.api.WebViewSystemHandler;
import com.nu.art.cyborg.web.api.WebViewVideoHandler;
import com.nu.art.cyborg.web.api.WebViewWindowHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class CyborgWebView
	extends WebView
	implements ILogger {

	static {
		try {
			OnPause = WebView.class.getMethod("onPause");
		} catch (Throwable e) {
			Log.e("REFLECTION STATIC", "Cannot extract WebView.onPause()");
		}
		try {
			OnResume = WebView.class.getMethod("onResume");
		} catch (Throwable e) {
			Log.e("REFLECTION STATIC", "Cannot extract WebView.onResume()");
		}
		try {
			FindAllAsync = WebView.class.getMethod("findAllAsync", String.class);
		} catch (Throwable e) {
			Log.e("REFLECTION STATIC", "Cannot extract WebView.findAllAsync()");
		}
		try {
			SetFindIsUp = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
		} catch (Throwable e) {
			Log.e("REFLECTION STATIC", "Cannot extract WebView.setFindIsUp()");
		}
	}

	public static final boolean DEBUG = false;

	private static Method OnPause;

	private static Method OnResume;

	private static Method FindAllAsync;

	private static Method SetFindIsUp;

	private boolean paused = false;

	private boolean destroyed = false;

	protected final String tag = getClass().getSimpleName();

	private ScriptActionErrorHandler scriptActionErrorHandler;

	private WebViewGeoLocationHandler geoLocationHandler;

	private WebViewJavaScriptHandler javaScriptHandler;

	private WebViewPageDetailsHandler pageDetailsHandler;

	private WebViewPageHandler pageHandler;

	private WebViewRequestHandler requestHandler;

	private WebViewSystemHandler systemHandler;

	private WebViewVideoHandler videoHandler;

	private WebViewFileChooserHandler fileChooserHandler;

	private WebViewWindowHandler windowHandler;

	private WebViewCustomViewHandler customViewHandler;

	private WebViewDownloadHandler downloadHandler;

	private String finishedURL;

	protected WebSettings settings;

	private OnLifeCycleListener lifeCycleListener;

	protected Cyborg cyborg;

	private ILogger logger;

	public CyborgWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CyborgWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CyborgWebView(Context context) {
		super(context);
		init();
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void init() {
		if (isInEditMode())
			return;

		cyborg = CyborgBuilder.getInstance();
		logger = cyborg.getLogger(this);
		settings = getSettings();
		settings.setJavaScriptEnabled(true);
		setupClients();

		JavascriptStack.getInstance().initWebView(this);
	}

	public final void enableCrossDomain() {
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			enableCrossDomain4_1();
		} else {
			enableCrossDomainPre4_1();
		}
	}

	private final void enableCrossDomainPre4_1() {
		try {
			java.lang.reflect.Field webviewcore_field = WebView.class.getDeclaredField("mWebViewCore");
			webviewcore_field.setAccessible(true);
			Object webviewcore = webviewcore_field.get(this);

			Method method = webviewcore.getClass().getDeclaredMethod("nativeRegisterURLSchemeAsLocal", String.class);
			method.setAccessible(true);
			method.invoke(webviewcore, "http");
			method.invoke(webviewcore, "https");
		} catch (Exception e) {
			logError("Failed to enable cross domain", e);
		}
	}

	// for android 4.1+
	private final void enableCrossDomain4_1() {
		try {
			java.lang.reflect.Field webviewclassic_field = WebView.class.getDeclaredField("mProvider");
			webviewclassic_field.setAccessible(true);
			Object webviewclassic = webviewclassic_field.get(this);

			java.lang.reflect.Field webviewcore_field = webviewclassic.getClass().getDeclaredField("mWebViewCore");
			webviewcore_field.setAccessible(true);
			Object mWebViewCore = webviewcore_field.get(webviewclassic);

			java.lang.reflect.Field nativeclass_field = webviewclassic.getClass().getDeclaredField("mNativeClass");
			nativeclass_field.setAccessible(true);
			Object mNativeClass = nativeclass_field.get(webviewclassic);

			Method method = mWebViewCore.getClass().getDeclaredMethod("nativeRegisterURLSchemeAsLocal", new Class[]{
				int.class,
				String.class
			});
			method.setAccessible(true);
			method.invoke(mWebViewCore, mNativeClass, "http");
			method.invoke(mWebViewCore, mNativeClass, "https");

			// Method method1 = WebSettings.class.getMethod("setAllowUniversalAccessFromFileURLs", new
			// Class[]{boolean.class});
			// Method method2 = WebSettings.class.getMethod("setAllowFileAccessFromFileURLs", new
			// Class[]{boolean.class});
			// method1.setAccessible(true);
			// method2.setAccessible(true);
			// method1.invoke(getSettings(), true);
			// method2.invoke(getSettings(), true);
		} catch (Exception e) {
			logError("Failed to enable cross domain", e);
		}
	}

	private void setupClients() {
		setDownloadListener(new DownloadListener() {

			private String downloadingNow;

			@Override
			public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimeType, final long contentLength) {
				logInfo("DEBUG-LOG: onDownloadStart... url: " + url + "  userAgent: " + userAgent + "  contentDisposition: " + contentDisposition + "  mimetype: " + mimeType + "  contentLength: " + contentLength);
				if (downloadingNow != null) {
					logWarning("DOWNLOAD IN PROGRESS: " + downloadingNow + "... NOT DOWNLOADING FILE FROM NEW URL: " + url);
					return;
				}
				if (downloadHandler == null) {
					if (getContext() instanceof Application) {
						logWarning("APPLICATION CONTEXT FOUND!!! NOT DOWNLOADING FILE FROM: " + url);
						return;
					}
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					getContext().startActivity(i);
					return;
				}
				HandlerThread fileDownloader = new HandlerThread("File Downloader: " + url);
				fileDownloader.start();
				Handler handler = new Handler(fileDownloader.getLooper());
				downloadingNow = url;
				handler.post(new Runnable() {

					@Override
					public void run() {
						FileOutputStream os = null;
						InputStream is = null;
						String fileName = contentDisposition;
						if (fileName == null)
							fileName = "unknown-file";
						int index = fileName.indexOf("filename=\"");
						if (index != -1)
							fileName = fileName.substring(index + "filename=\"".length(), fileName.length() - 1);

						fileName = fileName.replaceAll("[\\*/:<>\\?\\\\\\|\\+,\\.;=\\[\\]\\\"\\'\\^]", "_");
						try {
							HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
							connection.setRequestProperty("User-Agent", userAgent);
							connection.setRequestProperty("Content-Type", mimeType);
							connection.connect();
							is = connection.getInputStream();

							File outputFile;
							int counter = 0;
							while (true) {
								outputFile = new File(Storage.getDefaultStorage().getPath() + "/Download", fileName + (counter == 0 ? "" : "(" + counter + ")"));
								if (!outputFile.exists())
									break;
							}

							final File finalOutputFile = outputFile;
							FileTools.createNewFile(finalOutputFile);
							os = new FileOutputStream(finalOutputFile);

							StreamTools.copy(is, contentLength, os, new ProgressNotifier() {

								@Override
								public void reportState(String report) {

								}

								@Override
								public void onCopyStarted() {
									downloadHandler.onDownloadStarted(url);
								}

								@Override
								public void onProgressPercentage(double percentages) {
									downloadHandler.onDownloadProgress(url, (float) percentages);
								}

								@Override
								public void onCopyException(Throwable t) {
									downloadHandler.onDownloadError(url, t);
								}

								@Override
								public void onCopyEnded() {
									downloadHandler.onDownloadEneded(url, finalOutputFile);
								}
							});
						} catch (Exception e) {
							downloadHandler.onDownloadError(url, e);
						} finally {
							downloadingNow = null;
							if (os != null)
								try {
									os.close();
								} catch (IOException ignored) {
								}
							if (is != null)
								try {
									is.close();
								} catch (IOException ignored) {
								}
						}
					}
				});
			}
		});
		setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onProgressChanged... " + newProgress);
				if (pageHandler == null)
					return;
				if (newProgress >= 89)
					onPageFinished(view, view.getUrl());
				pageHandler.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onRequestFocus...");
				if (pageDetailsHandler == null)
					return;
				CyborgWebView.this.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(final String origin, final Callback callback) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onGeolocationPermissionsShowPrompt (origin: " + origin + ", callback: " + callback + ")");
				if (geoLocationHandler == null)
					return;
				geoLocationHandler.onGeolocationPermissionsShowPrompt(origin, new Processor<GeoLocationResponse>() {

					@Override
					public void process(GeoLocationResponse res) {
						callback.invoke(origin, res.enable, res.remember);
						settings.setGeolocationEnabled(res.enable);
					}
				});
			}

			@Override
			public void onGeolocationPermissionsHidePrompt() {
				if (DEBUG)
					logInfo("DEBUG-LOG: onGeolocationPermissionsHidePrompt...");
				if (geoLocationHandler == null)
					return;
				geoLocationHandler.onGeolocationPermissionsShowPrompt();
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
				if (DEBUG)
					logInfo("DEBUG-LOG: File Chooser: " + uploadMsg + ", acceptType: " + acceptType + ", capture: " + capture);
				openFileChooser(uploadMsg, acceptType);
			}

			public void openFileChooser(final ValueCallback<Uri> uploadMsg, String acceptType) {
				if (DEBUG)
					logInfo("DEBUG-LOG: File Chooser: " + uploadMsg + ", acceptType: " + acceptType);

				if (fileChooserHandler == null)
					uploadMsg.onReceiveValue(null);

				boolean handled = fileChooserHandler.openFileChooser(getUrl(), acceptType, new Processor<Uri>() {

					@Override
					public void process(Uri uri) {
						uploadMsg.onReceiveValue(uri);
					}
				});

				if (!handled)
					uploadMsg.onReceiveValue(null);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				if (DEBUG)
					logInfo("DEBUG-LOG: File Chooser: " + uploadMsg);
				openFileChooser(uploadMsg, null);
			}

			@Override
			public Bitmap getDefaultVideoPoster() {
				if (DEBUG)
					logInfo("DEBUG-LOG: getDefaultVideoPoster...");
				if (javaScriptHandler == null)
					return null;
				return videoHandler.getDefaultVideoPoster();
			}

			@Override
			public View getVideoLoadingProgressView() {
				if (DEBUG)
					logInfo("DEBUG-LOG: getVideoLoadingProgressView...");
				if (javaScriptHandler == null)
					return null;
				return videoHandler.getVideoLoadingProgressView();
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onJsAlert (url: " + url + ", message: " + message + ", result: " + result + ")");
				if (javaScriptHandler == null) {
					result.confirm();
					return true;
				}
				return javaScriptHandler.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onJsBeforeUnload (url: " + url + ", message: " + message + ", result: " + result + ")");
				if (javaScriptHandler == null) {
					result.cancel();
					return false;
				}
				return javaScriptHandler.onJsBeforeUnload(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onJsConfirm (url: " + url + ", message: " + message + ", result: " + result + ")");
				if (javaScriptHandler == null) {
					result.cancel();
					return false;
				}
				return javaScriptHandler.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onJsPrompt (url: " + url + ", message: " + message + ", defaultValue: " + defaultValue + ", result: " + result + ")");
				if (javaScriptHandler == null) {
					result.cancel();
					return false;
				}
				return javaScriptHandler.onJsPrompt(view, url, message, defaultValue, result);
			}

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onCreateWindow (isDialog: " + isDialog + ", isUserGesture: " + isUserGesture + ", resultMsg: " + resultMsg + ")");
				if (windowHandler == null)
					return false;
				return windowHandler.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
			}

			@Override
			public void onCloseWindow(WebView window) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onCloseWindow...");
				if (windowHandler == null)
					return;
				windowHandler.onCloseWindow(window);
			}

			@Override
			public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onShowCustomView (callback: " + callback + ")");
				if (javaScriptHandler == null)
					return;
				customViewHandler.onShowCustomView(view, requestedOrientation, callback);
			}

			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				onShowCustomView(view, 0, callback);
			}

			@Override
			public void onHideCustomView() {
				if (DEBUG)
					logInfo("DEBUG-LOG: onHideCustomView...");
				if (javaScriptHandler == null)
					return;
				customViewHandler.onHideCustomView();
			}

			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedIcon (icon: " + icon + ")");
				if (pageDetailsHandler == null)
					return;
				pageDetailsHandler.onReceivedIcon(view, icon);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedTitle (title: " + title + ")");
				if (pageDetailsHandler == null)
					return;
				pageDetailsHandler.onReceivedTitle(view, title);
			}

			@Override
			public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedTouchIconUrl (url: " + url + ", precomposed: " + precomposed + ")");
				if (pageDetailsHandler == null)
					return;
				pageDetailsHandler.onReceivedTouchIconUrl(view, url, precomposed);
			}

			@Override
			public void onRequestFocus(WebView view) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onRequestFocus...");
				if (pageDetailsHandler == null)
					return;
				pageDetailsHandler.onRequestFocus(view);
			}
		});

		setWebViewClient(new WebViewClient() {

			@Override
			public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
				if (DEBUG)
					logInfo("DEBUG-LOG: doUpdateVisitedHistory (url: " + url + ", isReload: " + isReload + ")");
				if (pageHandler == null)
					return;
				pageHandler.doUpdateVisitedHistory(view, url, isReload);
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onLoadResource (url: " + url + ")");
				if (pageHandler == null)
					return;
				pageHandler.onLoadResource(view, url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onPageFinished (url: " + url + ")");
				CyborgWebView.this.onPageFinished(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onPageStarted (url: " + url + ", favicon: " + favicon + ")");
				finishedURL = null;
				if (pageHandler == null)
					return;
				pageHandler.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedError (errorCode: " + errorCode + ", description: " + description + ", failingUrl: " + failingUrl + ")");
				if (pageHandler == null)
					return;
				pageHandler.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (DEBUG)
					logInfo("DEBUG-LOG: shouldOverrideUrlLoading: " + url);
				if (url.toLowerCase().equals("about:blank"))
					return super.shouldOverrideUrlLoading(view, url);

				if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
					if (pageHandler != null && pageHandler.shouldOverrideUrlLoading(view, url))
						return true;
					return super.shouldOverrideUrlLoading(view, url);
				}

				if (getContext() instanceof Activity && resolveUrl(url))
					return true;

				if (pageHandler != null && pageHandler.resolveNoneHttpUrl(view, url))
					return true;

				if (!(getContext() instanceof Activity))
					return true;

				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					getContext().startActivity(intent);
				} catch (Throwable e) {
					logError(e);
				}
				return true;
			}

			/*
			 * requestHandler
			 */
			@Override
			public void onFormResubmission(WebView view, Message dontResend, Message resend) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onFormResubmission...");
				if (requestHandler == null)
					return;
				requestHandler.onFormResubmission(view, dontResend, resend);
			}

			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedHttpAuthRequest (host: " + host + ", realm: " + realm + ")");
				if (requestHandler == null)
					return;
				requestHandler.onReceivedHttpAuthRequest(view, handler, host, realm);
			}

			@Override
			@SuppressWarnings("unused")
			public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedLoginRequest (realm: " + realm + ", account: " + account + ", args: " + args + ")");
				if (requestHandler == null)
					return;
				requestHandler.onReceivedLoginRequest(view, realm, account, args);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onReceivedSslError (error: " + error + ")");
				if (requestHandler == null)
					return;
				requestHandler.onReceivedSslError(view, handler, error);
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				if (DEBUG)
					logInfo("DEBUG-LOG: shouldInterceptRequest: " + url);
				if (requestHandler == null)
					return null;
				return requestHandler.shouldInterceptRequest(view, url);
			}

			@Override
			public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onTooManyRedirects...");
				if (requestHandler == null)
					return;
				requestHandler.onTooManyRedirects(view, cancelMsg, continueMsg);
			}

			@Override
			public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onUnhandledKeyEvent: " + event);
				if (systemHandler == null)
					return;
				systemHandler.onUnhandledKeyEvent(view, event);
			}

			@Override
			public void onScaleChanged(WebView view, float oldScale, float newScale) {
				if (DEBUG)
					logInfo("DEBUG-LOG: onScaleChanged: " + oldScale + " => " + newScale);
				if (systemHandler == null)
					return;
				systemHandler.onScaleChanged(view, oldScale, newScale);
			}

			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				if (DEBUG)
					logInfo("DEBUG-LOG: shouldOverrideKeyEvent: " + event);
				if (systemHandler == null)
					return false;
				return systemHandler.shouldOverrideKeyEvent(view, event);
			}
		});
	}

	protected boolean resolveUrl(String url) {
		if (url.startsWith("mailto:")) {
			sendMailUrl(url);
			return true;
		}
		if (url.startsWith("tel:")) {
			sendTelIntent(url);
			return true;
		}
		return false;
	}

	private void sendTelIntent(String url) {
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		getContext().startActivity(intent);
	}

	private void sendMailUrl(String url) {
		MailTo mailTo = MailTo.parse(url);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo.getTo()});
		intent.putExtra(Intent.EXTRA_TEXT, mailTo.getBody());
		intent.putExtra(Intent.EXTRA_SUBJECT, mailTo.getSubject());
		intent.putExtra(Intent.EXTRA_CC, mailTo.getCc());
		intent.setType("message/rfc822");
		getContext().startActivity(intent);
	}

	@SuppressWarnings("unused")
	protected void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
	}

	final void onScriptErrorListener(String err) {
		if (scriptActionErrorHandler != null)
			scriptActionErrorHandler.onScriptExecutionError(this, err);
	}

	final void onPageFinished(WebView view, String url) {
		if (finishedURL != null && finishedURL.equals(url))
			return;

		finishedURL = url;

		if (pageHandler == null)
			return;
		pageHandler.onPageFinished(view, url);
	}

	@Override
	public void destroy() {
		pause();
		if (getParent() != null)
			((ViewGroup) getParent()).removeView(this);
		removeAllViews();
		super.destroy();
		destroyed = true;
		if (lifeCycleListener != null)
			lifeCycleListener.onDestroyed();
	}

	public final boolean isDestroyed() {
		return destroyed;
	}

	public final boolean isPaused() {
		return paused;
	}

	public final void pause() {
		if (paused)
			return;
		try {
			OnPause.invoke(this);
			paused = true;
			if (lifeCycleListener != null)
				lifeCycleListener.onPause();
		} catch (Exception e) {
			logError("Error invoking onPause method!", e);
		}
	}

	public final void resume() {
		if (!paused)
			return;
		try {
			OnResume.invoke(this);
			paused = false;
			if (lifeCycleListener != null)
				lifeCycleListener.onResumed();
		} catch (Exception e) {
			logError("Error invoking onPause method!", e);
		}
	}

	public final void findTextInPage(String textToFind) {
		if (textToFind == null || textToFind.equals("")) {
			clearMatches();
			return;
		}
		try {
			FindAllAsync.invoke(this, textToFind);
		} catch (Throwable notIgnored) {
			findAll(textToFind);
			try {
				SetFindIsUp.invoke(this, true);
			} catch (Throwable ignored) {
			}
		}
	}

	@Override
	public void loadUrl(String url) {
		super.loadUrl(JavascriptStack.getInstance().OnLoadJS);
		super.loadUrl(url);
	}

	public void executeActions(JavascriptAction... actions) {
		for (JavascriptAction action : actions) {
			action.buildAndExecute(this);
		}
	}

	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {
		try {
			super.onScrollChanged(l, t, oldl, oldt);
		} catch (NullPointerException e) {
			logError("I have no idea why the fuck this happens... and I don't give a shit right now... it 4:00AM and I want to get some sleep!!!", e);
		}
	}

	public void requestFocusWorkaround() {
		postDelayed(new Runnable() {

			@Override
			public void run() {
				onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY());
			}
		}, 500);
	}

	public final void setLifeCycleListener(OnLifeCycleListener lifeCycleListener) {
		this.lifeCycleListener = lifeCycleListener;
	}

	public final void setGeoLocationHandler(WebViewGeoLocationHandler geoLocationHandler) {
		this.geoLocationHandler = geoLocationHandler;
	}

	public final void setJavaScriptHandler(WebViewJavaScriptHandler javaScriptHandler) {
		this.javaScriptHandler = javaScriptHandler;
	}

	public final void setPageDetailsHandler(WebViewPageDetailsHandler pageDetailsHandler) {
		this.pageDetailsHandler = pageDetailsHandler;
	}

	public final void setPageHandler(WebViewPageHandler pageHandler) {
		this.pageHandler = pageHandler;
	}

	public final void setRequestHandler(WebViewRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public final void setSystemHandler(WebViewSystemHandler systemHandler) {
		this.systemHandler = systemHandler;
	}

	public final void setVideoHandler(WebViewVideoHandler videoHandler) {
		this.videoHandler = videoHandler;
	}

	public final void setFileChooserHandler(WebViewFileChooserHandler fileChooserHandler) {
		this.fileChooserHandler = fileChooserHandler;
	}

	public final void setWindowHandler(WebViewWindowHandler windowHandler) {
		this.windowHandler = windowHandler;
	}

	public final void setCustomViewHandler(WebViewCustomViewHandler customViewHandler) {
		this.customViewHandler = customViewHandler;
	}

	public void setDownloadHandler(WebViewDownloadHandler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

	@Override
	public void logVerbose(String verbose) {
		if (logger != null)
			logger.logVerbose(verbose);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		if (logger != null)
			logger.logVerbose(verbose, params);
	}

	@Override
	public void logVerbose(Throwable e) {
		if (logger != null)
			logger.logVerbose(e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		if (logger != null)
			logger.logVerbose(verbose, e);
	}

	@Override
	public void logDebug(String debug) {
		if (logger != null)
			logger.logDebug(debug);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		if (logger != null)
			logger.logDebug(debug, params);
	}

	@Override
	public void logDebug(Throwable e) {
		if (logger != null)
			logger.logDebug(e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		if (logger != null)
			logger.logDebug(debug, e);
	}

	@Override
	public void logInfo(String info) {
		if (logger != null)
			logger.logInfo(info);
	}

	@Override
	public void logInfo(String info, Object... params) {
		if (logger != null)
			logger.logInfo(info, params);
	}

	@Override
	public void logInfo(Throwable e) {
		if (logger != null)
			logger.logInfo(e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		if (logger != null)
			logger.logInfo(info, e);
	}

	@Override
	public void logWarning(String warning) {
		if (logger != null)
			logger.logWarning(warning);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		if (logger != null)
			logger.logWarning(warning, params);
	}

	@Override
	public void logWarning(Throwable e) {
		if (logger != null)
			logger.logWarning(e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		if (logger != null)
			logger.logWarning(warning, e);
	}

	@Override
	public void logError(String error) {
		if (logger != null)
			logger.logError(error);
	}

	@Override
	public void logError(String error, Object... params) {
		if (logger != null)
			logger.logError(error, params);
	}

	@Override
	public void logError(Throwable e) {
		if (logger != null)
			logger.logError(e);
	}

	@Override
	public void logError(String error, Throwable e) {
		if (logger != null)
			logger.logError(error, e);
	}
}
