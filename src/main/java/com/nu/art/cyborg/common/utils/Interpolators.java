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

package com.nu.art.cyborg.common.utils;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by TacB0sS on 23/11/2017.
 */

public interface Interpolators {

	LinearInterpolator LinearInterpolator = new LinearInterpolator();

	AccelerateDecelerateInterpolator AccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

	AccelerateInterpolator AccelerateInterpolator = new AccelerateInterpolator();

	DecelerateInterpolator DecelerateInterpolator = new DecelerateInterpolator();
}
