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

@ModuleDescriptor(usesPermissions = {
	permission.READ_CONTACTS,
	permission.READ_PHONE_STATE
})
public class ContactsModule
	extends CyborgModule {

	private static final String phoneNumberSpecialChars = "- \\*N\\(\\)/,;\\+\\.";
	private static final String AlphaBeticChars = "[a-z][A-Z]";
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

	public static String convertToLetterRegexp(String query) {
		query = query.replaceAll(phoneNumberSpecialChars, "");
		query = query.replaceAll(AlphaBeticChars, "");

		char[] chars = query.toCharArray();

		StringBuilder sb = new StringBuilder();
		sb.append(".*?");

		for (char character : chars) {
			if (character >= '2' && character <= '9')
				sb.append(NumberToLetterConverter[character - '2']);
			else
				sb.append(character).append("?");
		}

		sb.append(".*");
		return sb.toString();
	}

	public static String convertToNumericRegexp(String query) {
		return convertToNumericRegexp(query, false);
	}

	public static String convertToNumericRegexp(String query, boolean strict) {
		if (query == null || query.length() == 0)
			return query;

		query = query.replaceAll(phoneNumberSpecialChars, "");
		query = query.replaceAll(AlphaBeticChars, "");

		StringBuilder sb = new StringBuilder();
		sb.append(".*?");

		int startIndex = 0;
		char[] chars = query.toCharArray();
		if (!strict && chars.length > 0 && chars[0] == '0') {
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

	public static String getInitialsFromName(String name) {
		name = removeSpecialCharsFromName(name);
		String[] split = name.split(" ");
		String initials = "";
		if (split.length == 0)
			return "N/A";

		for (String aSplit : split) {
			if (aSplit.length() <= 0)
				continue;

			initials += String.valueOf(aSplit.charAt(0)).toUpperCase();

			if (initials.length() == 2)
				break;
		}
		return initials;
	}

	public static String removeSpecialCharsFromName(String string) {
		return string.replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
	}
}
