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

package com.nu.art.cyborg.ui.animations.consts;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;

/**
 * Created by TacB0sS on 28-Jul 2015.
 */
public enum Direction {
	Up,
	Down,
	Left,
	Right;

	public static Direction getOpposite(Direction direction) {
		switch (direction) {
			case Up:
				return Down;
			case Down:
				return Up;
			case Left:
				return Right;
			case Right:
				return Left;
			default:
				throw new MUST_NeverHappenedException("");
		}
	}
}
