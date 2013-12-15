package com.idunnolol.ragefaces.data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class Cache {

	private static final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);

	private static final LruCache<Integer, Bitmap> sMemoryCache = new LruCache<Integer, Bitmap>(MAX_MEMORY / 3) {
		@Override
		protected int sizeOf(Integer key, Bitmap bitmap) {
			return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
		}
	};

	public static Bitmap getBitmap(Resources res, Integer id) {
		Bitmap bitmap = sMemoryCache.get(id);

		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(res, id);
			sMemoryCache.put(id, bitmap);
		}

		return bitmap;
	}
}
