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

import android.os.Handler;
import android.view.animation.Animation;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;

public interface _UI {

	Handler getUI_Handler();

	void postOnUI(int delay, Runnable action);

	void postOnUI(Runnable action);

	void removeAndPostOnUI(Runnable action);

	void removeAndPostOnUI(int delay, Runnable action);

	void removeActionFromUI(Runnable action);

	void postActivityAction(ActivityStackAction action);

	<ListenerType> void dispatchEvent(final Class<ListenerType> listenerType, final Processor<ListenerType> processor);

	Animation loadAnimation(int animationId);
}
