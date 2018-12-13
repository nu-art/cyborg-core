package com.nu.art.cyborg.ui.animations;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import com.nu.art.belog.Logger;

public abstract class SimpleAnimator
	extends Logger {

	private static final int Default_AnimationDuration = 700;

	private int duration = Default_AnimationDuration;
	private float currentProgress;
	private Animator animator;
	private AnimatorListener listener;

	protected SimpleAnimator() {
	}

	public void init(float initialProgress) {
		this.currentProgress = initialProgress;
		onAnimationProgressed(currentProgress);
	}

	public final SimpleAnimator setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	public SimpleAnimator setListener(AnimatorListener listener) {
		this.listener = listener;
		return this;
	}

	public void animateTo(float target) {
		if (target == currentProgress) {
			logDebug("will not animate... target == currentProgress == " + target);
			return;
		}

		if (animator != null && animator.isRunning())
			animator.cancel();

		animator = createAnimate(currentProgress, target);
		animator.setDuration((long) (Math.abs(target - currentProgress) * duration));
		if (animator instanceof ValueAnimator)
			((ValueAnimator) animator).addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					currentProgress = (float) animation.getAnimatedValue();
					onAnimationProgressed(currentProgress);
				}
			});

		if (listener != null)
			animator.addListener(listener);
		animator.start();
	}

	protected abstract void onAnimationProgressed(float currentProgress);

	protected Animator createAnimate(float currentProgress, float target) {
		return ValueAnimator.ofFloat(currentProgress, target);
	}

	public void cancel() {
		animator.cancel();
	}
}
