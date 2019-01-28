package com.nu.art.modules.cache;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.DefaultLogClient;
import com.nu.art.core.GenericListener;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.modules.CacheModule;
import com.nu.art.cyborg.modules.CacheModule.CacheListener;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;
import com.nu.art.http.HttpModule;
import com.nu.art.http.HttpModule.BaseTransaction;
import com.nu.art.http.HttpResponse;
import com.nu.art.http.HttpResponseListener;
import com.nu.art.modular.core.ModuleManagerBuilder;
import com.nu.art.modular.core.ModulesPack;
import com.nu.art.modular.tests.BaseTest;
import com.nu.art.modular.tests.ModuleManager_TestClass;

import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.InputStream;

import static com.nu.art.http.consts.HttpMethod.Get;

public class Test_BaseCacheTest
	extends ModuleManager_TestClass {

	public static final String URL_musicIconCategoryJazz = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_jazz.png";
	public static final String URL_musicIconCategoryClassical = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_classical.png";
	public static final String URL_musicIconCategoryCountry = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_country.png";
	public static final String URL_musicIconCategoryModernHits = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_modern_hits.png";
	public static final String URL_musicIconCategoryRock = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_rock.png";
	public static final String URL_musicIconCategoryEnergetic = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_energetic.png";
	public static final String URL_musicIconCategoryOpera = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_opera.png";
	public static final String URL_musicIconCategoryBroadway = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_broadway.png";
	public static final String URL_musicIconCategoryInternational = "https://storage.googleapis.com/elliq-env-dev.appspot.com/resources/1-18/music_plan/music_category_icons/music_icon_category_ces_international.png";

	public static class DownloadTransaction
		extends BaseTransaction {

		public void download(final String url, final String suffix, final GenericListener<InputStream> listener) {
			download(url, suffix, 0, listener);
		}

		public void download(final String url, final String suffix, final int interval, final GenericListener<InputStream> listener) {
			HttpResponseListener<InputStream, String> httpListener = new HttpResponseListener<InputStream, String>(InputStream.class, String.class) {
				@Override
				public void onSuccess(HttpResponse httpResponse, InputStream responseBody) {
					getModule(CacheModule.class).new Cacheable().setKey(url).setSuffix(suffix).setInterval(interval).cacheSync(responseBody, new CacheListener() {

						@Override
						public void onItemCacheError(Cacheable key, Throwable e) {
							listener.onError(e);
						}

						@Override
						public void onItemCacheCompleted(Cacheable key) {
							logInfo("Caching: " + key);
							listener.onSuccess(null);
						}
					});
				}

				@Override
				public void onError(HttpResponse httpResponse, String errorBody) {
					logError("Error downloading: " + url);

					listener.onError(createException(httpResponse, errorBody));
				}
			};

			createRequest().setUrl(url).setMethod(Get).execute(httpListener);
		}
	}

	private static DownloadTransaction dt;

	static class _ModulePack
		extends ModulesPack {

		_ModulePack() {
			super(CacheModule.class, HttpModule.class);
		}

		@Override
		protected void init() {
			super.init();
			getModule(CacheModule.class).DebugFlag.enable();
			getModule(CacheModule.class).setCacheDir(new File("build/test/cache"));
			getModule(CacheModule.class).setPersistentDir(new File("build/test/persist"));
		}
	}

	@BeforeClass
	@SuppressWarnings("unchecked")
	public static void setUp() {
		initWithPacks(_ModulePack.class);
		dt = new DownloadTransaction();
	}

	private AsyncTest<Boolean> cacheUrlWithError(String name, final String suffix, final String url) {
		return cacheUrl(name, suffix, url, false);
	}

	protected AsyncTest<Boolean> cacheUrlSuccess(String name, final String suffix, final String url) {
		return cacheUrl(name, suffix, url, true);
	}

	private AsyncTest<Boolean> cacheUrl(final String name, final String suffix, final String url, boolean expectedValue) {
		return new AsyncTest<Boolean>()
			.setName(name)
			.expectedValue(expectedValue)
			.setValidator(expectedValue)
			.setProcessor(new Processor<AsyncTest<Boolean>>() {
				@Override
				public void process(final AsyncTest<Boolean> test) {
					dt.download(url, suffix, new GenericListener<InputStream>() {
						@Override
						public void onSuccess(InputStream inputStream) {
							test._set(true);
						}

						@Override
						public void onError(Throwable e) {
							test._set(e);
						}
					});
				}
			});
	}
}
