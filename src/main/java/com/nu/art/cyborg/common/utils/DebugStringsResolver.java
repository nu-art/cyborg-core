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

import com.nu.art.software.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.abs.Cyborg;

public final class DebugStringsResolver
		implements StringResourceResolver {

	private String text;

	public DebugStringsResolver(String text) {
		super();
		this.text = text;
	}

	@Override
	public String getString(Cyborg cyborg) {
		if (!cyborg.isDebuggable())
			throw new BadImplementationException("THIS is a debug utility... DO NOT USE IN PRODUCTION! or the application would crash!");
		return text;
	}
}
