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

package com.nu.art.cyborg.modules;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;

import java.util.HashMap;

@ModuleDescriptor(usesPermissions = {})
public final class AttributeModule
	extends CyborgModule {

	public static abstract class AttributesSetter<Expected>
		extends CyborgModuleItem {

		final Class<Expected> expectedType;

		final int[] styleableId;

		final int[] attributeIds;

		public AttributesSetter(Class<Expected> expectedType, int[] styleableId, int... attributeIds) {
			super();
			this.expectedType = expectedType;
			this.styleableId = styleableId;
			this.attributeIds = attributeIds;
		}

		protected final boolean isInEditMode() {
			return cyborg.isInEditMode();
		}

		private void resolveAttributes(Context context, AttributeSet attrs, Expected instance) {
			TypedArray a = context.obtainStyledAttributes(attrs, styleableId);
			try {
				if (a.length() == 0)
					return;

				for (int attributeId : attributeIds) {
					setAttribute(instance, a, attributeId);
				}
			} finally {
				a.recycle();
			}
			onSettingCompleted(instance);
		}

		private int getAttr(TypedArray a, int i) {
			try {
				return a.getIndex(i);
			} catch (Exception e) {
				e.printStackTrace();
				return -2;
			}
		}

		protected void onSettingCompleted(Expected instance) {}

		protected abstract void setAttribute(Expected instance, TypedArray a, int attr);

		protected void init() {}

		@SuppressWarnings( {
			                   "unchecked",
			                   "UnusedParameters"
		                   })
		protected final <Type> Class<? extends Type> resolveClassType(Class<Type> type, String className) {
			if (className == null || className.length() == 0)
				throw ExceptionGenerator.noValueForControllerClassNameSpecified();

			if (className.startsWith("."))
				className = cyborg.getPackageName() + className;

			try {
				return (Class<? extends Type>) getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				throw ExceptionGenerator.invalidControllerClassNameSpecified(className, e);
			}
		}
	}

	private HashMap<Class<?>, AttributesSetter<?>> settersMap = new HashMap<>();

	@Override
	protected void init() {
		for (AttributesSetter<?> setter : settersMap.values()) {
			setter.init();
		}
	}

	public final <SetterType extends AttributesSetter<?>> SetterType registerAttributesSetter(Class<SetterType> setterType) {
		SetterType setter = createModuleItem(setterType);
		settersMap.put(setter.expectedType, setter);
		return setter;
	}

	@SuppressWarnings("unchecked")
	public final <SetterType extends AttributesSetter<?>> SetterType getAttributesSetter(Class<SetterType> setterType) {
		return (SetterType) settersMap.get(setterType);
	}

	public void setAttributes(Context context, AttributeSet attrs, Object instance) {
		if (attrs == null)
			throw new BadImplementationException("Cannot build rootView without reference to a controller type!");

		Class<?> type = instance.getClass();
		while (type != Object.class) {
			resolveAttributes(context, attrs, instance, type);
			for (Class<?> _interface : type.getInterfaces()) {
				resolveAttributes(context, attrs, instance, _interface);
			}
			type = type.getSuperclass();
		}
	}

	@SuppressWarnings("unchecked")
	private <ExpectedType> void resolveAttributes(Context context, AttributeSet attrs, Object instance, Class<?> type) {
		AttributesSetter<ExpectedType> setter = (AttributesSetter<ExpectedType>) settersMap.get(type);
		if (setter == null)
			return;

		setter.resolveAttributes(context, attrs, (ExpectedType) instance);
	}
}
