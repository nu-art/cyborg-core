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

import android.os.Handler;
import android.os.Looper;

import com.nu.art.core.interfaces.Condition;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.utils.PoolQueue;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.abs._Analytics;
import com.nu.art.cyborg.web.JavascriptStack.JavascriptActionExecutor;
import com.nu.art.cyborg.web.api.OnLifeCycleListener;

public final class JavascriptStack
	extends PoolQueue<JavascriptActionExecutor>
	implements ILogger, _Analytics {

	public static final String ClickFromAndroidScript = "" + //
		"function calculateCoordinateAndClickOnNode(node) {\n" + //
		"    var rect = node.getBoundingClientRect();\n" + //
		"    var x = rect.left;\n" + //
		"    var y = rect.top;\n" + //
		"    var w = rect.right - rect.left;\n" + //
		"    var h = rect.bottom - rect.top;\n\n" + //

		"    var finalX = x + w / 2;\n" + //
		"    var finalY = y + h / 2;\n\n" + //

		"    var webViewWidth = window.webviewAPI.getWidth();\n" + //
		"    var webViewHeight = window.webviewAPI.getHeight();\n" + //
		"    var screenWidth = document.documentElement.clientWidth;\n" + //
		"    var screenHeight = document.documentElement.clientHeight;\n" + //
		"    logD('WebView: [' + webViewWidth + ',' + webViewHeight + ']');\n" + //
		"    logD('viewport: [' + screenWidth + ',' + screenHeight + ']');\n\n" + //

		"    finalX *= webViewWidth / screenWidth;\n" + //
		"    finalY *= webViewHeight / screenHeight;\n\n" + //

		"    logD('Button: [' + x + ',' + y + ',' + w + ',' + h + ']');\n" + //
		"    clickOnElement(finalX, finalY);\n" + //
		"};\n";

	public static final String HideNodeScript = "" + //
		"function hideNode(node) {\n" + //
		"    node.style.visibility = 'hidden';\n" + //
		"};\n";

	public static final String GetNodeByClassAndIndex = "" + //
		"function getNodeByClassAndIndex(parentNode, className, index) {\n" + //
		"    var nodes = parentNode.getElementsByClassName(className);\n" + //
		"    if(!nodes || nodes.length <= index) {\n" + //
		"        logW('Could not find element by CLASS: ' + className + ', With index: ' + index);\n" + //
		"        return;\n" + //
		"    }\n" + //
		"    return nodes[index];\n" + //
		"};\n";

	public static final String GetNodeByTagAndIndex = "" + //
		"function getNodeByTagAndIndex(parentNode, tagName, index) {\n" + //
		"    var nodes = parentNode.getElementsByTagName(tagName);\n" + //
		"    if(!nodes || nodes.length <= index) {\n" + //
		"        return;\n" + //
		"        logW('Could not find element by TAG: ' + tagName + ', With index: ' + index);\n" + //
		"    }\n" + //
		"    return nodes[index];\n" + //
		"};\n";

	public static final String WaitForNode = "" + //
		"function waitForNode(searchForNode, maxDT, dt) {\n" + //
		"    var node;\n" + //
		"    var passedDT = 0;\n" + //
		"    while(!(node = searchForNode()) && maxDT < passedDT) {\n" + //
		"        delay(dt);\n" + //
		"        passedDT += dt;\n" + //
		"    }\n" + //
		"    return node;\n" + //
		"};\n";

	public static final String SetNodeVisibilityGoneScript = "" + //
		"function setNodeVisibilityGone(node) {\n" + //
		// "    node.createAttribute('style');\n" + //
		"    node.setAttribute('style','display:none');\n" + //
		"};\n";

	public static final String SetNodeStyleToNoneScript = "" + //
		"function setNodeStyleToNoneScript(node) {\n" + //
		"    node.style='display:none';\n" + //
		"};\n";

	private static final String BaseWrappingScript = "" + //
		"javascript:(\n" + //
		"    function() {\n" + //
		"/* --------- APIS BEGIN --------- */\n" + //
		GetNodeByClassAndIndex + //
		GetNodeByTagAndIndex + //
		ClickFromAndroidScript + //
		HideNodeScript + //
		WaitForNode + //
		SetNodeVisibilityGoneScript + //
		"${APIs}\n" + //
		"/* ---------- APIS END ---------- */\n" + //
		"        var scriptToExecute = function() {\n" + //
		"            ${SCRIPT}" + //
		"        };\n" + //
		"        try {\n" + //
		"            scriptToExecute();\n" + //
		"        } catch(err) {\n" + //
		"            if(onScriptErrorListener)\n" + //
		"                onScriptErrorListener('' + err);\n" + //
		"            onScriptError('${SCRIPT_NAME}', '' + err);\n" + //
		"        }\n" + //
		"        onScriptCompleted('${SCRIPT_NAME}');\n" + //
		"    }\n" + //
		")();\n";//

	public static class JavascriptAction {

		private static volatile long ActionIndex = 0;

		private final int delay;

		private final String name;

		private final String originalScript;

		private String script;

		private String apisAsString = "";

		public JavascriptAction(String name, String script, JavascriptAPI... apis) {
			this(name, script, 0, apis);
		}

		public JavascriptAction(String name, String script, int delay, JavascriptAPI... apis) {
			this.name = name;
			this.originalScript = BaseWrappingScript.replace("${SCRIPT}", script);
			this.script = originalScript;
			this.delay = delay;

			addAPIs(ScriptStack.logAPI, ScriptStack.stackAPI, ScriptStack.webViewAPI);
			addAPIs(apis);
		}

		public void replaceText(String search, String replacement) {
			script.replace(search, replacement);
		}

		public final void addAPIs(JavascriptAPI... apis) {
			for (JavascriptAPI api : apis) {
				apisAsString += api.getAPI();
			}
		}

		final synchronized void buildAndExecute(final CyborgWebView webView) {
			final String actionName = "(" + (ActionIndex++) + ")" + name;
			final String finalScript = this.script.replace("${APIs}", apisAsString).replace("${SCRIPT_NAME}", actionName);
			script = originalScript;
			new JavascriptActionExecutor(this, finalScript, actionName, webView, delay).postInQueue();
		}

		/*/**
		 * <b>DO NOT CALL THIS METHOD EXTERNALLY!!! ONLY STACK MANAGEMENT LOGIC MAY CALL THIS METHOD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</b>
         *
         * @param actionName
         */
		protected void execute(CyborgWebView webView, String script, String actionName) {
			if (webView.isDestroyed()) {
				ScriptStack.logDebug("JavascriptStack - WebView is DESTROYED... skipping for now: " + actionName);
				ScriptStack.releaseJavaScriptExecutionThread();
				return;
			}
			if (webView.isPaused()) {
				ScriptStack.logDebug("JavascriptStack - WebView is PAUSED... skipping for now: " + actionName);
				ScriptStack.releaseJavaScriptExecutionThread();
				return;
			}
			ScriptStack.logDebug("JavascriptStack - Executing: " + actionName);
			webView.loadUrl(script);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class WebViewJavascriptStackConditionalAction
		extends JavascriptAction {

		private Condition<String> condition;

		public WebViewJavascriptStackConditionalAction(Condition<String> condition, String name, String script) {
			this(condition, name, script, 0);
		}

		public WebViewJavascriptStackConditionalAction(Condition<String> condition, String name, String script, int delay) {
			super(name, script, delay);
			this.condition = condition;
		}

		@Override
		protected final void execute(CyborgWebView webView, String script, String actionName) {
			String url = webView.getUrl();
			if (url == null || !condition.checkCondition(url)) {
				ScriptStack.logDebug("JavascriptStack - Skipping: " + actionName);
				ScriptStack.releaseJavaScriptExecutionThread();
				return;
			}
			super.execute(webView, script, actionName);
		}
	}

	static final class JavascriptActionExecutor
		implements Runnable, OnLifeCycleListener {

		JavascriptAction action;

		String finalScript;

		String actionName;

		CyborgWebView webView;

		int delay;

		private JavascriptActionExecutor(JavascriptAction action, String finalScript, String actionName, CyborgWebView webView, int delay) {
			super();
			this.action = action;
			this.finalScript = finalScript;
			this.actionName = actionName;
			this.webView = webView;
			this.delay = delay;
		}

		private void prepare() {
			webView.setLifeCycleListener(this);
		}

		@Override
		public void run() {
			if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
				/*
				 * Run this on UI THREAD!!!
				 */
				if (delay != 0)
					uiHandler.postDelayed(this, delay);
				else
					uiHandler.post(this);
				return;
			}

			action.execute(webView, finalScript, actionName);
		}

		private void cleanup() {
			webView.setLifeCycleListener(null);
		}

		private void postInQueue() {
			ScriptStack.addItem(this);
		}

		@Override
		public void onPause() {
			ScriptStack.releaseJavaScriptExecutionThread();
		}

		@Override
		public void onResumed() {}

		@Override
		public void onDestroyed() {
			// ScriptStack.releaseJavaScriptExecutionThread();
		}

		@Override
		public String toString() {
			return actionName;
		}
	}

	private final String TAG = getClass().getSimpleName();

	private final Object Monitor = new Object();

	private final LogAPI logAPI = new LogAPI();

	private final StackAPI stackAPI = new StackAPI();

	private final WebViewAPI webViewAPI = new WebViewAPI();

	final String OnLoadJS = "javascript:(\n" + //
		"    function() {\n" + //
		webViewAPI.getAPI() + //
		"        if(!('documentOffsetTop' in Element.prototype)) \n" + //
		"            window.Object.defineProperty( Element.prototype, 'documentOffsetTop', {\n" + //
		"                get: function () {\n" + //
		"                    var offset = this.offsetTop + ( this.offsetParent ? this.offsetParent.documentOffsetTop : 0 );\n" + //
		"                    return offset + parseFloat((this.currentStyle || window.getComputedStyle(this)).marginTop);\n" + //
		"                }\n" + //
		"            });\n" + //
		"        if(!('documentOffsetLeft' in Element.prototype)) \n" + //
		"            window.Object.defineProperty( Element.prototype, 'documentOffsetLeft', {\n" + //
		"                get: function () {\n" + //
		"                    var offset = this.offsetLeft + ( this.offsetParent ? this.offsetParent.documentOffsetLeft : 0 );\n" + //
		"                    return offset + parseFloat((this.currentStyle || window.getComputedStyle(this)).marginLeft);\n" + //
		"                }\n" + //
		"            });\n" + //
		"        window.onload = function() {\n" + //
		"            onWindowLoaded(window.location.href);\n" + //
		"        };\n" + //
		"    }\n" + //
		")();\n";//

	static Handler uiHandler;

	private Runnable interruptThread = new Runnable() {

		@Override
		public void run() {
			getThreads()[0].interrupt();
		}
	};

	private final Cyborg cyborg;

	private final ILogger logger;

	private static JavascriptStack ScriptStack;

	private JavascriptStack(Cyborg cyborg) {
		uiHandler = new Handler(Looper.getMainLooper());
		this.cyborg = cyborg;
		logger = cyborg.getLogger(this);

		createThreads("Javascript Stack - Action PoolQueue Executer");
	}

	@Override
	protected void onExecutionError(JavascriptActionExecutor scriptExecuter, Throwable e) {
		logError(e);
	}

	void releaseJavaScriptExecutionThread() {
		synchronized (Monitor) {
			Monitor.notify();
		}
	}

	@Override
	protected void executeAction(JavascriptActionExecutor scriptExecuter) {
		synchronized (Monitor) {
			scriptExecuter.prepare();
			scriptExecuter.run();
			uiHandler.postDelayed(interruptThread, 15000);
			try {
				Monitor.wait();
			} catch (Exception e) {
				logError("Interrupted Javascript action: " + scriptExecuter);
			}
			uiHandler.removeCallbacks(interruptThread);
			scriptExecuter.cleanup();
		}
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

	public final void initWebView(CyborgWebView webView) {
		WebViewAPI webViewApi = new WebViewAPI(webView);

		webViewApi.appendToWebView(webView);
		logAPI.appendToWebView(webView);
		stackAPI.appendToWebView(webView);
	}

	@Override
	public void sendView(String screnName) {
		cyborg.sendView(screnName);
	}

	@Override
	public void sendEvent(String category, String action, String label, long count) {
		cyborg.sendEvent(category, action, label, count);
	}

	@Override
	public void sendException(String description, Throwable t, boolean crash) {
		cyborg.sendException(description, t, crash);
	}

	public static JavascriptStack getInstance() {
		if (ScriptStack == null)
			ScriptStack = new JavascriptStack(CyborgBuilder.getInstance());
		return ScriptStack;
	}
}
