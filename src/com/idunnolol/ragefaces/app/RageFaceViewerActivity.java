package com.idunnolol.ragefaces.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.idunnolol.ragefaces.app.fragment.ViewerFragment;

public class RageFaceViewerActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ViewerFragment viewer = new ViewerFragment();
			viewer.setArguments(getIntent().getExtras());
			ft.add(android.R.id.content, viewer);
			ft.commit();
		}
	}
}
