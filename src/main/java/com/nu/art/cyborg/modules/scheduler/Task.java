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

package com.nu.art.cyborg.modules.scheduler;

import com.nu.art.modular.core.ModuleItem;

/**
 * Created by TacB0sS on 13-Nov 2016.
 */

public abstract class Task<DataType>
	extends ModuleItem {

	protected short id;

	final Class<DataType> dataType;

	protected Task(Class<DataType> dataType) {this.dataType = dataType;}

	@Override
	protected void init() {

	}

	protected abstract void execute(DataType dataType);

	final short getId() {
		return 0;
	}
}
