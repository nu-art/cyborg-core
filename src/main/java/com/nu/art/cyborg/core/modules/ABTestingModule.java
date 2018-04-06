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

package com.nu.art.cyborg.core.modules;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.MathTools;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.FloatPreference;

import java.util.HashMap;
import java.util.Random;

public class ABTestingModule
	extends CyborgModule {

	private static final String StorageGroup = "ab-testing";

	private final Random random = new Random();

	private final HashMap<String, float[]> abRatings = new HashMap<>();

	public final void addRating(String key, float[] rating) {
		float sum = MathTools.sum(rating);

		if (sum != 1) {
			float ratio = 1 / sum;
			for (int i = 0; i < rating.length; i++) {
				rating[i] = rating[i] *= ratio;
			}
		}

		abRatings.put(key, rating);
	}

	@Override
	protected void init() {}

	// need to save the result index for this client per ab testing key and reuse it, unless specified otherwise by the developer, for a consistent UX
	public final <T> T calcABTesting(String key, T... options) {
		return calcABTesting(key, true, options);
	}

	public final <T> T calcABTesting(String key, boolean saveResult, T... options) {
		if (options.length == 0)
			throw new BadImplementationException("No option specified");

		float[] rating = abRatings.get(key);
		if (rating == null) {
			logWarning("No rating specified for key: " + key + ", returning first value as default!");
			return options[0];
		}

		if (options.length > rating.length)
			logWarning("Will never return later elements after index: " + rating.length);

		FloatPreference storedResult = getModule(PreferencesModule.class).new FloatPreference("ABTesting_" + key, -1, StorageGroup);
		float previousFloat = storedResult.get();
		if (saveResult && previousFloat != -1)
			return getOptionFromRatio(rating, previousFloat, options);

		float randomValue = random.nextInt(1000) * 1f / 1000;
		if (saveResult)
			storedResult.set(randomValue);

		return getOptionFromRatio(rating, randomValue, options);
	}

	private <T> T getOptionFromRatio(float[] rating, float randomValue, T[] options) {
		int index = -1;
		for (int i = 0; i < rating.length; i++) {
			if (randomValue > rating[i])
				continue;

			if (options.length < rating.length) {
				index = Math.round(1f * i / rating.length * options.length);
				logWarning("Options count does not match ratings count, will return relative index: " + index + ", instead of index: " + i);
				break;
			}

			index = i;
			break;
		}

		if (index == -1)
			index = options.length - 1;

		return options[index];
	}
}
