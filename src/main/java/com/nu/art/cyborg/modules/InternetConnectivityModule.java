package com.nu.art.cyborg.modules;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.net.InetAddress;

public class InternetConnectivityModule
		extends CyborgModule {

	private static final String DEFAULT_HOST = "google.com";

	public interface InternetConnectivityListener {

		void onInternetConnectivityChanged();
	}

	private Handler handler;

	private volatile boolean isConnected;

	@Override
	protected void init() {
		handler = getModule(ThreadsModule.class).getDefaultHandler("Internet Check");
		registerReceiver(ConnectivityCheckReceiver.class);
	}

	public boolean isConnected() {
		return isConnected;
	}

	private void checkInternetConnectionAsync() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress.getByName(DEFAULT_HOST);
					isConnected = true;
				} catch (Exception e) {
					logError("Couldn't ping google.com", e);
					isConnected = false;
				}
				dispatchEvent("Internet Check - " + (isConnected ? "Has Internet" : "No Internet"), new Processor<InternetConnectivityListener>() {
					@Override
					public void process(InternetConnectivityListener listener) {
						listener.onInternetConnectivityChanged();
					}
				});
			}
		});
	}

	private static class ConnectivityCheckReceiver
			extends CyborgReceiver<InternetConnectivityModule> {

		protected ConnectivityCheckReceiver() {
			super(InternetConnectivityModule.class, ConnectivityManager.CONNECTIVITY_ACTION);
		}

		@Override
		protected void onReceive(Intent intent, InternetConnectivityModule module) {
			module.checkInternetConnectionAsync();
		}
	}
}