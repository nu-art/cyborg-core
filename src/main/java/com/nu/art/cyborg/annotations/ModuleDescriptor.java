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

package com.nu.art.cyborg.annotations;

import android.Manifest.permission;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.modular.core.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Defines some properties for a {@link CyborgModule}
 *
 * @author TacB0sS
 */
@Target( {TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDescriptor {

	/**
	 * Upon launch the application Manifest would be checked for these uses-permissions<br>
	 * <br>
	 * Most permissions can be acquired by the static fields in {@link permission} object.
	 *
	 * @return An array of the expected permissions to be used in the application and must be found in the project's
	 * Manifest.
	 */
	String[] usesPermissions() default {};

	/**
	 * Some applications may require to define permissions in order to use a service.<br>
	 * Upon launch the application Manifest would be checked for these uses-permissions<br>
	 *
	 * @return An array of the expected permissions to be defined in the project's Manifest.
	 */
	String[] definedPermissions() default {};

	/**
	 * Upon launch your application would be check for these dependency modules array, and would throw a
	 * {@link ImplementationMissingException} if one or more of these modules are not found!
	 *
	 * @return an array of {@link Module} types.
	 */
	Class<? extends Module>[] dependencies() default {};

	/**
	 * Upon launch your application would be checked for these features and would throw a
	 * {@link BadImplementationException} if one of features are not found! <br>
	 * <br>
	 * Most features can be acquired by the static fields in {@link permission} object.
	 *
	 * @return an array of permission strings.
	 */
	String[] features() default {};
}
