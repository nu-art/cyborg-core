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

package com.nu.art.cyborg.errorMessages;

import android.view.View;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenedException;
import com.nu.art.cyborg.common.consts.ViewListener;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.reflection.tools.ART_Tools;

import java.lang.reflect.Field;

/**
 * Created by tacb0ss on 02/06/2017.
 */

public class ExceptionGenerator {

	private ExceptionGenerator() {
		throw new BadImplementationException("Don't be naughty");
	}

	public static BadImplementationException noValueForControllerClassNameSpecified() {
		return new BadImplementationException("Expected a controller class FQN, but attribute value was empty");
	}

	public static BadImplementationException invalidControllerClassNameSpecified(String className, ClassNotFoundException e) {
		return new BadImplementationException("Expected a controller class FQN, but the value did not match to a class:\n" //
				+ "value found: " + className + "\n" //
				+ "if this is an issue of refactoring, please go and favorite this feature request: " + ErrorLinks.ClassNameRefactoringNotAppliedToXML, e);
	}

	public static BadImplementationException didNotProvideLayoutIdOrCustomView(CyborgController cyborgController) {
		return new BadImplementationException("MUST specify a valid layoutId in the controller constructor or override createCustomView method in controller " + cyborgController
				.getClass().getSimpleName() + "!");
	}

	public static BadImplementationException developerDidNotSetViewIdForViewInjector(Field viewField) {
		return new BadImplementationException("You MUST set valid viewId id for field '" + viewField.getName() + "' in class, '" + viewField.getDeclaringClass()
				.getSimpleName() + "'");
	}

	public static BadImplementationException developerSetViewIdForViewArrayInjector(Field viewField) {
		return new BadImplementationException("You MUST NOT set a viewId id for array of views in field '" + viewField.getName() + "' in class, '" + viewField
				.getDeclaringClass().getSimpleName() + "'");
	}

	public static BadImplementationException developerDidNotSetViewIdsForViewArrayInjector(Field viewField) {
		return new BadImplementationException("You MUST set valid viewIds id for field '" + viewField.getName() + "' in class, '" + viewField.getDeclaringClass()
				.getSimpleName() + "'");
	}

	public static ThisShouldNotHappenedException infraErrorInTheArtTools(Class<?> declaringClass) {
		return new ThisShouldNotHappenedException("Extracting fields error using: '" + ART_Tools.class.getName() + "' from class '" + declaringClass
				.getName() + "'");
	}

	public static BadImplementationException couldNotFindViewForViewIdInLayout(Field viewField) {
		return new BadImplementationException("Could not find the supplied viewId in layout for field '" + viewField.getName() + "' in class, '" + viewField
				.getDeclaringClass().getSimpleName() + "'");
	}

	public static BadImplementationException wrongListenerToViewAssignment(View view, ViewListener listener) {
		return new BadImplementationException("Cannot assign '" + listener + "' listener to type: " + view.getClass()
				.getSimpleName() + ", it does not inherit from super type: " + listener.getMethodOwnerType().getSimpleName());
	}

	public static BadImplementationException errorWhileAssigningListenerToView(Exception e) {
		throw new BadImplementationException("Error while assigning listener to view", e);
	}
}
