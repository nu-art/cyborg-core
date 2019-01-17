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

package com.nu.art.cyborg.core.abs;

import android.os.Handler;
import android.view.animation.Animation;

import androidx.annotation.AnimRes;

import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;

public interface _UI {

	/**
	 * @return The one and ONLY instance of the UI handler
	 */
	Handler getUI_Handler();

	/**
	 * Will execute the provided action on the UI thread once the specified delay expires.
	 *
	 * @param delay  The delay interval before executing the provided {@link Runnable}
	 * @param action The runnable to execute once interval expires.
	 */
	void postOnUI(long delay, Runnable action);

	/**
	 * Will execute the provided action on the UI thread as soon as possible.
	 *
	 * @param action The runnable to execute.
	 */
	void postOnUI(Runnable action);

	/**
	 * Remove an action and post it again for execution.
	 *
	 * @param action The runnable to execute.
	 */
	void removeAndPostOnUI(Runnable action);

	/**
	 * Remove an action and post it again for execution after the specified interval.
	 *
	 * @param delay  The delay interval before executing the provided {@link Runnable}
	 * @param action The runnable to be removed and execute once interval expires.
	 */
	void removeAndPostOnUI(long delay, Runnable action);

	/**
	 * Remove the specified runnable from the UI handler.
	 *
	 * @param action The runnable to be removed from the UI handler queue.
	 */
	void removeActionFromUI(Runnable action);

	/**
	 * @param action The action to run once an activity is in foreground.
	 */
	void postActivityAction(ActivityStackAction action);

	/**
	 * Utility api to load animations
	 *
	 * @param animationId The animation id to load from the resources.
	 *
	 * @return an instance of the animation.
	 */
	Animation loadAnimation(@AnimRes int animationId);
}
