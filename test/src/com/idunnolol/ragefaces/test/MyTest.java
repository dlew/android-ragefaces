package com.idunnolol.ragefaces.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.RageFacePickerActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MyTest {

    @Test
    public void shouldHaveHappySmiles() throws Exception {
        String hello = new RageFacePickerActivity().getResources().getString(R.string.app_name);
        assertThat(hello, equalTo("Rage Faces"));
    }

}
