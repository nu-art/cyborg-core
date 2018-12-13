package com.nu.art.cyborg.ui.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

import java.util.ArrayList;

public abstract class RippleAnimator {

	private float currentProgress;
	private int repeatCount = ValueAnimator.INFINITE;

	private ArrayList<Animator> animators = new ArrayList<>();

	public RippleAnimator setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
		return this;
	}

	public void startAnimation(int duration, View... toAnimate) {
		startAnimation(duration, 0f, toAnimate);
	}

	public void startAnimation(int duration, float initProgress, View... toAnimate) {
		int delay = (int) (1f * duration / (toAnimate.length));
		for (int i = 0; i < toAnimate.length; i++) {
			View imageView = toAnimate[i];
			onAnimationProgressed(imageView, 0);
			ValueAnimator valueAnimator = animateProgress(imageView, i * delay, duration);
			animators.add(valueAnimator);
		}
	}

	private ValueAnimator animateProgress(final View imageView, int startDelay, int duration) {
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
		valueAnimator.setRepeatCount(repeatCount);
		valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				currentProgress = (float) animation.getAnimatedValue();
				onAnimationProgressed(imageView, currentProgress);
			}
		});
		valueAnimator.setStartDelay(startDelay);
		valueAnimator.setDuration(duration);
		valueAnimator.start();
		return valueAnimator;
	}

	protected abstract void onAnimationProgressed(View imageView, float currentProgress);

	public void cancel() {
		for (Animator animator : animators) {
			animator.cancel();
		}
	}
}