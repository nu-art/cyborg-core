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

package com.nu.art.cyborg.modules.downloader;

import android.net.Uri;
import android.os.Handler;

import com.nu.art.core.GenericListener;
import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.cyborg.modules.CacheModule;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;
import com.nu.art.cyborg.modules.CacheModule.UnableToCacheException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by tacb0ss on 14/06/2017.
 */

public class GenericDownloaderModule
	extends CyborgModule {

	private static final Function<InputStream, InputStream> DefaultConverter = new Function<InputStream, InputStream>() {
		@Override
		public InputStream map(InputStream inputStream) {
			return inputStream;
		}
	};

	private Handler resourceLoader;

	@Override
	protected void init() {
		resourceLoader = getModule(ThreadsModule.class).getDefaultHandler("Resource-Loader");
	}

	public final DownloaderBuilder createDownloader() {
		return new DownloaderBuilderImpl();
	}

	public interface Downloader {

		void download(DownloaderBuilder builder, GenericListener<InputStream> listener);

		void cancel();
	}

	public interface DownloaderBuilder {

		DownloaderBuilder setUrl(String url);

		String getUrl();

		boolean isSameUrl(String url);

		<Type> DownloaderBuilder onSuccess(Function<InputStream, Type> converter, Processor<Type> processor);

		DownloaderBuilder onSuccess(Runnable onSuccess);

		DownloaderBuilder setDownloader(Downloader downloader);

		DownloaderBuilder onError(Processor<Throwable> processor);

		DownloaderBuilder cancel();

		DownloaderBuilder setCacheable(Cacheable cacheable);

		DownloaderBuilder setCacheable(String cacheToFolder, String suffix, boolean isMust);

		DownloaderBuilder onBefore(Runnable runnable);

		DownloaderBuilder onAfter(Runnable runnable);

		void download();
	}

	private class DownloaderBuilderImpl
		implements DownloaderBuilder {

		private final GenericListener<InputStream> downloadListener = new GenericListener<InputStream>() {

			@Override
			public void onSuccess(InputStream inputStream) {
				if (cacheable == null) {
					handleResponse(inputStream);
					return;
				}

				try {
					cacheable.cacheSync(inputStream);
					loadFromCache();
				} catch (UnableToCacheException e) {
					logWarning("COULD NOT CACHE... ", e);
					handleResponse(inputStream);
				} catch (IOException e) {
					onError(e);
				}
			}

			@Override
			public void onError(Throwable e) {
				if (onError != null)
					onError.process(e);
				else
					logError("Error downloading image", e);
			}
		};

		private String url;

		private Cacheable cacheable;

		private Function<InputStream, ?> converter = DefaultConverter;

		private Processor<?> onSuccess;

		private Processor<Throwable> onError;

		private Downloader downloader;

		private Runnable onBefore;

		private Runnable onAfter;

		@Override
		public boolean isSameUrl(String url) {
			return this.url.equals(url);
		}

		public final DownloaderBuilder setUrl(String url) {
			this.url = url;
			return this;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public DownloaderBuilder onSuccess(final Runnable onSuccess) {
			return onSuccess(DefaultConverter, new Processor<InputStream>() {
				@Override
				public void process(InputStream inputStream) {
					logInfo("Download completed: " + url);
					if (onSuccess != null)
						onSuccess.run();
				}
			});
		}

		public final <Type> DownloaderBuilder onSuccess(Function<InputStream, Type> converter, Processor<Type> onSuccess) {
			this.converter = converter;
			this.onSuccess = onSuccess;
			return this;
		}

		@Override
		public DownloaderBuilder setDownloader(Downloader downloader) {
			this.downloader = downloader;
			return this;
		}

		public DownloaderBuilder setCacheable(String cacheToFolder, String suffix, boolean isMust) {
			return setCacheable(getModule(CacheModule.class).new Cacheable().setKey(url).setSuffix(suffix).setMustCache(isMust).setPathToDir(cacheToFolder));
		}

		@Override
		public DownloaderBuilder setCacheable(Cacheable cacheable) {
			this.cacheable = cacheable;
			return this;
		}

		@Override
		public DownloaderBuilder onError(Processor<Throwable> onError) {
			this.onError = onError;
			return this;
		}

		@Override
		public synchronized DownloaderBuilder cancel() {
			if (downloader != null)
				downloader.cancel();

			onBefore = null;
			onAfter = null;

			return this;
		}

		@Override
		public DownloaderBuilder onBefore(Runnable onBefore) {
			this.onBefore = onBefore;
			return this;
		}

		@Override
		public DownloaderBuilder onAfter(Runnable onAfter) {
			this.onAfter = onAfter;
			return this;
		}

		public final void download() {
			if (url == null || url.trim().length() == 0) {
				IOException error = new IOException("url is null or empty");
				onError(error);

				return;
			}

			if (cacheable != null && cacheable.isCached()) {
				loadFromCache();
				return;
			}

			if (onBefore != null)
				onBefore.run();

			if (url.startsWith("android.resource://")) {
				loadFromResources(downloadListener);
				return;
			}

			if (url.startsWith("file://")) {
				loadFromFile(downloadListener);
				return;
			}

			downloader.download(this, downloadListener);
		}

		private void onError(Throwable error) {
			if (onError != null) {
				onError.process(error);
				return;
			}

			logError(error);
		}

		private void loadFromFile(final GenericListener<InputStream> listener) {
			resourceLoader.post(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onSuccess(new FileInputStream(new File(URI.create(url))));
					} catch (FileNotFoundException e) {
						logWarning("Failed getting file from path: '" + url + "'", e);
						onError(e);
					}
				}
			});
		}

		private void loadFromResources(final GenericListener<InputStream> listener) {
			resourceLoader.post(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onSuccess(getContentResolver().openInputStream(Uri.parse(url)));
					} catch (FileNotFoundException e) {
						logWarning("Failed getting file from path: '" + url + "'", e);
						onError(e);
					}
				}
			});
		}

		private void loadFromCache() {
			cacheable.load(new GenericListener<InputStream>() {

				@Override
				public void onSuccess(InputStream inputStream) {
					handleResponse(inputStream);
				}

				@Override
				public void onError(Throwable e) {
					DownloaderBuilderImpl.this.onError(e);
				}
			});
		}

		@SuppressWarnings("unchecked")
		private <Type> void handleResponse(InputStream inputStream) {
			Type value = ((Function<InputStream, Type>) converter).map(inputStream);
			((Processor<Type>) onSuccess).process(value);

			if (onAfter != null)
				onAfter.run();
		}
	}
}
