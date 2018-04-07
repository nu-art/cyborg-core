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

package com.nu.art.cyborg.common.beans;

import android.content.ComponentName;
import android.content.Context;

import com.nu.art.cyborg.common.consts.ComponentType;

public final class FullComponentName {

	private ComponentType type;

	private String className;

	private String packageName;

	private final ComponentName componentName;

	public FullComponentName(ComponentType type, Context context, String className) {
		this(type, context.getPackageName(), className);
	}

	public FullComponentName(ComponentType type, Context context, Class<?> classType) {
		this(type, context.getPackageName(), classType.getName());
	}

	public FullComponentName(ComponentType type, String packageName, String className) {
		super();
		this.type = type;
		this.packageName = packageName;
		this.className = className;
		componentName = new ComponentName(packageName, className);
	}

	public final ComponentName getComponentName() {
		return componentName;
	}

	public final ComponentType getType() {
		return type;
	}

	public final String getClassName() {
		return className;
	}

	public final String getPackageName() {
		return packageName;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (o instanceof ComponentName)
			return equals((ComponentName) o);

		if (o instanceof FullComponentName)
			return equals((FullComponentName) o);
		return super.equals(o);
	}

	private boolean equals(FullComponentName o) {
		if (o.type != type)
			return false;
		return equals(o.getComponentName());
	}

	private boolean equals(ComponentName o) {
		if (!o.getClassName().equals(className))
			return false;
		if (!o.getPackageName().equals(packageName))
			return false;
		return true;
	}
}
