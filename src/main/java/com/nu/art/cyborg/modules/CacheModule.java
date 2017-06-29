package com.nu.art.cyborg.modules;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.core.utils.RunnableQueue;
import com.nu.art.cyborg.core.CyborgModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tacb0ss on 14/06/2017.
 */
public class CacheModule
		extends CyborgModule {

	public interface CacheListener {

		void onItemCacheError(String key, Exception e);

		void onItemCacheCompleted(String key);
	}

	private RunnableQueue cacheQueue = new RunnableQueue();

	private File cacheDir;

	@Override
	protected void init() {
		cacheDir = getApplicationContext().getCacheDir();
		cacheQueue.createThreads("Caching Thread", 5);
	}

	private String convertKeyToFile(String key) {
		return "" + key.hashCode();
	}

	public void cacheFile(final String key, final InputStream inputStream) {
		cacheQueue.addItem(new Runnable() {

			@Override
			public void run() {
				File file = new File(cacheDir, convertKeyToFile(key));
				try {
					StreamTools.copy(inputStream, file);
					dispatchCachingCompleted(key);
				} catch (final IOException e) {
					dispatchCachingFailed(e, key);
				}
			}
		});
	}

	private void dispatchCachingFailed(final IOException e, final String key) {
		dispatchModuleEvent("Error caching item: " + key, CacheListener.class, new Processor<CacheListener>() {

			@Override
			public void process(CacheListener cacheListener) {
				cacheListener.onItemCacheError(key, e);
			}
		});
	}

	private void dispatchCachingCompleted(final String key) {
		dispatchModuleEvent("Caching done: " + key, CacheListener.class, new Processor<CacheListener>() {

			@Override
			public void process(CacheListener cacheListener) {
				cacheListener.onItemCacheCompleted(key);
			}
		});
	}
}