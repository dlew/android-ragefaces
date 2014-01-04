package com.idunnolol.ragefaces.test;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import junit.framework.AssertionFailedError;
import android.annotation.TargetApi;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ImageView;

import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.ViewAssertion;
import com.google.common.base.Optional;
import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.app.RageFacePickerActivity;

public class MyTest extends ActivityInstrumentationTestCase2<RageFacePickerActivity> {

	@SuppressWarnings("deprecation")
	public MyTest() {
		// This constructor was deprecated - but we want to support lower API levels.
		super("com.idunnolol.ragefaces", RageFacePickerActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getActivity();
	}

	public void testApp() {
		onData(allOf(is(instanceOf(String.class)), is("cereal_spit"))).check(
				imageViewUsesResource(R.drawable.cereal_spit));

		onView(withText(R.string.dialog_actions_title)).check(doesNotExist());

		onData(allOf(is(instanceOf(String.class)), is("angry_angry"))).perform(click());

		onView(withText(R.string.dialog_actions_title)).check(matches(isDisplayed()));

		onView(withText(R.string.dialog_actions_opt_view)).perform(click());

		onView(withId(R.id.Face)).check(matches(isCompletelyDisplayed()));

		Espresso.pressBack();

		onView(withText(R.string.dialog_actions_title)).check(doesNotExist());

		onData(allOf(is(instanceOf(String.class)), is("megusta_original"))).perform(click());

		onView(withText(R.string.dialog_actions_title)).check(matches(isDisplayed()));
	}

	public static ViewAssertion imageViewUsesResource(final int resId) {
		return new ViewAssertion() {
			// TODO: Make this work on all versions of Android
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public void check(Optional<View> view, Optional<NoMatchingViewException> noViewException) {
				// TODO: Learn description syntax, then use
				if (noViewException.isPresent()) {
					throw noViewException.get();
				}

				ImageView imageView = (ImageView) view.get();
				BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
				if (!bitmapDrawable.getBitmap().sameAs(
						BitmapFactory.decodeResource(imageView.getContext().getResources(), resId))) {
					throw new AssertionFailedError();
				}
			}
		};
	}
}
