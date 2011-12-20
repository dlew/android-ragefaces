package com.idunnolol.ragefaces.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.idunnolol.ragefaces.R;

public class ViewerFragment extends Fragment {

	public static String EXTRA_FACE_ID = "EXTRA_FACE_ID";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.viewer, container, false);

		ImageView faceImageView = (ImageView) v.findViewById(R.id.Face);
		int faceId = getArguments().getInt(EXTRA_FACE_ID, R.raw.rage_original);
		faceImageView.setImageResource(faceId);

		return v;
	}

}
