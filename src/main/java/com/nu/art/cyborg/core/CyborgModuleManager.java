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

package com.nu.art.cyborg.core;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.modular.core.ModuleManager;

import static com.nu.art.cyborg.core.abs.Cyborg.paramExtractor;

/**
 * This is an internal object.
 */
public final class CyborgModuleManager
	extends ModuleManager {

	CyborgModuleManager() {
		super(paramExtractor);
	}

	protected final <ListenerType> void dispatchModuleEvent(ILogger originator,
	                                                        String message,
	                                                        Class<ListenerType> listenerType,
	                                                        Processor<ListenerType> processor) {
		super.dispatchModuleEvent(originator, message, listenerType, processor);
	}
}