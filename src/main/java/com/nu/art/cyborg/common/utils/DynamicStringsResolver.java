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

package com.nu.art.cyborg.common.utils;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.abs.Cyborg;

public final class DynamicStringsResolver
		implements StringResourceResolver {

	public static enum Case {
		Upper,
		Lower,
		None
	}

	private Case defaultCase = Case.None;

	private int stringId;

	private Object[] params;

	public DynamicStringsResolver(int stringId, Case defaultCase, Object... params) {
		super();
		this.defaultCase = defaultCase;
		this.stringId = stringId;
		this.params = params;
	}

	public DynamicStringsResolver(int stringId, Object... params) {
		super();
		this.stringId = stringId;
		this.params = params;
	}

	@Override
	public String getString(Cyborg cyborg) {
		return applyCase(cyborg);
	}

	private String applyCase(Cyborg cyborg) {
		String string = resolveString(cyborg);
		switch (defaultCase) {
			case Lower:
				return string.toLowerCase();
			case None:
				return string;
			case Upper:
				return string.toUpperCase();
		}
		throw new MUST_NeverHappenedException("STUPID JAVA");
	}

	private String resolveString(Cyborg cyborg) {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				if (params[i] instanceof StringResourceResolver)
					params[i] = ((StringResourceResolver) params[i]).getString(cyborg);
			}
			return cyborg.getString(stringId, params);
		}
		return cyborg.getString(stringId);
	}
}
