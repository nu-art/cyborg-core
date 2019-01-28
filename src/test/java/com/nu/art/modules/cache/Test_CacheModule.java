package com.nu.art.modules.cache;

import org.junit.Test;

public class Test_CacheModule
	extends Test_BaseCacheTest {

	@Test
	public void test_CacheOne() {
		AsyncScenario testGroup = createAsyncScenario();
		testGroup.addTest(cacheUrlSuccess("JazzIcon", "png", URL_musicIconCategoryJazz));
		testGroup.execute();
	}

	@Test
	public void test_CacheTwoTheDifferent() {
		AsyncScenario testGroup = createAsyncScenario();
		testGroup.addTest(cacheUrlSuccess("JazzIcon", "png", URL_musicIconCategoryJazz));
		testGroup.addTest(cacheUrlSuccess("JazzIcon", "png", URL_musicIconCategoryClassical));
		testGroup.execute();
	}

	@Test
	public void test_CacheTwoTheSame() {
		AsyncScenario testGroup = createAsyncScenario();
		testGroup.addTest(cacheUrlSuccess("JazzIcon-1", "png", URL_musicIconCategoryJazz));
		testGroup.addTest(cacheUrlSuccess("JazzIcon-2", "png", URL_musicIconCategoryJazz));
		testGroup.execute();
	}
}