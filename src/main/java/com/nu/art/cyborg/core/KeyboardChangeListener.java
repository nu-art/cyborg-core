package com.nu.art.cyborg.core;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.interfaces.ActivityLifeCycleImpl;

public class KeyboardChangeListener {

	public interface OnKeyboardVisibilityListener {

		void onVisibilityChanged(boolean visible);
	}

	private final Activity activity;

	private final Cyborg cyborg;

	private OnKeyboardVisibilityListener[] listeners = {};

	private OnGlobalLayoutListener layoutChangeListener;

	public KeyboardChangeListener(Cyborg cyborg, Activity activity) {
		this.cyborg = cyborg;
		this.activity = activity;
	}

	public final void addKeyboardListener(OnKeyboardVisibilityListener listener) {
		listeners = ArrayTools.appendElement(listeners, listener);
		if (layoutChangeListener == null) {
			addActivityLifecycleListener();
		}
	}

	private void addActivityLifecycleListener() {
		activity.getApplication().registerActivityLifecycleCallbacks(new ActivityLifeCycleImpl() {
			@Override
			public void onActivityResumed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				if (listeners.length == 0)
					return;

				addLayoutChangeListener();
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}
		});
	}

	public final void removeKeyboardListener(OnKeyboardVisibilityListener listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	private void removeLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

		ViewTreeObserver viewTreeObserver = activityRootView.getViewTreeObserver();
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			viewTreeObserver.removeOnGlobalLayoutListener(layoutChangeListener);
		} else
			viewTreeObserver.removeGlobalOnLayoutListener(layoutChangeListener);
	}

	private void addLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutChangeListener = new OnGlobalLayoutListener() {

			private final int DefaultKeyboardDP = 100;

			// From @nathanielwolf answer...  Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
			private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);

			private final Rect r = new Rect();

			private boolean wasOpened;

			@Override
			public void onGlobalLayout() {
				int estimatedKeyboardHeight = cyborg.dpToPx(EstimatedKeyboardDP);

				activityRootView.getWindowVisibleDisplayFrame(r);
				int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				boolean isShown = heightDiff >= estimatedKeyboardHeight;

				if (isShown == wasOpened)
					return;

				wasOpened = isShown;
				for (OnKeyboardVisibilityListener listener : listeners) {
					listener.onVisibilityChanged(isShown);
				}
			}
		});
	}
}
