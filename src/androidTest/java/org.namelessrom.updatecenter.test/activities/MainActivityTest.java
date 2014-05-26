package org.namelessrom.updatecenter.test.activities;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import org.namelessrom.updatecenter.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @SmallTest
    public void testSomething() {
        MainActivity activity = getActivity();
        assertNotNull(activity);

        activity.setImmersive(true);
        assertEquals(activity.isImmersive(), true);
    }
}
