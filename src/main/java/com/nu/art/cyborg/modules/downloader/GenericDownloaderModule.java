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

import com.nu.art.core.GenericListener;
import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;
import com.nu.art.cyborg.modules.CacheModule.UnableToCacheException;
import com.nu.art.modular.core.Module;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tacb0ss on 14/06/2017.
 */

public class GenericDownloaderModule
	extends Module {

	@Override
	protected void init() {}

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

		DownloaderBuilder setDownloader(Downloader downloader);

		DownloaderBuilder onError(Processor<Throwable> processor);

		DownloaderBuilder cancel();

		DownloaderBuilder setCacheable(Cacheable cacheable);

		DownloaderBuilder onBefore(Runnable runnable);

		DownloaderBuilder onAfter(Runnable runnable);

		void download();
	}

	private class DownloaderBuilderImpl
		implements DownloaderBuilder {

		private String url;

		private Cacheable cacheable;

		private Function<InputStream, ?> converter;

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
			if (cacheable != null && cacheable.isCached()) {
				loadFromCache();
				return;
			}

			if (onBefore != null)
				onBefore.run();

			downloadFromUrl();
		}

		private void downloadFromUrl() {
			downloader.download(this, new GenericListener<InputStream>() {

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
						logWarning("COULD NOT CACHE... " + e.getMessage());
						handleResponse(inputStream);
					} catch (IOException e) {
						logError("Error caching stream... ", e);
						onError(e);
					}
				}

				@Override
				public void onError(Throwable e) {
					onError.process(e);
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
					onError.process(e);
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
