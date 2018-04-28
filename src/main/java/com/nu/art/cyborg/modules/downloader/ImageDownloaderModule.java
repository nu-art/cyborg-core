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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;
import com.nu.art.cyborg.modules.downloader.GenericDownloaderModule.Downloader;
import com.nu.art.cyborg.modules.downloader.GenericDownloaderModule.DownloaderBuilder;
import com.nu.art.cyborg.modules.downloader.converters.Converter_Bitmap;

/**
 * Created by tacb0ss on 14/06/2017.
 */

public class ImageDownloaderModule
	extends CyborgModule {

	@Override
	protected void init() {

	}

	public final ImageDownloaderBuilder createDownloader() {
		return new ImageDownloaderBuilderImpl();
	}

	public interface ImageDownloaderBuilder {

		ImageDownloaderBuilder setUrl(String url);

		boolean isSameUrl(String url);

		ImageDownloaderBuilder setDownloader(Downloader downloader);

		ImageDownloaderBuilder setPostDownloading(Function<Bitmap, Bitmap> postDownloading);

		ImageDownloaderBuilder setTarget(ImageView target);

		ImageDownloaderBuilder setCacheable(Cacheable cacheable);

		ImageDownloaderBuilder onSuccess(Processor<Bitmap> onSuccess);

		ImageDownloaderBuilder onError(@DrawableRes int drawableId);

		ImageDownloaderBuilder onError(Drawable errorDrawable);

		ImageDownloaderBuilder onError(Bitmap errorBitmap);

		ImageDownloaderBuilder cancel();

		ImageDownloaderBuilder onBefore(Runnable runnable);

		ImageDownloaderBuilder onAfter(Runnable runnable);

		ImageDownloaderBuilder onError(Runnable runnable);

		void download();
	}

	private class ImageDownloaderBuilderImpl
		implements ImageDownloaderBuilder {

		private boolean cancelled;

		private String url;

		private int errorDrawableId;

		private Drawable errorDrawable;

		private Bitmap errorBitmap;

		private Downloader downloader;

		private DownloaderBuilder downloaderBuilder;

		private ImageView target;

		private Function<Bitmap, Bitmap> postDownloading;

		private Processor<Bitmap> onSuccess;

		private Runnable onBefore;

		private Runnable onAfter;

		private Runnable onError;

		private Cacheable cacheable;

		public final ImageDownloaderBuilder setUrl(String url) {
			this.url = url;
			return this;
		}

		public ImageDownloaderBuilder setDownloader(Downloader downloader) {
			this.downloader = downloader;
			return this;
		}

		public ImageDownloaderBuilder setCacheable(Cacheable cacheable) {
			this.cacheable = cacheable;
			return this;
		}

		@Override
		public boolean isSameUrl(String url) {
			return this.url.equals(url);
		}

		@Override
		public ImageDownloaderBuilder setPostDownloading(Function<Bitmap, Bitmap> postDownloading) {
			this.postDownloading = postDownloading;
			return this;
		}

		@Override
		public ImageDownloaderBuilder setTarget(ImageView target) {
			this.target = target;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onSuccess(Processor<Bitmap> onSuccess) {
			this.onSuccess = onSuccess;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(@DrawableRes int errorDrawableId) {
			this.errorDrawable = null;
			this.errorBitmap = null;
			this.errorDrawableId = errorDrawableId;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(Drawable errorDrawable) {
			this.errorDrawable = errorDrawable;
			this.errorBitmap = null;
			this.errorDrawableId = -1;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(Bitmap errorBitmap) {
			this.errorBitmap = errorBitmap;
			this.errorDrawable = null;
			this.errorDrawableId = -1;
			return this;
		}

		@Override
		public ImageDownloaderBuilder cancel() {
			cancelled = true;
			if (downloaderBuilder != null)
				downloaderBuilder.cancel();

			return this;
		}

		public final void download() {
			downloaderBuilder = getModule(GenericDownloaderModule.class).createDownloader();
			downloaderBuilder.setUrl(url);
			downloaderBuilder.setCacheable(cacheable);
			downloaderBuilder.onBefore(onBefore);
			downloaderBuilder.onAfter(onAfter);
			downloaderBuilder.setDownloader(downloader);
			downloaderBuilder.onSuccess(Converter_Bitmap.converter, new Processor<Bitmap>() {

				@Override
				public void process(Bitmap bitmap) {
					if (cancelled)
						return;

					if (postDownloading != null)
						bitmap = postDownloading.map(bitmap);

					final Bitmap finalBitmap = bitmap;
					postOnUI(new Runnable() {
						@Override
						public void run() {
							if (cancelled)
								return;

							if (onSuccess != null)
								onSuccess.process(finalBitmap);
							else
								target.setImageBitmap(finalBitmap);
						}
					});
				}
			});
			downloaderBuilder.onError(new Processor<Throwable>() {
				@Override
				public void process(final Throwable e) {
					if (cancelled)
						return;

					postOnUI(new Runnable() {
						@Override
						public void run() {
							processError();

							if (onError != null)
								onError.run();
							else
								logError("Error downloading image", e);
						}

						private void processError() {
							if (errorDrawableId != -1) {
								target.setImageResource(errorDrawableId);
								return;
							}

							if (errorDrawable != null) {
								target.setImageDrawable(errorDrawable);
								return;
							}

							if (errorBitmap != null) {
								target.setImageBitmap(errorBitmap);
								return;
							}
						}
					});
				}
			});

			downloaderBuilder.download();
		}

		@Override
		public ImageDownloaderBuilder onBefore(Runnable onBefore) {
			this.onBefore = onBefore;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(Runnable onError) {
			this.onError = onError;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onAfter(Runnable onAfter) {
			this.onAfter = onAfter;
			return this;
		}
	}
}
