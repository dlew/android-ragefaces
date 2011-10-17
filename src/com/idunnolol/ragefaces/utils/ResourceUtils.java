package com.idunnolol.ragefaces.utils;

import java.lang.reflect.Field;

import android.util.Log;

import com.idunnolol.ragefaces.RageFacesApp;

public class ResourceUtils {
	public static int getResourceId(Class<?> type, String name) {
		try {
			Field field = type.getField(name);
			int resId = field.getInt(null);
			return resId;
		}
		catch (Exception e) {
			Log.e(RageFacesApp.TAG, "Failure to get raw id.", e);
			return -1;
		}
	}
}
