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

package com.nu.art.cyborg.modules;

import android.Manifest.permission;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

@ModuleDescriptor(usesPermissions = permission.READ_CONTACTS)
public class ContactsModule
	extends CyborgModule {

	private static final String phoneNumberSpecialChars = "- \\*N\\(\\)/,;\\+\\.";
	private static final String regex = "[" + phoneNumberSpecialChars + "]?";

	private static final String[] NumberToLetterConverter = {
		"[abcABC]",
		"[defDEF]",
		"[ghiGHI]",
		"[jklJKL]",
		"[mnoMNO]",
		"[pqrsPQRS]",
		"[tuvTUV]",
		"[wxyzWXYZ]"
	};

	@Override
	protected void init() {

	}

	private String getLettersForDigit(char digit) {
		if (digit >= '2' && digit <= '9')
			return NumberToLetterConverter[digit - '0'];

		return "";
	}

	public final String convertToLetterRegexp(String query) {
		query = query.replaceAll(phoneNumberSpecialChars, "");
		char[] chars = query.toCharArray();

		StringBuilder sb = new StringBuilder();
		sb.append(".*?");

		for (char character : chars) {
			if (character >= '2' && character <= '9')
				sb.append(NumberToLetterConverter[character - '0']);
			else
				sb.append(character).append("?");
		}

		sb.append(".*");
		return sb.toString();
	}

	public final String convertToNumericRegexp(String query) {
		if (query == null || query.length() == 0)
			return query;

		query = query.replaceAll(phoneNumberSpecialChars, "");

		StringBuilder sb = new StringBuilder();
		sb.append(".*?");

		int startIndex = 0;
		char[] chars = query.toCharArray();
		if (chars[0] == '0') {
			sb.append("0?");
			startIndex = 1;
		}

		for (int i = startIndex; i < chars.length; i++) {
			char character = chars[i];
			sb.append(character);
			sb.append(regex).append("?");
		}

		sb.append(".*");
		return sb.toString();
	}
}
