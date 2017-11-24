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

package com.nu.art.cyborg.core;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.consts.DebugFlags;
import com.nu.art.modular.core.ModuleManager;

/**
 * This is an internal object.
 */
public final class CyborgModuleManager
		extends ModuleManager {

	CyborgModuleManager() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected <ListenerType> void dispatchModuleEvent(String message, Processor<ListenerType> processor) {
		Class<ListenerType> _listenerType = DebugFlags.paramExtractor.extractGenericTypeFromProcessorTest(null, processor);
		if (_listenerType == null)
			throw new MUST_NeverHappenedException("Cannot resolve processor type from instance, " + processor.getClass().getSimpleName());

		super.dispatchModuleEvent(message, _listenerType, processor);
	}

	@SuppressWarnings("unchecked")
	protected <ListenerType> void dispatchModuleEvent(String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		dispatchModuleEvent(message, processor);
	}
}
