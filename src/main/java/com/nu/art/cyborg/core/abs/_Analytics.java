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

package com.nu.art.cyborg.core.abs;

public interface _Analytics {

	/**
	 * Will dispatch module event to handle screen viewed Analytics.
	 *
	 * @param screenName The screen name to send to analytics.
	 */
	void sendView(String screenName);

	/**
	 * Will dispatch module event to handle event Analytics.
	 *
	 * @param category The category to send to analytics.
	 * @param action   The action to send to analytics.
	 * @param label    The label to send to analytics.
	 * @param value    The value to send to analytics.
	 */
	void sendEvent(String category, String action, String label, long value);

	/**
	 * Will dispatch module event to handle error Analytics.
	 *
	 * @param description The error description.
	 * @param t           The exception if exists.
	 * @param crash       Did the app crash or not due to the exception.
	 */
	void sendException(String description, Throwable t, boolean crash);
}
