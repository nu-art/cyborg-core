/*
 * The notifications module, is an extendable infrastructure to
 * encapsulate the posting and event handling of Android notifications.
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

package com.nu.art.cyborg.notifications;

import android.app.PendingIntent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;

import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgModuleItem;

public abstract class NotificationHandler
		extends CyborgModuleItem
		implements NotificationKeys {

	protected NotificationsModule module;

	protected void init() {
		module = getModule(NotificationsModule.class);
	}

	protected final Builder createBasicBuilder(short notificationId) {
		return createBasicBuilder(notificationId, new Bundle());
	}

	protected Builder createBasicBuilder(short notificationId, Bundle data) {
		Builder builder = new Builder(getApplication());

		PendingIntent clickedPendingIntent = createPendingIntent(notificationId, Action_Click, data);
		builder.setContentIntent(clickedPendingIntent);

		PendingIntent deletePendingIntent = createPendingIntent(notificationId, Action_Cancel, data);
		builder.setDeleteIntent(deletePendingIntent);
		return builder;
	}

	protected final void postNotification(int notificationId, Builder builder) {
		module.postNotification(notificationId, builder);
	}

	protected void addActionButton(int notificationId, String action, Builder builder, int iconResId, StringResourceResolver stringResolver, Bundle notificationData) {
		PendingIntent moreInfoIntent = createPendingIntent(notificationId, action, notificationData, CyborgModule.getNextRandomPositiveShort());
		builder.addAction(iconResId, stringResolver.getString(cyborg), moreInfoIntent);
	}

	protected final PendingIntent createPendingIntent(int notificationId, String action) {
		return createPendingIntent(notificationId, action, new Bundle());
	}

	protected final PendingIntent createPendingIntent(int notificationId, String action, Bundle data) {
		return createPendingIntent(notificationId, action, data, 0);
	}

	protected final PendingIntent createPendingIntent(int notificationId, String action, Bundle data, int flags) {
		return module.createPendingIntent(notificationId, action, data, flags);
	}

	protected abstract void processNotification(int notificationId, String action, Bundle bundle);
}