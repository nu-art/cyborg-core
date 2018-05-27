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

package com.nu.art.cyborg.modules;

import android.graphics.Bitmap;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Getter;
import com.nu.art.core.utils.PoolQueue;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.CacheModule.Cacheable;

import java.lang.ref.WeakReference;

public class BlurModule
	extends CyborgModule {

	public static final String DebugFlag = "Debug_" + BlurModule.class.getSimpleName();

	private PoolQueue<BlurBuilder> poolQueue = new PoolQueue<BlurBuilder>() {
		@Override
		protected void onExecutionError(BlurBuilder item, Throwable e) {
			logError("Error blurring '" + item.name + "'", e);
		}

		@Override
		protected void executeAction(BlurBuilder blurBuilder)
			throws Exception {
			blurSync(blurBuilder);
		}
	};

	private int blurringThreads = 1;

	public void setBluringThreads(int blurringThreads) {
		this.blurringThreads = blurringThreads;
	}

	@Override
	protected void init() {
		poolQueue.createThreads("blur-thread", blurringThreads);
	}

	public final BlurBuilder createBlur() {
		return new BlurBuilder();
	}

	@SuppressWarnings("WeakerAccess")
	public final class BlurBuilder {

		private WeakReference<Processor<Bitmap>> onSuccess;
		private WeakReference<Getter<Bitmap>> bitmapResolver;
		private Cacheable cacheable;
		private String name;
		private int radius = 20;

		public BlurBuilder setBitmapResolver(Getter<Bitmap> bitmapResolver) {
			this.bitmapResolver = new WeakReference<>(bitmapResolver);
			return this;
		}

		public BlurBuilder setOnSuccess(Processor<Bitmap> onSuccess) {
			this.onSuccess = new WeakReference<>(onSuccess);
			return this;
		}

		public BlurBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public BlurBuilder setRadius(int radius) {
			this.radius = radius;
			return this;
		}

		public BlurBuilder setCacheable(Cacheable cacheable) {
			this.cacheable = cacheable;
			return this;
		}

		public BlurBuilder setCacheable(String cacheToFolder, String key, String suffix) {
			return setCacheable(getModule(CacheModule.class).new Cacheable().setKey(key).setSuffix(suffix).setMustCache(false).setPathToDir(cacheToFolder));
		}

		public BlurBuilder setBitmap(final Bitmap bitmap) {
			return setBitmapResolver(new Getter<Bitmap>() {
				@Override
				public Bitmap get() {
					return bitmap;
				}
			});
		}

		public final void blur() {
			poolQueue.addItem(this);
		}
	}

	public final void blurSync(BlurBuilder blurBuilder) {
		if (isMainThread())
			throw new BadImplementationException("MUST not call this method on the UI Thread!");

		long started = System.currentTimeMillis();
		Getter<Bitmap> bitmapGetter = blurBuilder.bitmapResolver.get();
		if (bitmapGetter == null) {
			logWarning("blur ignored.. bitmapGetter is not set or was GC");
			return;
		}

		logDebug("+---- starting blur " + blurBuilder.name);
		Bitmap bitmap = bitmapGetter.get();
		blurImpl(bitmap, blurBuilder.radius);
		Processor<Bitmap> bitmapProcessor = blurBuilder.onSuccess.get();
		if (bitmapProcessor != null)
			bitmapProcessor.process(bitmap);

		logInfo("+---- blur took: " + (System.currentTimeMillis() - started) + "ms");
	}

	private void blurImpl(Bitmap bitmap, int radius) {
		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int[] pix = new int[wh];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;

		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++)
			dv[i] = (i / divsum);

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		logDebug("+-- radius: " + radius);
		logDebug("+-- bitmap (w, h): (" + w + ", " + h + ")");
		logDebug("+-- image footprint: " + (pix.length + r.length + g.length + b.length) + " * int");
		logDebug("+-- pix: " + pix.length);
		logDebug("+-- r: " + r.length);
		logDebug("+-- g: " + g.length);
		logDebug("+-- b: " + b.length);
		logDebug("+-- vmin: " + vmin.length);
		logDebug("+-- dv: " + dv.length);
		logDebug("+-- stack: " + stack.length + " * 3");

		int[] sir; // assigned, not allocated!!
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}

		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}

			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		if (!bitmap.isMutable())
			bitmap = bitmap.copy(bitmap.getConfig(), true);

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
	}
}
