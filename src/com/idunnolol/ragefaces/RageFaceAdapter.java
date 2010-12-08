package com.idunnolol.ragefaces;

import java.util.Arrays;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class RageFaceAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private Map<String, Integer> mRageFaces;
	private String[] mRageFacesOrdered;

	public RageFaceAdapter(Context context, Map<String, Integer> rageFaces) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRageFaces = rageFaces;

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
		imageView.setImageResource(mRageFaces.get(getItem(position)));

		return imageView;
	}
}
