package com.idunnolol.ragefaces.data;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Cache {

	private static final Map<Integer, SoftReference<Bitmap>> mCache = new ConcurrentHashMap<Integer, SoftReference<Bitmap>>();

	public static Bitmap getBitmap(Resources res, Integer id) {
		Bitmap bitmap = null;

		SoftReference<Bitmap> ref = mCache.get(id);
		if (ref != null) {
			bitmap = ref.get();
		}

		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(res, id);
			mCache.put(id, new SoftReference<Bitmap>(bitmap));
		}

		return bitmap;
	}
}
