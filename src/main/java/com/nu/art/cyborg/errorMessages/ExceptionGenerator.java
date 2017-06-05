package com.nu.art.cyborg.errorMessages;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.core.CyborgController;

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
}
