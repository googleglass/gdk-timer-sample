/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.timer;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.media.Sounds;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link SelectValueActivity}.
 */
public class SelectValueActivityTest extends ActivityUnitTestCase<MockSelectValueActivity> {

    private static final int COUNT = 60;
    private static final int INITIAL_VALUE = 15;

    private Intent mActivityIntent;

    public SelectValueActivityTest() {
        super(MockSelectValueActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivityIntent =
                new Intent(getInstrumentation().getTargetContext(), SelectValueActivity.class);
        mActivityIntent.putExtra(SelectValueActivity.EXTRA_COUNT, COUNT);
        mActivityIntent.putExtra(SelectValueActivity.EXTRA_INITIAL_VALUE, INITIAL_VALUE);
    }

    @UiThreadTest
    public void testOnCreate() {
        MockSelectValueActivity activity = startActivity(mActivityIntent, null, null);
        SelectValueScrollAdapter adapter =
                (SelectValueScrollAdapter) activity.getView().getAdapter();

        assertEquals(COUNT, adapter.getCount());
    }

    @UiThreadTest
    public void testOnResumeSetsSelection() throws Throwable {
        MockSelectValueActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        assertEquals(INITIAL_VALUE, activity.getView().getSelectedItemPosition());
    }

    @UiThreadTest
    public void testOnResumeNoInitialValue() throws Throwable {
        mActivityIntent.removeExtra(SelectValueActivity.EXTRA_INITIAL_VALUE);
        MockSelectValueActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        assertEquals(0, activity.getView().getSelectedItemPosition());
    }

    @UiThreadTest
    public void testOnGestureTapSupported() {
        MockSelectValueActivity activity = startActivity(mActivityIntent, null, null);
        int expectedSelectedValue = 35;

        activity.onResume();
        activity.getView().setSelection(expectedSelectedValue);

        assertTrue(activity.onGesture(Gesture.TAP));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.TAP, activity.mPlayedSoundEffects.get(0).intValue());

        assertTrue(isFinishCalled());
        assertEquals(Activity.RESULT_OK, activity.mResultCode);
        assertNotNull(activity.mResultIntent);
        assertEquals(
                expectedSelectedValue,
                activity.mResultIntent.getIntExtra(SelectValueActivity.EXTRA_SELECTED_VALUE, 0));
    }

    @UiThreadTest
    public void testOnGestureSwipeDownSupported() {
        MockSelectValueActivity activity = startActivity(mActivityIntent, null, null);

        assertTrue(activity.onGesture(Gesture.SWIPE_DOWN));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.DISMISSED, activity.mPlayedSoundEffects.get(0).intValue());

        assertTrue(isFinishCalled());
        assertEquals(Activity.RESULT_CANCELED, activity.mResultCode);
        assertNull(activity.mResultIntent);
    }

}
