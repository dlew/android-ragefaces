package com.idunnolol.ragefaces.uiautomator;

import java.io.IOException;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class RageFacesTest extends UiAutomatorTestCase {

	public void testDemo() throws UiObjectNotFoundException {
		// Simulate a short press on the HOME button.
		getUiDevice().pressHome();

		startApp("com.idunnolol.ragefaces", "com.idunnolol.ragefaces.app.RageFacePickerActivity");

		UiScrollable gridView = new UiScrollable(new UiSelector().className("android.widget.GridView"));
		gridView.flingToBeginning(100);
		sleep(1000);

		UiObject face = gridView.getChild(new UiSelector().instance(5));
		face.click();

		sleep(1000);

		UiObject viewFaceButton = new UiObject(new UiSelector().text("View Face"));
		viewFaceButton.click();

		sleep(1000);

		getUiDevice().pressBack();
	}

	// Horrible workaround to start an app... what the...
	private void startApp(String packageName, String activity) {
		try {
			Runtime.getRuntime().exec("am start -n " + packageName + "/" + activity);
			sleep(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
