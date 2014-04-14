/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * Unit tests for {@link SetTimerActivity}.
 */
public class SetTimerActivityTest extends ActivityUnitTestCase<MockSetTimerActivity> {

    private static final long INITIAL_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private Intent mActivityIntent;

    public SetTimerActivityTest() {
        super(MockSetTimerActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivityIntent =
                new Intent(getInstrumentation().getTargetContext(), SetTimerActivity.class);
        mActivityIntent.putExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, INITIAL_DURATION_MILLIS);
    }

    @UiThreadTest
    public void testOnCreate() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        SetTimerScrollAdapter adapter = (SetTimerScrollAdapter) activity.getView().getAdapter();

        assertEquals(INITIAL_DURATION_MILLIS, adapter.getDurationMillis());
    }

    @UiThreadTest
    public void testOnCreateNoDuration() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        SetTimerScrollAdapter adapter = (SetTimerScrollAdapter) activity.getView().getAdapter();

        assertEquals(0, adapter.getDurationMillis());
    }

    @UiThreadTest
    public void testOnGestureTapSupported() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        activity.getView().setSelection(0);

        assertTrue(activity.onGesture(Gesture.TAP));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.TAP, activity.mPlayedSoundEffects.get(0).intValue());

        Intent startedIntent = getStartedActivityIntent();

        assertNotNull(startedIntent);
        assertEquals(
                SelectValueActivity.class.getName(), startedIntent.getComponent().getClassName());
        assertEquals(
                SetTimerScrollAdapter.TimeComponents.HOURS.getMaxValue(),
                startedIntent.getIntExtra(SelectValueActivity.EXTRA_COUNT, 0));
        assertEquals(
                0, startedIntent.getIntExtra(SelectValueActivity.EXTRA_INITIAL_VALUE, 0));
        assertEquals(SetTimerActivity.SELECT_VALUE, getStartedActivityRequest());
        assertFalse(isFinishCalled());
    }

    @UiThreadTest
    public void testOnGestureTapSupportedSelectValueForMinutes() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        activity.getView().setSelection(1);

        assertTrue(activity.onGesture(Gesture.TAP));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.TAP, activity.mPlayedSoundEffects.get(0).intValue());

        Intent startedIntent = getStartedActivityIntent();

        assertNotNull(startedIntent);
        assertEquals(
                SelectValueActivity.class.getName(), startedIntent.getComponent().getClassName());
        assertEquals(
                SetTimerScrollAdapter.TimeComponents.MINUTES.getMaxValue(),
                startedIntent.getIntExtra(SelectValueActivity.EXTRA_COUNT, 0));
        assertEquals(
                5, startedIntent.getIntExtra(SelectValueActivity.EXTRA_INITIAL_VALUE, 0));
        assertEquals(SetTimerActivity.SELECT_VALUE, getStartedActivityRequest());
        assertFalse(isFinishCalled());
    }

    @UiThreadTest
    public void testOnGestureSwipeDownSupported() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        SetTimerScrollAdapter adapter = (SetTimerScrollAdapter) activity.getView().getAdapter();
        long expectedDuration = TimeUnit.SECONDS.toMillis(45);

        adapter.setDurationMillis(expectedDuration);
        assertTrue(activity.onGesture(Gesture.SWIPE_DOWN));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.DISMISSED, activity.mPlayedSoundEffects.get(0).intValue());

        assertTrue(isFinishCalled());
        assertEquals(Activity.RESULT_OK, activity.mResultCode);
        assertNotNull(activity.mResultIntent);
        assertEquals(
                expectedDuration,
                activity.mResultIntent.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
    }

    @UiThreadTest
    public void testOnActivityResult() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        SetTimerScrollAdapter adapter = (SetTimerScrollAdapter) activity.getView().getAdapter();
        Intent data = new Intent();
        int hoursValue = 3;
        long expectedDurationMillis = TimeUnit.HOURS.toMillis(hoursValue) + INITIAL_DURATION_MILLIS;

        data.putExtra(SelectValueActivity.EXTRA_SELECTED_VALUE, hoursValue);
        activity.onActivityResult(SetTimerActivity.SELECT_VALUE, Activity.RESULT_OK, data);
        assertEquals(expectedDurationMillis, adapter.getDurationMillis());
        assertEquals(
                hoursValue, adapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.HOURS));
    }

    @UiThreadTest
    public void testOnActivityResultCanceled() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        SetTimerScrollAdapter adapter = (SetTimerScrollAdapter) activity.getView().getAdapter();

        activity.onActivityResult(SetTimerActivity.SELECT_VALUE, Activity.RESULT_CANCELED, null);
        assertEquals(INITIAL_DURATION_MILLIS, adapter.getDurationMillis());
    }

}
