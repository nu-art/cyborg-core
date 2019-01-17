package com.nu.art.modules.cache;

import org.junit.Test;

public class Test_CacheModule
	extends Test_BaseCacheTest {

	@Test
	public void test_CacheOne() {
		AsyncTestim<Boolean> testGroup = createTestGroup();
		testGroup.addTest(cacheUrlSuccess("Jazz Icon", "png", URL_musicIconCategoryJazz));
		testGroup.execute();
	}

	@Test
	public void test_CacheTwoTheDifferent() {
		AsyncTestim<Boolean> testGroup = createTestGroup();
		testGroup.addTest(cacheUrlSuccess("Jazz Icon", "png", URL_musicIconCategoryJazz));
		testGroup.addTest(cacheUrlSuccess("Jazz Icon", "png", URL_musicIconCategoryClassical));
		testGroup.execute();
	}

	@Test
	public void test_CacheTwoTheSame() {
		AsyncTestim<Boolean> testGroup = createTestGroup();
		testGroup.addTest(cacheUrlSuccess("Jazz Icon 1", "png", URL_musicIconCategoryJazz).setTimeout(10000000));
		testGroup.addTest(cacheUrlSuccess("Jazz Icon 2", "png", URL_musicIconCategoryJazz));
		testGroup.execute();
	}
}