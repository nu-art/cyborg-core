/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

package com.nu.art.cyborg.core.modules;

import android.content.res.Resources;

import com.nu.art.cyborg.core.CyborgModule;

import java.util.Locale;

public class LocaleModule
		extends CyborgModule {

	public interface SupportedLocale {

		Locale getLocale();
	}

	private static final SupportedLocale DefaultLanguage = LanguageLocale.English;

	public enum LanguageLocale
			implements SupportedLocale {
		English("English", "en"),
		Arabic("Arabic", "ar"),
		German("German", "de"),
		Spanish("Spanish", "es"),
		French("French", "fr"),
		Italian("Italian", "it"),
		Japanese("Japanese", "ja"),
		Portuguese("Portuguese", "pt_BR"),
		Korean("Korean", "ko"),
		Russian("Russian", "ru"),
		Chinese_PRC("Chinese_PRC", "zh_CN"),;

		public final String name;

		public final String localeString;

		LanguageLocale(String name, String localeString) {
			this.name = name;
			this.localeString = localeString;
		}

		public Locale getLocale() {
			String[] localeParams = localeString.split("_");
			if (localeParams.length == 1)
				return new Locale(localeParams[0]);

			return new Locale(localeParams[0], localeParams[1]);
		}
	}

	@Override
	protected void init() {
		if (isDebug())
			changeApplicationLocale(DefaultLanguage);
	}

	public final void changeApplicationLocale(SupportedLocale newLocale) {
		Resources res = getResources();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.setLocale(newLocale.getLocale());
		getApplicationContext().createConfigurationContext(conf);
	}
}
