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

package com.nu.art.cyborg.modules;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build.VERSION;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.common.consts.AnalyticsConstants;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.StringPreference;
import com.nu.art.cyborg.core.modules.crashReport.CrashReportListener;
import com.nu.art.cyborg.common.utils.BootStarterReceiver.OnBootCompletedListener;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.UUID;

@ModuleDescriptor(usesPermissions = {/*permission.READ_PHONE_STATE*/},
									dependencies = {PreferencesModule.class})
public final class AppDetailsModule
		extends CyborgModule
		implements AnalyticsConstants, CrashReportListener {

	@Override
	public void onApplicationCrashed(HashMap<String, Object> moduleCrashData) {
		moduleCrashData.put("Certificate", getCertificate().toString());
	}

	public interface CyborgAppCertificate {

		boolean isDebugCertificate();

		String getMD5();

		boolean isShowingLogs();

		String name();
	}

	private StringPreference installationUUID;

	public enum DummyCertificate
			implements CyborgAppCertificate {
		Default;

		private boolean debuggable = false;

		@Override
		public boolean isDebugCertificate() {
			return debuggable;
		}

		@Override
		public String getMD5() {
			return "";
		}

		@Override
		public boolean isShowingLogs() {
			return debuggable;
		}
	}

	private CyborgAppCertificate certificate = null;

	private boolean automated;

	private boolean debugSimulationMode;

	private Class<? extends Enum<?>> certificateType = DummyCertificate.class;

	/**
	 * @return The Application <b>CURRENT</b> debug state. In general this method would be called to simulate release mode conditions.
	 */
	public final boolean isDebuggable() {
		return getCertificate().isDebugCertificate() && debugSimulationMode;
	}

	public final boolean isAutomated() {
		return automated;
	}

	public final CyborgAppCertificate getCertificate() {
		if (certificate == null)
			checkCertificate();
		return certificate;
	}

	public <Type extends Enum<?>> void setCertificateType(Class<Type> certificateType) {
		this.certificateType = certificateType;
	}

	protected static String doFingerprint(byte[] certificateBytes, String algorithm)
			throws Exception {
		MessageDigest md = MessageDigest.getInstance(algorithm);
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

	public final void setSimulateProduction(boolean debugSimulationMode) {
		this.debugSimulationMode = debugSimulationMode;
	}

	@Override
	protected void printModuleDetails() {
		logInfo("    Found certificate: " + certificate);
		logInfo("    Installation Id: " + installationUUID.get());
		logInfo("    OS Version: " + VERSION.RELEASE);
		logInfo("    OS Version code: " + VERSION.SDK_INT);
	}

	@Override
	protected void init() {
		PreferencesModule preferences = getModule(PreferencesModule.class);
		installationUUID = preferences.new StringPreference("installationUUID", null);

		String installationId = installationUUID.get();
		if (installationId == null) {
			installationId = UUID.randomUUID().toString() + UUID.randomUUID().toString();
			installationUUID.set(installationId);
		}

		if (isInEditMode()) {
			debugSimulationMode = true;
			return;
		}

		checkCertificate();
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("PackageManagerGetSignatures")
	private <Type extends Enum<?>> void checkCertificate() {
		try {
			CyborgAppCertificate certificate = DummyCertificate.Default;

			PackageManager pm = cyborg.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(cyborg.getPackageName(), PackageManager.GET_SIGNATURES);
			Signature sig = packageInfo.signatures[0];
			String md5Fingerprint = doFingerprint(sig.toByteArray(), "MD5");
			Type[] certificateList = (Type[]) ReflectiveTools.getEnumValues(certificateType);
			logDebug("Certificate found: " + md5Fingerprint);
			for (Type type : certificateList) {
				CyborgAppCertificate _certificate = (CyborgAppCertificate) type;
				logDebug("Certificate(" + _certificate + "): " + _certificate.getMD5());
				if (_certificate.getMD5().toLowerCase().equals(md5Fingerprint.toLowerCase())) {
					certificate = _certificate;
					break;
				}
			}

			if (certificate == DummyCertificate.Default) {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(sig.toByteArray()));
				String certificateName = cert.getIssuerDN().getName();
				DummyCertificate.Default.debuggable = certificateName.contains("Android Debug");
			}

			logInfo("Found Certificate: " + certificate.name());
			debugSimulationMode = certificate.isDebugCertificate();
			this.certificate = certificate;
		} catch (Exception e) {
			logInfo("Error getting certificate, assuming release version...", e);
			throw new MUST_NeverHappenedException("Why the Fuck did that happened...", e);
		}
	}

	public final void onBootCompleted() {
		dispatchModuleEvent("Boot completed", new Processor<OnBootCompletedListener>() {
			@Override
			public void process(OnBootCompletedListener onBootCompletedListener) {
				onBootCompletedListener.onBootCompleted();
			}
		});
	}

	public final String getInstallationUUID() {
		return installationUUID.get();
	}
}
