/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
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

import android.view.View;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;

/**
 * Created by TacB0sS on 24-Jul 2015.
 */
public enum StackTransitions
	implements Transition {

	Slide() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			SlideH.animate(layer, progress, in);
		}
	},
	SlideH() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentWidth = parent.getWidth();

			view.setTranslationX(parentWidth * (in ? -1 + progress : progress));
		}
	},
	SlideV() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentHeight = parent.getHeight();

			view.setTranslationY(parentHeight * (in ? -1 + progress : progress));
		}
	},
	Scale() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			ScaleH.animate(layer, progress, in);
		}
	},
	ScaleH() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentHeight = parent.getHeight();
			int parentWidth = parent.getWidth();

			view.setPivotX((!in ? 1 : 0) * parentWidth);
			view.setPivotY(parentHeight * 0.5f);

			view.setScaleX(in ? progress : 1 - progress);
		}
	},
	ScaleV() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentHeight = parent.getHeight();
			int parentWidth = parent.getWidth();

			view.setPivotY((in ? 0 : 1) * parentHeight);
			view.setPivotX(parentWidth * 0.5f);
			view.setScaleY(in ? progress : 1 - progress);
		}
	},
	Cube() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			CubeH.animate(layer, progress, in);
		}
	},
	CubeH() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentWidth = parent.getWidth();

			progress = (in ? -1 : 0) + progress;
			view.setTranslationX(parentWidth * progress);
			view.setPivotX((!in ? 0 : 1) * view.getWidth());
			view.setPivotY(view.getHeight() * 0.5f);
			view.setRotationY(90f * progress);
		}
	},
	CubeV() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			View parent = (View) view.getParent();
			int parentHeight = parent.getHeight();

			float _progress = (in ? -1 : 0) + progress;
			view.setTranslationY(-parentHeight * _progress);
			view.setPivotY((in ? 0 : 1) * view.getHeight());
			view.setPivotX(view.getWidth() * 0.5f);
			view.setRotationX(90f * _progress);
		}
	},
	Fade() {
		@Override
		public void animate(StackLayerBuilder layer, float progress, boolean in) {
			View view = layer.getRootView();
			view.setAlpha(in ? progress : 1 - progress);
		}
	},
	//
	;

}
