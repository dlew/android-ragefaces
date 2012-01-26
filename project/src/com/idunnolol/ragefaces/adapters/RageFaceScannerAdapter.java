package com.idunnolol.ragefaces.adapters;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.RageFacesApp;
import com.idunnolol.ragefaces.data.Cache;

public class RageFaceScannerAdapter extends BaseAdapter implements RawRetriever {

	private Resources mResources;
	private LayoutInflater mInflater;

	private Map<String, Integer> mRageFaces;
	private String[] mRageFacesOrdered;

	public RageFaceScannerAdapter(Context context) {
		mResources = context.getResources();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Use reflection to make a list of all rage faces contained in /res/raw/
		mRageFaces = new HashMap<String, Integer>();
		for (Field field : R.raw.class.getFields()) {
			try {
				mRageFaces.put(field.getName(), field.getInt(null));
			}
			catch (Exception e) {
				Log.e(RageFacesApp.TAG, "HOW DID THIS HAPPEN FFFFUUUUU", e);
			}
		}

		// Setup a specific ordering for the faces
		int numFaces = mRageFaces.size();
		mRageFacesOrdered = new String[numFaces];
		int n = 0;
		for (String key : mRageFaces.keySet()) {
			mRageFacesOrdered[n++] = key;
		}
		Arrays.sort(mRageFacesOrdered);
	}

	@Override
	public int getCount() {
		return mRageFacesOrdered.length;
	}

	@Override
	public Object getItem(int position) {
		return mRageFacesOrdered[position];
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		return mRageFaces.get(mRageFacesOrdered[position]);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.face, parent, false);
		}

		ImageView imageView = (ImageView) convertView;
		imageView.setImageBitmap(Cache.getBitmap(mResources, mRageFaces.get(getItem(position))));

		return imageView;
	}

	@Override
	public int getRawResourceId(String drawableName) {
		return mRageFaces.get(drawableName);
	}
}
