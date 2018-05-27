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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.CacheModule;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;
import com.nu.art.cyborg.modules.downloader.GenericDownloaderModule.Downloader;
import com.nu.art.cyborg.modules.downloader.GenericDownloaderModule.DownloaderBuilder;
import com.nu.art.cyborg.modules.downloader.converters.Converter_Bitmap;

import java.lang.ref.WeakReference;

/**
 * Created by tacb0ss on 14/06/2017.
 */

public class ImageDownloaderModule
	extends CyborgModule {

	@Override
	protected void init() {

	}

	public ImageDownloaderBuilder createDownloader(ImageView target, String url) {
		ImageDownloaderBuilderImpl downloader = null;
		if (target != null)
			downloader = (ImageDownloaderBuilderImpl) target.getTag();

		if (downloader == null) {
			downloader = new ImageDownloaderBuilderImpl();
		}

		downloader.setUrl(url);
		downloader.setTarget(target);
		return downloader;
	}

	public interface ImageDownloaderBuilder {

		ImageDownloaderBuilder setDownloader(Downloader downloader);

		ImageDownloaderBuilder setPostDownloading(Function<Bitmap, Bitmap> postDownloading);

		ImageDownloaderBuilder setCacheable(Cacheable cacheable);

		ImageDownloaderBuilder setCacheable(String cacheToFolder, String suffix, boolean isMust);

		ImageDownloaderBuilder onSuccess(Processor<Bitmap> onSuccess);

		ImageDownloaderBuilder onError(@DrawableRes int drawableId);

		ImageDownloaderBuilder onError(Drawable errorDrawable);

		ImageDownloaderBuilder onError(Bitmap errorBitmap);

		ImageDownloaderBuilder cancel();

		ImageDownloaderBuilder onBefore(Runnable runnable);

		ImageDownloaderBuilder onAfter(Runnable runnable);

		ImageDownloaderBuilder onError(Runnable runnable);

		ImageDownloaderBuilder onError(Processor<Throwable> onError);

		void download();
	}

	private class ImageDownloaderBuilderImpl
		implements ImageDownloaderBuilder {

		// Internal
		private boolean sameUrl;
		private boolean cancelled;
		private DownloaderBuilder downloaderBuilder;

		private String url;

		private Drawable errorDrawable;

		private Cacheable cacheable;

		private Downloader downloader;

		// UI
		private WeakReference<ImageView> target;
		private WeakReference<Function<Bitmap, Bitmap>> postDownloading;
		private WeakReference<Runnable> onBefore;
		private WeakReference<Processor<Bitmap>> onSuccess;
		private WeakReference<Runnable> onAfter;
		private WeakReference<Processor<Throwable>> onError;

		private void setTarget(ImageView target) {
			this.target = new WeakReference<>(target);
		}

		private void setUrl(String url) {
			sameUrl = this.url != null && url != null && this.url.equals(url);
			if (!sameUrl)
				cancel();

			this.url = url;
		}

		public ImageDownloaderBuilder setDownloader(Downloader downloader) {
			this.downloader = downloader;
			return this;
		}

		public ImageDownloaderBuilder setCacheable(Cacheable cacheable) {
			this.cacheable = cacheable;
			return this;
		}

		public ImageDownloaderBuilder setCacheable(String cacheToFolder, String suffix, boolean isMust) {
			return setCacheable(getModule(CacheModule.class).new Cacheable().setKey(url).setSuffix(suffix).setMustCache(isMust).setPathToDir(cacheToFolder));
		}

		@Override
		public ImageDownloaderBuilder setPostDownloading(Function<Bitmap, Bitmap> postDownloading) {
			this.postDownloading = new WeakReference<>(postDownloading);
			return this;
		}

		@Override
		public ImageDownloaderBuilder onBefore(Runnable onBefore) {
			this.onBefore = new WeakReference<>(onBefore);
			return this;
		}

		@Override
		public ImageDownloaderBuilder onAfter(Runnable onAfter) {
			this.onAfter = new WeakReference<>(onAfter);
			return this;
		}

		@Override
		public ImageDownloaderBuilder onSuccess(Processor<Bitmap> onSuccess) {
			this.onSuccess = new WeakReference<>(onSuccess);
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(@DrawableRes int errorDrawableId) {
			return onError(getResources().getDrawable(errorDrawableId));
		}

		@Override
		public ImageDownloaderBuilder onError(Drawable errorDrawable) {
			this.errorDrawable = errorDrawable;
			return this;
		}

		@Override
		public ImageDownloaderBuilder onError(Bitmap errorBitmap) {
			return onError(new BitmapDrawable(getResources(), errorBitmap));
		}

		@Override
		public ImageDownloaderBuilder onError(final Runnable onError) {
			return onError(new Processor<Throwable>() {
				@Override
				public void process(Throwable throwable) {
					onError.run();
				}
			});
		}

		@Override
		public ImageDownloaderBuilder onError(Processor<Throwable> onError) {
			this.onError = new WeakReference<>(onError);
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
			if (sameUrl) {
				logWarning("Same url... will not download");
				return;
			}

			cancelled = false;

			downloaderBuilder = getModule(GenericDownloaderModule.class).createDownloader();
			downloaderBuilder.setUrl(url);
			downloaderBuilder.setCacheable(cacheable);
			downloaderBuilder.onBefore(onBefore.get());
			downloaderBuilder.onAfter(onAfter.get());
			downloaderBuilder.setDownloader(downloader);
			downloaderBuilder.onSuccess(Converter_Bitmap.converter, new Processor<Bitmap>() {

				@Override
				public void process(Bitmap bitmap) {
					sameUrl = false;
					if (cancelled)
						return;

					Function<Bitmap, Bitmap> postDownloading = getPostDownloading();
					if (postDownloading != null)
						bitmap = postDownloading.map(bitmap);

					final Bitmap finalBitmap = bitmap;
					postOnUI(new Runnable() {
						@Override
						public void run() {
							if (cancelled)
								return;

							Processor<Bitmap> onSuccess = getOnSuccess();
							if (onSuccess != null) {
								onSuccess.process(finalBitmap);
								return;
							}

							ImageView target = getTarget();
							if (target != null) {
								target.setImageBitmap(finalBitmap);
								return;
							}

							logInfo("Url cached: " + url);
						}
					});
				}
			});

			downloaderBuilder.onError(new Processor<Throwable>() {
				@Override
				public void process(final Throwable e) {
					sameUrl = false;
					if (cancelled)
						return;

					postOnUI(new Runnable() {
						@Override
						public void run() {

							Processor<Throwable> onError = getOnError();
							if (onError != null) {
								onError.process(e);
								return;
							}

							if (errorDrawable != null) {
								ImageView target = getTarget();
								if (target != null) {
									target.setImageDrawable(errorDrawable);
									return;
								}
							}

							logError("Error downloading image", e);
						}
					});
				}
			});

			downloaderBuilder.download();
		}

		private Processor<Bitmap> getOnSuccess() {
			if (ImageDownloaderBuilderImpl.this.onSuccess != null)
				return ImageDownloaderBuilderImpl.this.onSuccess.get();

			return null;
		}

		private ImageView getTarget() {
			if (ImageDownloaderBuilderImpl.this.target != null)
				return ImageDownloaderBuilderImpl.this.target.get();

			return null;
		}

		private Processor<Throwable> getOnError() {
			if (ImageDownloaderBuilderImpl.this.onError != null)
				return ImageDownloaderBuilderImpl.this.onError.get();

			return null;
		}

		private Function<Bitmap, Bitmap> getPostDownloading() {
			if (ImageDownloaderBuilderImpl.this.postDownloading != null)
				return ImageDownloaderBuilderImpl.this.postDownloading.get();

			return null;
		}
	}
}
