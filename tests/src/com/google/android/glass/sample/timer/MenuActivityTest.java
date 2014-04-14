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
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.test.ActivityUnitTestCase;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link MenuActivity}.
 */
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
            public boolean stopService(Intent intent) {
                assertEquals(TimerService.class.getName(), intent.getComponent().getClassName());
                mServiceStopped = true;
                return true;
            }
        });

        mActivityIntent = new Intent(getInstrumentation().getTargetContext(), MenuActivity.class);
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

    public void testOnActivityResultCanceled() {
        MenuActivity activity = startActivity(mActivityIntent, null, null);

        activity.onActivityResult(MenuActivity.SET_TIMER, Activity.RESULT_CANCELED, null);
        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getDurationMillis());
        assertTrue(isFinishCalled());
    }

    public void testOptionsMenuEnabledNoTimerSet() {
        // Reset Timer.
        mTimer.setDurationMillis(0);

        MenuActivity activity = startActivity(mActivityIntent, null, null);
        Instrumentation inst = getInstrumentation();

        // Those options menu should be disabled.
        assertFalse(inst.invokeMenuActionSync(activity, R.id.change_timer, 0));
        assertFalse(inst.invokeMenuActionSync(activity, R.id.start, 0));
        assertFalse(inst.invokeMenuActionSync(activity, R.id.resume, 0));
        assertFalse(inst.invokeMenuActionSync(activity, R.id.pause, 0));
        assertFalse(inst.invokeMenuActionSync(activity, R.id.reset, 0));

        // Those options menu should be enabled.
        assertTrue(inst.invokeMenuActionSync(activity, R.id.set_timer, 0));
        assertTrue(inst.invokeMenuActionSync(activity, R.id.stop, 0));
    }

    public void testOptionsMenuSetTimerNoTimeSet() {
        // Reset Timer.
        mTimer.setDurationMillis(0);
        assertOptionsMenu(R.id.set_timer, true);

        Intent startedIntent = getStartedActivityIntent();
        assertNotNull(startedIntent);
        assertEquals(SetTimerActivity.class.getName(), startedIntent.getComponent().getClassName());
        assertEquals(0, startedIntent.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
        assertEquals(MenuActivity.SET_TIMER, getStartedActivityRequest());


        assertFalse(isFinishCalled());
    }

    public void testOptionsMenuSetTimerTimeSet() {
        assertOptionsMenu(R.id.set_timer, false);
    }

    public void testOptionsMenuChangeTimerNoTimeSet() {
        // Reset Timer.
        mTimer.setDurationMillis(0);
        assertOptionsMenu(R.id.change_timer, false);
    }

    public void testOptionsMenuChangeTimerTimeSet() {
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
        activity.onOptionsMenuClosed(null);
    }

}
