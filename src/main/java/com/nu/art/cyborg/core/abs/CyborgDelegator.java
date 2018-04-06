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

public interface CyborgDelegator
	extends _Analytics, _Resources, _Toasts, _Debug, _UI {

	/**
	 * Vibrate for the provided ms.
	 *
	 * @param ms the interval to vibrate.
	 */
	void vibrate(long ms);

	/**
	 * Vibrate for the provided pattern.
	 *
	 * @param repeat  should the pattern repeat.
	 * @param pattern the pattern to vibrate.
	 */
	void vibrate(int repeat, long... pattern);

	/**
	 * @return The time in millis since last factory reset.
	 */
	long elapsedTimeMillis();
}
