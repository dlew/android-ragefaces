package com.idunnolol.ragefaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class RageFaceViewerActivity extends Activity {

	public static String EXTRA_FACE_ID = "EXTRA_FACE_ID";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		int faceId = intent.getIntExtra(EXTRA_FACE_ID, R.raw.rage_original);

		setContentView(R.layout.viewer);

		ImageView faceView = (ImageView) findViewById(R.id.Face);
		faceView.setImageResource(faceId);
	}
}
