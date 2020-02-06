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

package com.nu.art.cyborg.modules;

import com.nu.art.core.GenericListener;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.core.tools.StringTools;
import com.nu.art.core.utils.RunnableQueue;
import com.nu.art.cyborg.tools.CryptoTools;
import com.nu.art.modular.core.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by TacB0sS on 14/06/2017.
 */
@SuppressWarnings("unused")
public class CacheModule
	extends Module {

	public class Cacheable {

		private String key;

		private String suffix;

		private String pathToDir;

		private long interval;

		private boolean must = true;

		@Override
		public String toString() {
			return key + "<>" + getLocalCacheFile().getAbsolutePath();
		}

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
		 * Whether this item MUST be cached or can we live on with it not being cached.
		 *
		 * @param must Must this item be cached
		 *
		 * @return The instance {@link Cacheable} you currently edit
		 */
		public Cacheable setMustCache(boolean must) {
			this.must = must;
			return this;
		}

		/**
		 * @return Whether or not this item is cached.
		 */
		public boolean isCached() {
			if (StringTools.isEmpty(key))
				throw new BadImplementationException("Cacheable's KEY is NULL or EMPTY!");

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
		 * @param listener    callback to notify caching completion
		 */
		public void cacheSync(InputStream inputStream, CacheListener listener) {
			CacheModule.this.cacheSync(this, inputStream, listener);
		}

		public void load(GenericListener<InputStream> listener) {
			CacheModule.this.load(this, listener);
		}

		/**
		 * To call this method you might be using a bad utility OR you architecture is flawed OR you don't know what you are doing OR you don't have a choice OR you
		 * are smarter then I have anticipated...
		 *
		 * Regardless I think this is a bad way to use a rest api client!
		 *
		 * @return The response input stream, <b>be sure to close it when you are done</b>!
		 */
		public InputStream loadSync()
			throws FileNotFoundException {
			return CacheModule.this.loadSync(this);
		}
	}

	public interface CacheListener {

		void onItemCacheError(Cacheable cacheable, Throwable e);

		void onItemCacheCompleted(Cacheable cacheable);
	}

	private RunnableQueue cacheQueue = new RunnableQueue();

	private File persistentDir;

	private File cacheDir;

	private int threadCount = 5;

	private final HashMap<String, ArrayList<CacheListener>> currentlyCaching = new HashMap<>();

	@Override
	protected void init() {
		cacheQueue.createThreads("Caching Thread", threadCount);
	}

	public void setPersistentDir(File persistentDir) {
		this.persistentDir = persistentDir;
	}

	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	private boolean isCached(Cacheable cacheable) {
		File file = getFile(cacheable);
		if (!file.exists()) {
			if (DebugFlag.isEnabled())
				logVerbose("File does not exist: " + file.getAbsolutePath());
			return false;
		}

		if (!file.isFile()) {
			if (DebugFlag.isEnabled())
				logVerbose("Not a file: " + file.getAbsolutePath());
			return false;
		}

		if (file.length() == 0) {
			if (DebugFlag.isEnabled())
				logVerbose("File empty: " + file.getAbsolutePath());
			return false;
		}

		if (cacheable.interval == 0)
			return true;

		boolean isValidCacheInterval = System.currentTimeMillis() - file.lastModified() < cacheable.interval;
		if (!isValidCacheInterval)
			if (DebugFlag.isEnabled())
				logVerbose("cacheTimeout: " + file.getAbsolutePath());

		return isValidCacheInterval;
	}

	private File getFile(Cacheable cacheable) {
		File dir;
		if (cacheable.pathToDir != null)
			dir = new File(cacheable.pathToDir);
		else
			dir = cacheable.interval > 0 ? persistentDir : cacheDir;

		String fileName;
		try {
			fileName = CryptoTools.doFingerprint(cacheable.key.getBytes(), "MD5").replaceAll(":", "");
		} catch (NoSuchAlgorithmException e) {
			fileName = cacheable.key.hashCode() + "";
		}
		return new File(dir, fileName + "." + cacheable.suffix);
	}

	private void cacheAsync(final Cacheable cacheable, final InputStream inputStream, final CacheListener listener) {
		if (DebugFlag.isEnabled())
			logDebug("Added Cacheable to queue: " + cacheable.getLocalCacheFile().getAbsolutePath());
		cacheQueue.addItem(new Runnable() {

			@Override
			public void run() {
				cacheSync(cacheable, inputStream, listener);
			}
		});
	}

	private void cacheSync(Cacheable cacheable, InputStream inputStream, CacheListener listener) {
		String absolutePath = cacheable.getLocalCacheFile().getAbsolutePath();
		try {
			synchronized (currentlyCaching) {
				if (DebugFlag.isEnabled())
					logDebug("Register Cacheable Listener: " + cacheable.getLocalCacheFile().getAbsolutePath());

				ArrayList<CacheListener> cacheListeners = currentlyCaching.get(absolutePath);
				if (cacheListeners != null) {
					cacheListeners.add(listener);
					return;
				}

				currentlyCaching.put(absolutePath, cacheListeners = new ArrayList<>());
				cacheListeners.add(listener);
			}

			cacheSyncImpl(cacheable, inputStream);

			synchronized (currentlyCaching) {
				if (DebugFlag.isEnabled())
					logDebug("Firing Cacheable Cached: " + cacheable.getLocalCacheFile().getAbsolutePath());

				ArrayList<CacheListener> listeners = currentlyCaching.get(absolutePath);
				for (CacheListener cacheListener : listeners) {
					cacheListener.onItemCacheCompleted(cacheable);
				}
			}
		} catch (final Throwable e) {
			synchronized (currentlyCaching) {
				if (DebugFlag.isEnabled())
					logDebug("Firing Cacheable Error: " + cacheable.getLocalCacheFile().getAbsolutePath());
				ArrayList<CacheListener> listeners = currentlyCaching.get(absolutePath);
				for (CacheListener cacheListener : listeners) {
					cacheListener.onItemCacheError(cacheable, e);
				}
			}
		}

		synchronized (currentlyCaching) {
			if (DebugFlag.isEnabled())
				logDebug("Remove Cacheable Listeners: " + cacheable.getLocalCacheFile().getAbsolutePath());
			currentlyCaching.remove(absolutePath);
		}
	}

	private void cacheSyncImpl(Cacheable cacheable, InputStream inputStream)
		throws IOException {
		File file = getFile(cacheable);
		File tempFile = new File(file.getParentFile(), "_" + file.getName());
		if (DebugFlag.isEnabled())
			logDebug("Started caching to: " + tempFile.getAbsolutePath());

		try {
			// save the stream into a local temp file.
			if (DebugFlag.isEnabled())
				logDebug("Deleting temp cache file: " + tempFile.getAbsolutePath());
			FileTools.delete(tempFile);
			FileTools.createNewFile(tempFile);
		} catch (IOException e) {
			if (cacheable.must)
				throw e;

			throw new UnableToCacheException("Error Caching cacheable: " + cacheable.key + " => " + tempFile.getAbsolutePath());
		}

		try {
			StreamTools.copy(inputStream, tempFile);
			// Rename the file to the final expected name.
			FileTools.delete(file);
			FileTools.renameFile(tempFile, file);
			if (DebugFlag.isEnabled())
				logDebug("Caching completed to: " + file.getAbsolutePath());
		} catch (IOException e) {
			logError("Error caching stream... ", e);

			try {
				FileTools.delete(tempFile);
				FileTools.delete(file);
			} catch (IOException e1) {
				logError("Error deleting cache file!", e1);
			}

			throw new UnableToCacheException("Error Caching cacheable: " + cacheable.key + " => " + tempFile.getAbsolutePath(), e);
		}
	}

	private InputStream loadSync(final Cacheable cacheable)
		throws FileNotFoundException {
		File file = getFile(cacheable);
		return new FileInputStream(file);
	}

	private void load(final Cacheable cacheable, final GenericListener<InputStream> listener) {
		cacheQueue.addItem(new Runnable() {

			@Override
			public void run() {
				InputStream fis = null;
				try {
					fis = loadSync(cacheable);
					listener.onSuccess(fis);
				} catch (FileNotFoundException e) {
					listener.onError(e);
				} catch (Throwable e) {
					listener.onError(new RuntimeException("Error loading image from cache: " + cacheable.getLocalCacheFile().getAbsolutePath(), e));
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

	public static class UnableToCacheException
		extends IOException {

		UnableToCacheException(String message) {
			super(message);
		}

		UnableToCacheException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
