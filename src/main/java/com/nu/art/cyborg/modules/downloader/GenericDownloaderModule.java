package com.nu.art.cyborg.modules.downloader;

import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.modular.core.Module;

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

	public interface DownloaderListener {

		void onSuccess(InputStream inputStream);

		void onError(Throwable e);
	}

	public interface Downloader {

		void download(DownloaderListener listener);

		void cancel();
	}

	public interface DownloaderBuilder {

		DownloaderBuilder setUrl(String url);

		boolean isSameUrl(String url);

		<Type> DownloaderBuilder onSuccess(Function<InputStream, Type> converter, Processor<Type> processor);

		DownloaderBuilder setDownloader(Downloader downloader);

		DownloaderBuilder onError(Processor<Throwable> processor);

		DownloaderBuilder cancel();

		DownloaderBuilder onBefore(Runnable runnable);

		DownloaderBuilder onAfter(Runnable runnable);

		void download();
	}

	private class DownloaderBuilderImpl
			implements DownloaderBuilder {

		private String url;

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
			if (onBefore != null)
				onBefore.run();

			downloader.download(new DownloaderListener() {
				@Override
				public void onSuccess(InputStream inputStream) {
					handleResponse(inputStream);
				}

				@SuppressWarnings("unchecked")
				private <Type> void handleResponse(InputStream inputStream) {
					Type value = ((Function<InputStream, Type>) converter).map(inputStream);
					((Processor<Type>) onSuccess).process(value);

					if (onAfter != null)
						onAfter.run();
				}

				@Override
				public void onError(Throwable e) {
					onError.process(e);
				}
			});
		}
	}
}
