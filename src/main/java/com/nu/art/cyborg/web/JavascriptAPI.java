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

package com.nu.art.cyborg.web;

import android.annotation.SuppressLint;
import android.webkit.WebView;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.interfaces.Obfuscation_KeepMethods;
import com.nu.art.core.interfaces.Obfuscation_KeepMethodsNames;
import com.nu.art.cyborg.annotations.JavascriptBridgeMethod;
import com.nu.art.reflection.tools.ART_Tools;

import java.lang.reflect.Method;

/**
 * An wrapping API to use for {@link CyborgWebView} java script calls.<br>
 * in order to use with Proguard, one MUSt add the following lines to the '<b>proguard-project.txt</b>' project file: <br>
 * <code>-keep class ** implements Obfuscation_KeepMethods {<br>
 * <i># Keep Javascript callback names</i><br>
 * -keepclasseswithmembernames class ** extends com.nu.art.cyborg.web.WebViewJavascriptAPI {<br>
 * }</code><br>
 * <br>
 *
 * @author TacB0sS
 */
public abstract class JavascriptAPI
	implements Obfuscation_KeepMethods, Obfuscation_KeepMethodsNames {

	private String api;

	private final String name;

	protected JavascriptAPI(String name) {
		super();
		this.name = name;
		// Method[] methods = getClass().getMethods();
		// for (Method method : methods) {
		// JavascriptBridgeMethod annotation = method.getAnnotation(JavascriptBridgeMethod.class);
		// String methodName = method.getName();
		// if (annotation != null)
		// methodName = annotation.methodName();
		// Log.w("DEBUG_PRODUCTION", "Real name: " + methodName + " Obfuscated Name: " + method.getName());
		// }
	}

	public final String getName() {
		return name;
	}

	public final String getAPI() {
		if (api == null)
			api = getJavascriptAPI();
		return api;
	}

	private final String getJavascriptAPI() {
		StringBuilder stringBuilder = new StringBuilder();
		Method[] methods = ART_Tools.getAllMethodsWithAnnotation(getClass(), JavascriptBridgeMethod.class);
		stringBuilder.append("/* ------------------ API BEGIN: " + name + " ------------------ */\n");

		for (Method method : methods) {
			JavascriptBridgeMethod bridgeMethod = method.getAnnotation(JavascriptBridgeMethod.class);
			String methodName = bridgeMethod.methodName();
			int parametersCount = method.getParameterTypes().length;
			String variables = "";
			if (parametersCount > 0)
				variables = "arg0";
			for (int i = 1; i < parametersCount; i++) {
				variables += ", arg" + i;
			}
			stringBuilder.append("var ").append(methodName).append(" = ");
			stringBuilder.append("function ").append(methodName).append("(").append(variables).append(") { \n");
			stringBuilder.append("    window.").append(name).append(".").append(method.getName()).append("(").append(variables).append(");\n");
			stringBuilder.append("};\n");
		}
		stringBuilder.append("/* ------------------ API END: " + name + " ------------------ */");

		return stringBuilder.toString();
	}

	@SuppressLint( {
		               "JavascriptInterface",
		               "AddJavascriptInterface"
	               })
	public final void appendToWebView(WebView webView) {
		if (webView == null)
			throw new BadImplementationException("API: " + name + " - WEBVIEW == null");

		webView.addJavascriptInterface(this, name);
	}
}
