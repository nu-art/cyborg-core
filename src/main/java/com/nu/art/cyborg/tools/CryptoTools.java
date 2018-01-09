package com.nu.art.cyborg.tools;

import java.security.MessageDigest;

/**
 * Created by tacb0ss on 06/01/2018.
 */

public class CryptoTools {

	public static String doFingerprint(byte[] certificateBytes, MessageDigest md) {
		md.update(certificateBytes);
		byte[] digest = md.digest();

		StringBuilder toRet = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			if (i != 0)
				toRet.append(":");
			int b = digest[i] & 0xff;
			String hex = Integer.toHexString(b);
			if (hex.length() == 1)
				toRet.append("0");
			toRet.append(hex);
		}
		return toRet.toString();
	}
}
