///*
// * Copyright (c) 2015 to Adam van der Kruk (Zehavi) AKA TacB0sS - Nu-Art Software
// * Restricted usage under Binpress license
// *
// * For more details go to: http://cyborg.binpress.com/product/cyborg/2052
// */
//
//package com.nu.art.cyborg.ui.animations.viewBasedAnimations;
//
//import android.R;
//import android.app.Activity;
//import android.graphics.Rect;
//import android.support.annotation.NonNull;
//import android.view.View;
//import android.view.ViewTreeObserver;
//import android.view.ViewTreeObserver.OnPreDrawListener;
//import android.widget.FrameLayout;
//import android.widget.RelativeLayout;
//
//import com.nu.art.cyborg.core.CyborgController;
//import com.nu.art.cyborg.ui.animations.animationTransition.StackLayerAnimator;
//
///// need to think, if I want to reimplement this where the animation is not connected to the animation transition.
///// it is a separate animation over the two controllers transition.
//
///**
// * Created by TacB0sS on 14-Jul 2015.
// */
//public class LeadingViewAnimationBuilder<ViewType extends View>
//		extends StackLayerAnimator {
//
//	public interface ViewResolver<ViewType extends View, OriginController extends CyborgController, TargetController extends CyborgController> {
//
//		/**
//		 * @return The rootView that is in the origin Controller to prepare from.
//		 */
//		ViewType resolveOriginView(OriginController originController);
//
//		/**
//		 * @return The rootView that is in the target Controller to prepare to.
//		 */
//		ViewType resolveTargetView(TargetController targetController);
//	}
//
//	public interface ViewGenerator<ViewType> {
//
//		ViewType generate(ViewType origin, ViewType target);
//	}
//
//	private class LeadingViewAnimation {
//
//		private final FrameLayout rootFrameLayout;
//
//		private final RelativeLayout overlay;
//
//		private final Rect origin;
//
//		private final Rect target;
//
//		private final ViewType transitionView;
//
//		LeadingViewAnimation(ViewType transitionView, Rect origin, Rect target) {
//			rootFrameLayout = (FrameLayout) activity.findViewById(R.id.content);
//			overlay = new RelativeLayout(activity);
//			this.transitionView = transitionView;
//			overlay.addView(transitionView);
//			this.origin = origin;
//			this.target = target;
//		}
//
//		public void dispose() {
//			rootFrameLayout.removeView(overlay);
//		}
//
//		public void addView(ViewType tempView) {
//			overlay.addView(tempView);
//		}
//	}
//
//	private final Activity activity;
//
//	private ViewResolver<ViewType, ?, ?> viewResolver;
//
//	private ViewGenerator<ViewType> viewGenerator;
//
//	private LeadingViewAnimation overlay;
//
//	public LeadingViewAnimationBuilder(Activity activity) {
//		this.activity = activity;
//	}
//
//	@SuppressWarnings("unchecked")
//	public <Origin extends CyborgController, Target extends CyborgController> void start(Origin originController, Target targetController) {
//		ViewResolver<ViewType, Origin, Target> viewResolver = (ViewResolver<ViewType, Origin, Target>) LeadingViewAnimationBuilder.this.viewResolver;
//		final ViewType originView = viewResolver.resolveOriginView(originController);
//		final ViewType targetView = viewResolver.resolveTargetView(targetController);
//		ViewTreeObserver observer = targetView.getViewTreeObserver();
//		observer.addOnPreDrawListener(new OnPreDrawListener() {
//			public boolean onPreDraw() {
//				targetView.getViewTreeObserver().removeOnPreDrawListener(this);
//				int[] screenLocation = new int[2];
//
//				Rect origin = getViewRect(screenLocation, originView);
//				Rect target = getViewRect(screenLocation, targetView);
//
//				overlay = new LeadingViewAnimation(viewGenerator.generate(originView, targetView), origin, target);
//				return true;
//			}
//		});
//
//		//		ViewType tempView = viewGenerator.process(viewResolver.resolveOriginView());
//		//		overlay.addView(tempView);
//	}
//
//	@NonNull
//	private Rect getViewRect(int[] screenLocation, ViewType originView) {
//		originView.getLocationOnScreen(screenLocation);
//		return new Rect(screenLocation[0], screenLocation[1], originView.getMeasuredWidth(), originView.getMeasuredHeight());
//	}
//
//	/**
//	 * @param viewResolver To resolve the source and target rootView of a forward and backward transition animation.
//	 */
//	public void setViewResolver(ViewResolver<ViewType, ?, ?> viewResolver) {
//		this.viewResolver = viewResolver;
//	}
//
//	public void setViewGenerator(ViewGenerator<ViewType> viewGenerator) {
//		this.viewGenerator = viewGenerator;
//	}
//
//	private void dispose() {
//		overlay.dispose();
//	}
//}
