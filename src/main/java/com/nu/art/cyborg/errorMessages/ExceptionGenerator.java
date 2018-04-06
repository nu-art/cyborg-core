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

import android.app.Service;
import android.view.View;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.cyborg.common.consts.ViewListener;
import com.nu.art.cyborg.core.CyborgController;

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
		String controllerClassName = cyborgController.getClass().getSimpleName();
		String errorMessage = "MUST specify a valid layoutId in the controller constructor or override createCustomView method in controller " + controllerClassName + "!";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerDidNotSetViewIdForViewInjector(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String errorMessage = "You MUST set valid viewId id for field '" + fieldName + "' in class, '" + classSimpleName + "'";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdForViewArrayInjector(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String errorMessage = "You MUST NOT set a viewId id for array of views in field '" + fieldName + "' in class, '" + classSimpleName + "'";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerDidNotSetViewIdsForViewArrayInjector(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String errorMessage = "You MUST set valid viewIds id for field '" + fieldName + "' in class, '" + classSimpleName + "'";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdentifierAnnotationToMemberWithUnsupportedType(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String errorMessage = "ViewIdentifier Annotation was set to an unsupported type: " + fieldName + " in class, '" + classSimpleName;
		errorMessage += "\n Annotation can only be set to a member assignable from View, View[], CyborgController, CyborgController[]!";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdOfIncompatibleViewForController(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String message = "View id does not belong to a CyborgView in field '" + fieldName + "' in class, '" + classSimpleName + "'";
		return new BadImplementationException(message);
	}

	public static BadImplementationException couldNotFindViewForViewIdInLayout(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();

		return new BadImplementationException("Could not find view for field '" + fieldName + "' in class, '" + classSimpleName + "'");
	}

	public static BadImplementationException wrongListenerToViewAssignment(View view, ViewListener listener) {
		String viewSimpleName = view.getClass().getSimpleName();
		String listenerSimpleName = listener.getMethodOwnerType().getSimpleName();
		return new BadImplementationException("Cannot assign '" + listener + "' listener to type: " + viewSimpleName + ", it does not inherit from super type: " + listenerSimpleName);
	}

	public static BadImplementationException errorWhileAssigningListenerToView(Exception e) {
		return new BadImplementationException("Error while assigning listener to view", e);
	}

	public static ImplementationMissingException developerDidNotAddTheServiceToTheManifest(Class<? extends Service> serviceType) {
		String message = "<service android:name=\"" + serviceType.getName() + "\"/>";
		return new ImplementationMissingException("It might be that you didn't add the service to your manifest:\n\n   " + message);
	}
}
