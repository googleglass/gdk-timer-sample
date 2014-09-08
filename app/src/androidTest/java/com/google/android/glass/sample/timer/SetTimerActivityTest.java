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
import android.content.ComponentName;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link SetTimerActivity}.
 */
@SmallTest
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

        assertEquals(
                (float) TimeUnit.MILLISECONDS.toSeconds(INITIAL_DURATION_MILLIS),
                activity.getTimeSeconds());
    }

    @UiThreadTest
    public void testOnCreateNoDuration() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        assertEquals(0f, activity.getTimeSeconds());
    }

    @UiThreadTest
    public void testOnGestureTapSupported() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        assertTrue(activity.onGesture(Gesture.TAP));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.TAP, activity.mPlayedSoundEffects.get(0).intValue());
        assertTrue(activity.mOptionsMenuOpen);
    }

    @UiThreadTest
    public void testOnGestureTapDisallowed() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        assertTrue(activity.onGesture(Gesture.TAP));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.DISALLOWED, activity.mPlayedSoundEffects.get(0).intValue());
        assertFalse(activity.mOptionsMenuOpen);
    }

    @UiThreadTest
    public void testOnGestureSwipeDownSupported() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        assertTrue(activity.onGesture(Gesture.SWIPE_DOWN));
        assertEquals(1, activity.mPlayedSoundEffects.size());
        assertEquals(Sounds.DISMISSED, activity.mPlayedSoundEffects.get(0).intValue());

        assertTrue(isFinishCalled());
        assertEquals(Activity.RESULT_CANCELED, activity.mResultCode);
        assertNull(activity.mResultIntent);
    }

    public void testOnOptionsItemSelectedStart() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        assertTrue(getInstrumentation().invokeMenuActionSync(activity, R.id.start, 0));
        assertNotNull(activity.mServiceIntent);
        assertEquals(
                TimerService.class.getName(),
                activity.mServiceIntent.getComponent().getClassName());
        assertEquals(
                INITIAL_DURATION_MILLIS,
                activity.mServiceIntent.getLongExtra(TimerService.EXTRA_DURATION_MILLIS, 0));
    }

    public void testOnOptionsItemSelectedStartWithCallingActivity() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        activity.mCallingActivity = new ComponentName(activity, MenuActivity.class);

        assertTrue(getInstrumentation().invokeMenuActionSync(activity, R.id.start, 0));
        assertNull(activity.mServiceIntent);
        assertEquals(Activity.RESULT_OK, activity.mResultCode);
        assertNotNull(activity.mResultIntent);
        assertEquals(
                INITIAL_DURATION_MILLIS,
                activity.mResultIntent.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
        assertTrue(
                activity.mResultIntent.getBooleanExtra(SetTimerActivity.EXTRA_START_TIMER, true));
    }

    public void testOnOptionsItemSelectedSet() {
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        activity.mCallingActivity = new ComponentName(activity, MenuActivity.class);

        assertTrue(getInstrumentation().invokeMenuActionSync(activity, R.id.set, 0));
        assertEquals(Activity.RESULT_OK, activity.mResultCode);
        assertNotNull(activity.mResultIntent);
        assertEquals(
                INITIAL_DURATION_MILLIS,
                activity.mResultIntent.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
        assertFalse(
                activity.mResultIntent.getBooleanExtra(SetTimerActivity.EXTRA_START_TIMER, true));
    }

    public void testOnScroll() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);
        float seconds = 60;

        activity.onScroll(seconds, seconds, 1);
        assertEquals(seconds, activity.getTimeSeconds());
    }

    public void testOnFingerCountChangedNoFling() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        activity.onScroll(0, 0, 0.2f);
        assertEquals(0f, activity.getTimeSeconds());
        activity.onFingerCountChanged(1, 0);
        activity.forceEndAnimation();
        assertEquals(0f, activity.getTimeSeconds());
    }

    public void testOnFingerCountChangedFling() {
        mActivityIntent.removeExtra(SetTimerActivity.EXTRA_DURATION_MILLIS);
        MockSetTimerActivity activity = startActivity(mActivityIntent, null, null);

        activity.onScroll(0, 0, 20);
        assertEquals(0f, activity.getTimeSeconds());
        activity.onFingerCountChanged(1, 0);
        activity.forceEndAnimation();
        assertEquals(1000f, activity.getTimeSeconds());
    }

}
