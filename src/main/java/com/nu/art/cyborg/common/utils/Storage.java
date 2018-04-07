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

package com.nu.art.cyborg.common.utils;

import android.os.Environment;
import android.os.StatFs;

import com.nu.art.cyborg.common.beans.StoragePath;
import com.nu.art.cyborg.core.CyborgBuilder;

import java.io.File;

public enum Storage {
	ExternalStorage {
		@Override
		public boolean isAccessible() {
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state) || !new File(getPath()).exists()) {
				return false;
			}
			return true;
		}

		@Override
		public long freeSpace() {
			if (!isAccessible()) {
				return 0;
			}
			StatFs stat = new StatFs(getPath());
			return (long) stat.getBlockSize() * (long) stat.getFreeBlocks();
		}

		@Override
		public String getPath() {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
	},
	PhoneStorage {
		@Override
		public boolean isAccessible() {
			return true;
		}

		@Override
		public long freeSpace() {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			long internal_memory_left = availableBlocks * blockSize;
			return internal_memory_left;
		}

		@Override
		public String getPath() {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/" + CyborgBuilder.getInstance().getPackageName();
		}
	};

	private boolean _isInPath(String path) {
		return path.startsWith(getPath());
	}

	/**
	 * @return Whether the storage resource is available.
	 */
	public abstract boolean isAccessible();

	/**
	 * @return The free space the storage resource has.
	 */
	public abstract long freeSpace();

	public abstract String getPath();

	public static final boolean isInPath(String path) {
		for (Storage storage : values()) {
			if (storage._isInPath(path)) {
				return true;
			}
		}
		return false;
	}

	public static Storage getDefaultStorage() {
		return ExternalStorage.isAccessible() ? ExternalStorage : PhoneStorage;
	}

	public static Storage getStorageByPath(String path) {
		for (Storage storage : values()) {
			if (storage._isInPath(path)) {
				return storage;
			}
		}
		return null;
	}

	public static final StoragePath getBestStorage(String prePath) {
		File sdCard = new File(Storage.ExternalStorage.getPath());
		if (!sdCard.exists())
			return getLocalStorageFolder(prePath);
		return new StoragePath(ExternalStorage, Storage.ExternalStorage.getPath() + getPrePath(prePath));
	}

	private static StoragePath getLocalStorageFolder(String prePath) {
		return new StoragePath(PhoneStorage, PhoneStorage.getPath() + getPrePath(prePath));
	}

	private static String getPrePath(String prePath) {
		return "/" + CyborgBuilder.getInstance().getPackageName() + (prePath.startsWith("/") ? "" : "/") + prePath;
	}
}
