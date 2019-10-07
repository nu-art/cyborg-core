package com.nu.art.cyborg.modules.apps;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.ui.ApplicationLauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AppInstallerModule
	extends CyborgModule {

	@Override
	protected void init() {}

	/**
	 * Requires app to be DeviceOwner or DeviceAdmin
	 */
	public void installPackage(File apk, String packageName)
		throws IOException {
		Context context = getApplicationContext();
		PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

		PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
		params.setAppPackageName(packageName);
		int sessionId = packageInstaller.createSession(params);
		PackageInstaller.Session session = null;

		try {
			session = packageInstaller.openSession(sessionId);

			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(apk);
				out = session.openWrite("COSU", 0, -1);
				byte[] buffer = new byte[65536];
				int c;
				while ((c = in.read(buffer)) != -1) {
					out.write(buffer, 0, c);
				}
				session.fsync(out);
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}

			Intent intent = new Intent(context, ApplicationLauncher.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			session.commit(pendingIntent.getIntentSender());
		} finally {
			if (session != null)
				session.close();
		}
	}
}
