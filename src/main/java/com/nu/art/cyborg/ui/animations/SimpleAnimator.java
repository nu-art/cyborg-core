package com.nu.art.cyborg.ui.animations;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.animation.Interpolator;

import com.nu.art.belog.Logger;

/**
 * Animates between 0 and 1, float.
 * The duration is set for the maximum animation, from 0 to 1 or vice versa, if only animation a part of this value(say from 0 to 0.5), the duration will be
 * exactly relatively shorter.
 */
public class SimpleAnimator
	extends Logger {

	public interface AnimatorProgressListener {

		void onAnimationProgressed(float currentProgress);
	}

	public static final int Default_AnimationDuration = 700;

	private int duration = Default_AnimationDuration;
	private float currentProgress;
	private Animator animator;
	private AnimatorListener listener;
	private AnimatorProgressListener updateListener;
	private Interpolator interpolator;

	public SimpleAnimator() {
	}

	public void init(float initialProgress) {
		this.currentProgress = initialProgress;
	}

	public final SimpleAnimator setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	public void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	public SimpleAnimator setListener(AnimatorListener listener) {
		this.listener = listener;
		return this;
	}

	public SimpleAnimator setListener(AnimatorProgressListener listener) {
		this.updateListener = listener;
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
					updateListener.onAnimationProgressed(currentProgress);
				}
			});

		if (listener != null)
			animator.addListener(listener);
		animator.start();
	}

	protected Animator createAnimate(float currentProgress, float target) {
		return ValueAnimator.ofFloat(currentProgress, target);
	}

	public void cancel() {
		if (animator != null)
			animator.cancel();
	}

	public int getDuration() {
		return duration;
	}
}
