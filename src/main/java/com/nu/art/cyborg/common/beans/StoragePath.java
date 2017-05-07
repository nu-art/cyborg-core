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

package com.nu.art.cyborg.common.beans;

import java.io.File;
import java.io.IOException;

import com.nu.art.software.core.tools.FileTools;
import com.nu.art.cyborg.common.utils.Storage;

public final class StoragePath {

	private Storage type;

	private File file;

	public StoragePath(Storage type, String prePath) {
		this(type, new File(prePath));
	}

	public StoragePath(Storage type, File file) {
		super();
		this.type = type;
		this.file = file;
	}

	public final File getFile() {
		return file;
	}

	public final String getFileAsString(String pathPostfix) {
		return new File(file, pathPostfix).getAbsolutePath();
	}

	public final StoragePath getPath(String pathPostfix) {
		return new StoragePath(type, new File(file, pathPostfix).getAbsolutePath());
	}

	public final long getAvailableSpace() {
		return type.freeSpace();
	}

	public void delete()
			throws IOException {
		FileTools.delete(getFile());
	}

	public boolean exists() {
		return getFile().exists();
	}
}
