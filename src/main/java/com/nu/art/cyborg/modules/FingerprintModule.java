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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec.Builder;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.io.IOException;
import java.io.NotActiveException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * Created by tacb0ss on 29/04/2018.
 */
@TargetApi(VERSION_CODES.M)
public class FingerprintModule
	extends CyborgModule {

	private String keystoreName = "AndroidKeyStore";
	private KeyStore keyStore;
	private KeyGenerator keyGenerator;
	private String cipherTransformation = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
	private KeyguardManager keyguardManager;
	private CancellationSignal cancellationSignal;
	private Handler handler;

	@Override
	protected void init() {
		keyguardManager = getSystemService(KeyguardService);
		handler = getModule(ThreadsModule.class).getDefaultHandler("fingerprint");
		try {
			keyStore = KeyStore.getInstance(keystoreName);
			keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keystoreName);
		} catch (Exception e) {
			logWarning("No Fingerprint support", e);
		}
	}

	private Cipher createCipher(String name)
		throws IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException,
		       NoSuchPaddingException, InvalidAlgorithmParameterException {
		if (!isEnabled())
			throw new NotActiveException("Fingerprint feature is not enabled");

		Cipher cipher;
		cipher = Cipher.getInstance(cipherTransformation);

		keyStore.load(null);
		// Set the alias of the entry in Android KeyStore where the key will appear
		// and the constrains (purposes) in the constructor of the Builder

		Builder builder = new Builder(name, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
		builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
		builder.setUserAuthenticationRequired(true);
		builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

		// This is a workaround to avoid crashes on devices whose API level is < 24
		// because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
		// visible on API level +24.
		// Ideally there should be a compat library for KeyGenParameterSpec.Builder but
		// which isn't available yet.
		if (VERSION.SDK_INT >= VERSION_CODES.N) {
			builder.setInvalidatedByBiometricEnrollment(true);
		}

		keyGenerator.init(builder.build());
		keyGenerator.generateKey();

		keyStore.load(null);
		SecretKey key = (SecretKey) keyStore.getKey(name, null);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher;
	}

	@SuppressLint("MissingPermission")
	@RequiresPermission(USE_FINGERPRINT)
	public boolean hasEnrolledFingerprints() {
		if (!isEnabled())
			return false;

		// Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint
		return VERSION.SDK_INT >= VERSION_CODES.M && getFingerprintManager().hasEnrolledFingerprints();
	}

	@RequiresPermission(USE_FINGERPRINT)
	public void startListening(String cipherName)
		throws IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException,
		       InvalidKeyException, InvalidAlgorithmParameterException {

		Cipher cipher = createCipher(cipherName);
		startListening(cipher);
	}

	@RequiresPermission(USE_FINGERPRINT)
	public void startListening(Cipher cipher) {
		if (!isEnabled() || !hasEnrolledFingerprints()) {
			return;
		}

		if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
			logWarning("Already listening");
			return;
		}

		CryptoObject cryptoObject = new CryptoObject(cipher);
		cancellationSignal = new CancellationSignal();
		// The line below prevents the false positive inspection from Android Studio
		// noinspection ResourceType
		getFingerprintManager().authenticate(cryptoObject, cancellationSignal, 0 /* flags */, new AuthenticationCallback() {
			@Override
			public void onAuthenticationError(final int errorCode, final CharSequence errString) {
				cancellationSignal = null;
				dispatchGlobalEvent("Authentication help(" + errorCode + "): " + errString, FingerprintAuthenticationListener.class, new Processor<FingerprintAuthenticationListener>() {
					@Override
					public void process(FingerprintAuthenticationListener listener) {
						listener.onAuthenticationError(errorCode, errString);
					}
				});
			}

			@Override
			public void onAuthenticationHelp(final int helpCode, final CharSequence helpString) {
				dispatchGlobalEvent("Authentication help(" + helpCode + "): " + helpString, FingerprintAuthenticationListener.class, new Processor<FingerprintAuthenticationListener>() {
					@Override
					public void process(FingerprintAuthenticationListener listener) {
						listener.onAuthenticationHelp(helpCode, helpString);
					}
				});
			}

			@Override
			public void onAuthenticationSucceeded(final AuthenticationResult result) {
				cancellationSignal = null;
				dispatchGlobalEvent("Authentication successful", FingerprintAuthenticationListener.class, new Processor<FingerprintAuthenticationListener>() {
					@Override
					public void process(FingerprintAuthenticationListener listener) {
						listener.onAuthenticationSucceeded(result);
					}
				});
			}

			@Override
			public void onAuthenticationFailed() {
				dispatchGlobalEvent("Authentication failed", FingerprintAuthenticationListener.class, new Processor<FingerprintAuthenticationListener>() {
					@Override
					public void process(FingerprintAuthenticationListener listener) {
						listener.onAuthenticationFailed();
					}
				});
			}
		}, handler);
	}

	public void stopListening() {
		if (cancellationSignal == null || cancellationSignal.isCanceled()) {
			logWarning("Already stopped");
			return;
		}

		cancellationSignal.cancel();
	}

	@RequiresApi(api = VERSION_CODES.M)
	private FingerprintManager getFingerprintManager() {
		return getSystemService(new ServiceType<FingerprintManager>(Context.FINGERPRINT_SERVICE));
	}

	public void setKeystoreName(String keystoreName) {
		this.keystoreName = keystoreName;
	}

	public void setCipherTransformation(String cipherTransformation) {
		this.cipherTransformation = cipherTransformation;
	}

	public boolean isEnabled() {
		return VERSION.SDK_INT >= VERSION_CODES.M && keyStore != null && keyGenerator != null;
	}

	public boolean isKeyguardSecured() {
		// Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint
		return !keyguardManager.isKeyguardSecure();
	}

	public boolean isListening() {
		return cancellationSignal != null && !cancellationSignal.isCanceled();
	}

	public interface FingerprintAuthenticationListener {

		void onAuthenticationError(int errorCode, CharSequence errString);

		void onAuthenticationHelp(int helpCode, CharSequence helpString);

		void onAuthenticationSucceeded(AuthenticationResult result);

		void onAuthenticationFailed();
	}
}
