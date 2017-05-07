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

import com.nu.art.cyborg.common.interfaces.StringResourceResolver;

interface _Toasts {

	void toastDebug(String text);

	void toast(StringResourceResolver stringResolver, int length);

	void toast(int length, String text);

	void toast(int length, int stringId, Object... params);

	void toastShort(int stringId, Object... args);

	void toastLong(int stringId, Object... args);
}
