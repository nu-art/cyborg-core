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

package com.nu.art.cyborg.common.beans;

public class ServerDetails {

	private NetworkSetting serverNetworkSettings;

	private String serverDomainURL;

	private String serverRelativePathURL;

	public ServerDetails(NetworkSetting serverNetworkSettings, String serverDomainURL, String serverRelativePathURL) {
		this.serverNetworkSettings = serverNetworkSettings;
		setServerDomainURL(serverDomainURL);
		setServerRelativePathURL(serverRelativePathURL);
	}

	public ServerDetails() {}

	public final NetworkSetting getServerNetworkSettings() {
		return serverNetworkSettings;
	}

	public final void setServerNetworkSettings(NetworkSetting serverNetworkSettings) {
		this.serverNetworkSettings = serverNetworkSettings;
	}

	public final String getServerDomainURL() {
		return serverDomainURL;
	}

	public final void setServerDomainURL(String serverDomainURL) {
		if (!serverDomainURL.endsWith("/")) {
			serverDomainURL += "/";
		}
		this.serverDomainURL = serverDomainURL;
	}

	public final String getServerRelativePathURL() {
		return serverRelativePathURL;
	}

	public final void setServerRelativePathURL(String serverRelativePathURL) {
		while (serverRelativePathURL.startsWith("/")) {
			serverRelativePathURL = serverRelativePathURL.substring(1, serverRelativePathURL.length() - 1);
		}
		this.serverRelativePathURL = serverRelativePathURL;
	}

	public final String getURL_AccordingToNetwork(String networkName) {
		if (serverNetworkSettings != null && serverNetworkSettings.getName().equals(networkName)) {
			return serverNetworkSettings.getUrl() + serverRelativePathURL;
		}
		return serverDomainURL + serverRelativePathURL;
	}
}
