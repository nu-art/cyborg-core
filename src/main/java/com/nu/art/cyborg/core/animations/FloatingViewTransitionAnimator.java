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

package com.nu.art.cyborg.core.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nu.art.core.generics.Function;
import com.nu.art.cyborg.common.implementors.AnimatorListenerImpl;
import com.nu.art.cyborg.common.utils.Tools;
import com.nu.art.cyborg.core.CyborgStackController.StackLayer;
import com.nu.art.cyborg.core.CyborgStackController.StackTransitionAnimator;

/**
 * Created by TacB0sS on 30-Apr 2016.
 */
public class FloatingViewTransitionAnimator
		extends StackTransitionAnimator {

	private final Function<StackLayer, View> getOriginView;

	private final Function<StackLayer, View> getTargetView;

	public FloatingViewTransitionAnimator(final int originViewId, final int targetViewId) {
		this.getOriginView = new Function<StackLayer, View>() {
			@Override
			public View map(StackLayer stackLayer) {
				return stackLayer.getRootView().findViewById(originViewId);
			}
		};
		this.getTargetView = new Function<StackLayer, View>() {
			@Override
			public View map(StackLayer stackLayer) {
				return stackLayer.getRootView().findViewById(targetViewId);
			}
		};
	}

	public FloatingViewTransitionAnimator(Function<StackLayer, View> getOriginView, Function<StackLayer, View> getTargetView) {
		this.getOriginView = getOriginView;
		this.getTargetView = getTargetView;
	}

	private class WidthEvaluator
			extends IntEvaluator {

		private View v;

		public WidthEvaluator(View v) {
			this.v = v;
		}

		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int num = super.evaluate(fraction, startValue, endValue);
			ViewGroup.LayoutParams params = v.getLayoutParams();
			params.width = num;
			v.setLayoutParams(params);
			return num;
		}
	}

	private class HeightEvaluator
			extends IntEvaluator {

		private View v;

		public HeightEvaluator(View v) {
			this.v = v;
		}

		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int num = super.evaluate(fraction, startValue, endValue);
			ViewGroup.LayoutParams params = v.getLayoutParams();
			params.height = num;
			v.setLayoutParams(params);
			return num;
		}
	}

	@Override
	public void animateIn(StackLayer originLayer, StackLayer targetLayer, int duration, AnimationListener listener) {

		// prepare data for animation
		final View originViewToAnimate = getOriginView.map(originLayer);
		final View targetViewToAnimate = getTargetView.map(targetLayer);

		final Bitmap toAnimate = getCroppedViewImage(originLayer.getRootView(), originViewToAnimate);

		renderFromTo(originLayer.getRootView(), originViewToAnimate, targetViewToAnimate, toAnimate, duration, listener);
	}

	@Override
	public void animateOut(StackLayer originLayer, StackLayer targetLayer, int duration, final AnimationListener listener) {
		// prepare data for animation
		final View originViewToAnimate = getOriginView.map(originLayer);
		final View targetViewToAnimate = getTargetView.map(targetLayer);
		final Bitmap toAnimate = getCroppedViewImage(originLayer.getRootView(), originViewToAnimate);

		renderFromTo(originLayer.getRootView(), targetViewToAnimate, originViewToAnimate, toAnimate, duration, listener);
	}

	private void renderFromTo(View fromParent,
														final View viewToAnimateFrom,
														final View viewToAnimateTo,
														Bitmap imageToAnimate,
														int duration,
														final AnimationListener listener) {
		final FrameLayout rootView = (FrameLayout) fromParent.getRootView().findViewById(android.R.id.content);
		Context context = rootView.getContext();

		final RelativeLayout renderingLayer = new RelativeLayout(context);
		renderingLayer.setClipChildren(false);
		rootView.addView(renderingLayer);

		final ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(imageToAnimate);
		renderingLayer.addView(imageView);

		AnimatorSet animationSet = new AnimatorSet();
		animationSet.addListener(new AnimatorListenerImpl() {
			@Override
			public void onAnimationStart(Animator animator) {
				// the origin view should remain a bit longer to avoid the transition hiccup between the original view and the image to be animated...
				viewToAnimateFrom.post(new Runnable() {
					@Override
					public void run() {
						viewToAnimateFrom.setVisibility(View.INVISIBLE);
					}
				});

				// the target view should be hidden from the get go!
				viewToAnimateTo.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				rootView.removeView(renderingLayer);
				viewToAnimateTo.setVisibility(View.VISIBLE);
				if (listener != null)
					listener.onAnimationEnd(null);
			}
		});
		// extract the view real area's on screen
		Rect originRect = Tools.getViewRealRect(viewToAnimateFrom);
		Rect targetRect = Tools.getViewRealRect(viewToAnimateTo);

		imageView.setTranslationX(originRect.left);
		imageView.setTranslationY(originRect.top);
		Animator translateX = ObjectAnimator.ofFloat(imageView, "translationX", targetRect.left);
		Animator translateY = ObjectAnimator.ofFloat(imageView, "translationY", targetRect.top);

		Animator animateW = ValueAnimator.ofObject(new WidthEvaluator(imageView), originRect.width(), targetRect.width());
		Animator animateH = ValueAnimator.ofObject(new HeightEvaluator(imageView), originRect.height(), targetRect.height());

		animationSet.setDuration(duration);
		animationSet.playTogether(translateX, translateY, animateW, animateH);
		animationSet.start();
	}

	private Bitmap getCroppedViewImage(View origin, View viewToCrop) {
		boolean drawingEnabled = origin.isDrawingCacheEnabled();
		if (!drawingEnabled)
			origin.setDrawingCacheEnabled(true);

		Bitmap fullImage = origin.getDrawingCache();
		int width = viewToCrop.getRight() - viewToCrop.getLeft();
		int height = viewToCrop.getBottom() - viewToCrop.getTop();
		Bitmap croppedImage = Bitmap.createBitmap(fullImage, viewToCrop.getLeft(), viewToCrop.getTop(), width, height);

		if (!drawingEnabled)
			origin.setDrawingCacheEnabled(false);

		return croppedImage;
	}
}
