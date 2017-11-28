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

package com.nu.art.cyborg.modules;

import com.nu.art.core.GenericListener;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.core.utils.RunnableQueue;
import com.nu.art.cyborg.core.CyborgModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tacb0ss on 14/06/2017.
 */
public class CacheModule
		extends CyborgModule {

	public class Cacheable {

		private String key;

		private String suffix;

		private String pathToDir;

		private long interval;

		/**
		 * @param key The key that would be used to identify the cached item, can be any unique string you want.
		 *
		 * @return The instance {@link Cacheable} you currently edit
		 */
		public Cacheable setKey(String key) {
			this.key = key;
			return this;
		}

		/**
		 * Apparently Android in some cases does not redetermine the file type dynamically, and adding a suffix help entities like the MediaPlayer, to play the
		 * local cached file correctly
		 *
		 * @param suffix The file suffix would have
		 *
		 * @return The instance {@link Cacheable} you currently edit
		 */
		public Cacheable setSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		/**
		 * @param pathToDir The path you would like to save the cached file to, if not specified, would be saved to the cache or files folder depends if an
		 *                  interval
		 *                  was specified.
		 *
		 * @return The instance {@link Cacheable} you currently edit
		 */
		public Cacheable setPathToDir(String pathToDir) {
			this.pathToDir = pathToDir;
			return this;
		}

		/**
		 * If not specified, file would be saved to the app files folder and will be stored there, otherwise file would be saved to the app cache folder, and
		 * Android would manage it lifespan.
		 *
		 * @param interval The interval the cache is valid for.
		 *
		 * @return The instance {@link Cacheable} you currently edit
		 */
		public Cacheable setInterval(long interval) {
			this.interval = interval;
			return this;
		}

		/**
		 * @return Whether or not this item is cached.
		 */
		public boolean isCached() {
			return CacheModule.this.isCached(this);
		}

		/**
		 * @return The file pointing to the cached path of the item, <b>regardless if it is cached or not!!</b>
		 */
		public File getLocalCacheFile() {
			return CacheModule.this.getFile(this);
		}

		/**
		 * Cache the item on the cache queue pool.
		 *
		 * @param inputStream the input stream to cache.
		 * @param listener    to be notified on when completed.
		 */
		public void cacheAsync(InputStream inputStream, CacheListener listener) {
			CacheModule.this.cacheAsync(this, inputStream, listener);
		}

		/**
		 * Cache the item on the calling thread.
		 *
		 * @param inputStream the input stream to cache.
		 *
		 * @throws IOException if something goes wrong.
		 */
		public void cacheSync(InputStream inputStream)
				throws IOException {
			CacheModule.this.cacheSync(this, inputStream);
		}

		public void load(GenericListener<InputStream> listener) {
			CacheModule.this.load(this, listener);
		}
	}

	public interface CacheListener {

		void onItemCacheError(Cacheable key, Exception e);

		void onItemCacheCompleted(Cacheable key);
	}

	private RunnableQueue cacheQueue = new RunnableQueue();

	private File filesDir;

	private File cacheDir;

	private int threadCount = 5;

	@Override
	protected void init() {
		filesDir = getApplicationContext().getFilesDir();
		cacheDir = getApplicationContext().getCacheDir();
		cacheQueue.createThreads("Caching Thread", threadCount);
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	private boolean isCached(Cacheable cacheable) {
		File file = getFile(cacheable);
		if (!file.exists())
			return false;

		if (!file.isFile())
			return false;

		if (file.length() == 0)
			return false;

		if (cacheable.interval == 0)
			return true;

		return System.currentTimeMillis() - file.lastModified() > cacheable.interval;
	}

	private File getFile(Cacheable cacheable) {
		File dir;
		if (cacheable.pathToDir != null)
			dir = new File(cacheable.pathToDir);
		else
			dir = cacheable.interval > 0 ? filesDir : cacheDir;

		return new File(dir, cacheable.key.hashCode() + "." + cacheable.suffix);
	}

	private void cacheAsync(final Cacheable cacheable, final InputStream inputStream, final CacheListener listener) {
		cacheQueue.addItem(new Runnable() {

			@Override
			public void run() {
				try {
					cacheSync(cacheable, inputStream);
					listener.onItemCacheCompleted(cacheable);
				} catch (final IOException e) {
					listener.onItemCacheError(cacheable, e);
				}
			}
		});
	}

	private void cacheSync(Cacheable cacheable, InputStream inputStream)
			throws IOException {
		File file = getFile(cacheable);
		File tempFile = new File(file.getParentFile(), "_" + file.getName());

		try {
			// save the stream into a local temp file.
			FileTools.delete(tempFile);
			FileTools.createNewFile(tempFile);
			StreamTools.copy(inputStream, tempFile);

			// Rename the file to the final expected name.
			FileTools.delete(file);
			FileTools.renameFile(tempFile, file);
		} catch (IOException e) {
			logError("Error caching stream... ", e);

			try {
				FileTools.delete(tempFile);
				FileTools.delete(file);
			} catch (IOException e1) {
				logError("Error deleting cache file!", e1);
			}
			throw e;
		}
	}

	private void load(final Cacheable cacheable, final GenericListener<InputStream> listener) {
		cacheQueue.addItem(new Runnable() {

			@Override
			public void run() {
				File file = getFile(cacheable);
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					listener.onSuccess(fis);
				} catch (FileNotFoundException e) {
					listener.onError(e);
				} finally {
					try {
						if (fis != null)
							fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
