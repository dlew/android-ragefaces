package com.idunnolol.ragefaces.test;

import java.util.List;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.AbsListView;

import com.idunnolol.ragefaces.app.RageFacePickerActivity;
import com.jayway.android.robotium.solo.Solo;

public class RageFacesTest extends ActivityInstrumentationTestCase2<RageFacePickerActivity> {

	private Solo mSolo;

	public RageFacesTest() {
		// Using deprecated constructor for back-compat
		super("com.idunnolol.ragefaces", RageFacePickerActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		mSolo = new Solo(getInstrumentation(), getActivity());
	}

	public void testClickingThroughGrid() throws Exception {
		List<AbsListView> listViews = mSolo.getCurrentViews(AbsListView.class);
		AbsListView rageFaceListView = listViews.get(0);

		int count = rageFaceListView.getCount();
		for (int a = 1; a < count; a += 5) {
			while (a > rageFaceListView.getLastVisiblePosition()) {
				mSolo.scrollDownList(rageFaceListView);
			}

			// Click on view face, view face, go back
			mSolo.clickInList(a - rageFaceListView.getFirstVisiblePosition());
			mSolo.clickInList(2);
			mSolo.sleep(1000); // Need sleep just in case going to new Activity takes a while...
			mSolo.goBack();
		}
	}

	@Override
	public void tearDown() throws Exception {
		mSolo.finishOpenedActivities();
	}

}
