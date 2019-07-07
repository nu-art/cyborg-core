package com.nu.art.modules.cache;

import org.junit.Test;

import java.io.IOException;

public class Test_CacheModule
	extends Test_BaseCacheTest {

	@Test
	public void test_CacheOne()
		throws IOException {
		printTestName();
		deleteOutput();

		createAsyncScenario()
			.addTest(cacheUrlSuccess("JazzIcon", "png", URL_musicIconCategoryClassical))
			.execute();
	}

	@Test
	public void test_CacheTwoTheDifferent()
		throws IOException {
		printTestName();
		deleteOutput();

		createAsyncScenario()
			.addTest(cacheUrlSuccess("JazzIcon", "png", URL_musicIconCategoryJazz))
			.addTest(cacheUrlSuccess("ClassicalIcon", "png", URL_musicIconCategoryClassical))
			.execute();
	}

	@Test
	public void test_CacheTwoTheSame()
		throws IOException {
		printTestName();
		deleteOutput();

		createAsyncScenario()
			.addTest(cacheUrlSuccess("JazzIcon-1", "png", URL_musicIconCategoryJazz))
			.addTest(cacheUrlSuccess("JazzIcon-2", "png", URL_musicIconCategoryJazz))
			.execute();
	}
}

b31e74b3710f31f9d2e9311639b544b6469c75d6b97e612be9a948c20c27d0ea