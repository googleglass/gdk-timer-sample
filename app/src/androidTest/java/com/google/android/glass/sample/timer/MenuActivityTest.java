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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.Window;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link MenuActivity}.
 */
@SmallTest
public class MenuActivityTest extends ActivityUnitTestCase<MockMenuActivity> {

    private static final long INITIAL_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private Timer mTimer;
    private boolean mServiceStopped;
    private boolean mBindServiceCalled;
    private boolean mUnbindServiceCalled;
    private Intent mActivityIntent;

    private TimerService.TimerBinder mTimerBinder;

    public MenuActivityTest() {
        super(MockMenuActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTimer = new Timer(INITIAL_DURATION_MILLIS);
        mTimerBinder = new TimerService.TimerBinder(mTimer);

        // Set a mock context to simulate service binding.
        setActivityContext(new ContextWrapper(getInstrumentation().getTargetContext()) {

            @Override
            public boolean bindService(Intent service, ServiceConnection conn, int flags) {
                assertNotNull(service);
                assertNotNull(conn);
                assertEquals(TimerService.class.getName(), service.getComponent().getClassName());
                assertTrue(service.hasExtra(TimerService.EXTRA_TIMER_HASH_CODE));
                // Bind the mock service with the Activity.
                conn.onServiceConnected(null, mTimerBinder);
                mBindServiceCalled = true;
                return true;
            }

            @Override
            public void unbindService(ServiceConnection conn) {
                mUnbindServiceCalled = true;
            }

            @Override
            public ComponentName startService(Intent intent) {
                assertEquals(TimerService.class.getName(), intent.getComponent().getClassName());
                assertTrue(intent.hasExtra(TimerService.EXTRA_TIMER_HASH_CODE));
                assertEquals(TimerService.ACTION_STOP, intent.getAction());
                mServiceStopped = true;
                return intent.getComponent();
            }
        });

        mActivityIntent = new Intent(getInstrumentation().getTargetContext(), MenuActivity.class);
        mActivityIntent.putExtra(TimerService.EXTRA_TIMER_HASH_CODE, mTimer.hashCode());
    }

    public void testOnCreateBindsAndUnbindsService() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        assertTrue(mBindServiceCalled);
        assertTrue(mUnbindServiceCalled);
    }

    public void testOnActivityResult() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        Intent data = new Intent();
        long expectedDurationMillis = TimeUnit.SECONDS.toMillis(30);

        data.putExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, expectedDurationMillis);
        activity.onActivityResult(MenuActivity.SET_TIMER, Activity.RESULT_OK, data);
        assertEquals(expectedDurationMillis, mTimer.getDurationMillis());
        assertTrue(isFinishCalled());
    }

    public void testOnActivityResultStartTimer() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        Intent data = new Intent();
        long expectedDurationMillis = TimeUnit.SECONDS.toMillis(30);

        data.putExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, expectedDurationMillis);
        data.putExtra(SetTimerActivity.EXTRA_START_TIMER, true);
        activity.onActivityResult(MenuActivity.SET_TIMER, Activity.RESULT_OK, data);
        assertEquals(expectedDurationMillis, mTimer.getDurationMillis());
        assertTrue(mTimer.isStarted());
        assertTrue(isFinishCalled());
    }

    public void testOnActivityResultCanceled() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);

        activity.onActivityResult(MenuActivity.SET_TIMER, Activity.RESULT_CANCELED, null);
        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getDurationMillis());
        assertTrue(isFinishCalled());
    }

    public void testOptionsMenuChangeTimer() {
        assertOptionsMenu(R.id.change_timer, true);

        Intent startedIntent = getStartedActivityIntent();
        assertNotNull(startedIntent);
        assertEquals(SetTimerActivity.class.getName(), startedIntent.getComponent().getClassName());
        assertEquals(
                INITIAL_DURATION_MILLIS,
                startedIntent.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
        assertEquals(MenuActivity.SET_TIMER, getStartedActivityRequest());
        assertFalse(isFinishCalled());
    }

    public void testOptionsMenuStart() {
        assertOptionsMenu(R.id.start, true);
        assertTrue(isFinishCalled());
        assertTrue(mTimer.isStarted());
        assertTrue(mTimer.isRunning());
    }

    public void testOptionsMenuStartTimerRunning() {
        mTimer.start();
        assertOptionsMenu(R.id.start, false);
    }

    public void testOptionsMenuStartTimerPaused() {
        mTimer.start();
        mTimer.pause();
        assertOptionsMenu(R.id.start, false);
    }

    public void testOptionsMenuResume() {
        assertOptionsMenu(R.id.resume, false);
    }

    public void testOptionsMenuResumeTimerRunning() {
        mTimer.start();
        assertOptionsMenu(R.id.resume, false);
    }

    public void testOptionsMenuResumeTimerPaused() {
        mTimer.start();
        mTimer.pause();
        assertOptionsMenu(R.id.resume, true);
        assertTrue(isFinishCalled());
        assertTrue(mTimer.isStarted());
        assertTrue(mTimer.isRunning());
    }

    public void testOptionsMenuPause() {
        assertOptionsMenu(R.id.pause, false);
    }

    public void testOptionsMenuPauseTimerRunning() {
        mTimer.start();
        assertOptionsMenu(R.id.pause, true);
        assertTrue(isFinishCalled());
        assertTrue(mTimer.isStarted());
        assertFalse(mTimer.isRunning());
    }

    public void testOptionsMenuPauseTimerPaused() {
        mTimer.start();
        mTimer.pause();
        assertOptionsMenu(R.id.pause, false);
    }

    public void testOptionsMenuReset() {
        assertOptionsMenu(R.id.reset, false);
    }

    public void testOptionsMenuResetTimerRunning() {
        mTimer.start();
        assertOptionsMenu(R.id.reset, true);
        assertTrue(isFinishCalled());
        assertFalse(mTimer.isStarted());
        assertFalse(mTimer.isRunning());
        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getDurationMillis());
    }

    public void testOptionsMenuResetTimerPaused() {
        mTimer.start();
        mTimer.reset();
        assertOptionsMenu(R.id.reset, false);
        assertTrue(isFinishCalled());
        assertFalse(mTimer.isStarted());
        assertFalse(mTimer.isRunning());
        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getDurationMillis());
    }

    public void testOptionsMenuStop() {
        assertOptionsMenu(R.id.stop, true);
        assertTrue(isFinishCalled());
        assertTrue(mServiceStopped);
    }

    /** A convenience method to assert options menu behavior. */
    private void assertOptionsMenu(int menuId, boolean shouldBeHandled) {
        MenuActivity activity = startActivity(mActivityIntent, null, null);
        boolean handled = getInstrumentation().invokeMenuActionSync(activity, menuId, 0);

        assertEquals(shouldBeHandled, handled);
        activity.onPanelClosed(Window.FEATURE_OPTIONS_PANEL, null);
    }

}
