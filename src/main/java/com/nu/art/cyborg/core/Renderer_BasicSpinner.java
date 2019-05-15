package com.nu.art.cyborg.core;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.widget.TextView;

public abstract class Renderer_BasicSpinner<Type>
	extends ItemRenderer<Type> {

	@IdRes
	private int textViewId;
	protected TextView tvText;

	public Renderer_BasicSpinner(@LayoutRes int layoutId, @IdRes int textViewId) {
		super(layoutId);
		this.textViewId = textViewId;
	}

	public Renderer_BasicSpinner() {
		this(android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
	}

	@Override
	protected void extractMembers() {
		tvText = getViewById(TextView.class, textViewId);
	}
}
