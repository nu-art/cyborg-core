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

package com.nu.art.cyborg.common.interfaces;

import android.text.Editable;
import android.widget.TextView;

/**
 * A better implementation for the listening to the changes of multiple EditTexts...
 */
public interface OnTextChangedListener {

	void beforeTextChanged(TextView view, CharSequence string, int start, int count, int after);

	void onTextChanged(TextView view, CharSequence string, int start, int before, int count);

	void afterTextChanged(TextView view, Editable editableValue);
}
