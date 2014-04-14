/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.glass.sample.timer;

import android.os.SystemClock;
import android.test.AndroidTestCase;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Timer}.
 */
public class TimerTest extends AndroidTestCase {

    private static final long INITIAL_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private Timer mTimer;
    private long mElapsedRealtime;

    private boolean mOnStartCalled;
    private boolean mOnPauseCalled;
    private boolean mOnResetCalled;

    /**
     * Mock {@link Timer.TimerListener} to verify that callbacks are properly called.
     */
    private class MockTimerListener implements Timer.TimerListener {

        @Override
        public void onStart() {
            mOnStartCalled = true;
        }

        @Override
        public void onPause() {
            mOnPauseCalled = true;
        }

        @Override
        public void onReset() {
            mOnResetCalled = true;
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTimer = new Timer(INITIAL_DURATION_MILLIS) {

            @Override
            protected long getElapsedRealtime() {
                return mElapsedRealtime;
            }
        };
        mTimer.setListener(new MockTimerListener());
        mElapsedRealtime = SystemClock.elapsedRealtime();
        mOnStartCalled = false;
        mOnPauseCalled = false;
        mOnResetCalled = false;
    }

    public void testSetDurationMillisCallsListener() {
        long expectedDurationMillis = TimeUnit.SECONDS.toMillis(5);

        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getDurationMillis());
        mTimer.setDurationMillis(expectedDurationMillis);
        assertTrue(mOnResetCalled);
        assertFalse(mOnStartCalled);
        assertFalse(mOnPauseCalled);
        assertEquals(expectedDurationMillis, mTimer.getDurationMillis());
    }

    public void testIsRunningNotStarted() {
        assertFalse(mTimer.isRunning());
        assertListenersNotCalled();
    }

    public void testIsRunningStartedAndRunning() {
        mTimer.start();
        assertTrue(mTimer.isRunning());
    }

    public void testIsRunningStartedAndPaused() {
        mTimer.start();
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(1);
        mTimer.pause();
        assertFalse(mTimer.isRunning());
    }

    public void testIsStartedNotStarted() {
        assertFalse(mTimer.isStarted());
        assertListenersNotCalled();
    }

    public void testIsStartedStartedAndRunning() {
        mTimer.start();
        assertTrue(mTimer.isStarted());
    }

    public void testIsStartedStartedAndPaused() {
        mTimer.start();
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(1);
        mTimer.pause();
        assertTrue(mTimer.isStarted());
    }

    public void testGetRemainingTimeMillisNotStarted() {
        assertEquals(INITIAL_DURATION_MILLIS, mTimer.getRemainingTimeMillis());
        assertListenersNotCalled();
    }

    public void testGetRemainingTimeMillisStartedAndRunning() {
        long elapsedTime = TimeUnit.SECONDS.toMillis(30);

        mTimer.start();
        mElapsedRealtime += elapsedTime;
        assertEquals(INITIAL_DURATION_MILLIS - elapsedTime, mTimer.getRemainingTimeMillis());
    }

    public void testGetRemainingTimeMillisStartedAndRunningNegative() {
        long elapsedTime = INITIAL_DURATION_MILLIS + TimeUnit.MINUTES.toMillis(30);

        mTimer.start();
        mElapsedRealtime += elapsedTime;
        assertEquals(INITIAL_DURATION_MILLIS - elapsedTime, mTimer.getRemainingTimeMillis());
    }

    public void testGetRemainingTimeMillisStartedAndPaused() {
        long elapsedTime = TimeUnit.SECONDS.toMillis(30);

        mTimer.start();
        mElapsedRealtime += elapsedTime;
        mTimer.pause();
        mElapsedRealtime += elapsedTime;
        assertEquals(INITIAL_DURATION_MILLIS - elapsedTime, mTimer.getRemainingTimeMillis());
    }

    public void testStartCallsListener() {
        mTimer.start();
        assertTrue(mTimer.isStarted());
        assertTrue(mTimer.isRunning());
        assertTrue(mOnStartCalled);
        assertFalse(mOnResetCalled);
        assertFalse(mOnPauseCalled);
    }

    public void testPauseCallsListener() {
        mTimer.start();
        mOnStartCalled = false;
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(5);
        mTimer.pause();
        assertTrue(mTimer.isStarted());
        assertFalse(mTimer.isRunning());
        assertTrue(mOnPauseCalled);
        assertFalse(mOnStartCalled);
        assertFalse(mOnResetCalled);
    }

    public void testResetCallsListener() {
        mTimer.start();
        mOnStartCalled = false;
        mElapsedRealtime += TimeUnit.SECONDS.toMillis(5);
        assertTrue(mTimer.isStarted());
        assertTrue(mTimer.isRunning());
        mTimer.reset();
        assertFalse(mTimer.isStarted());
        assertFalse(mTimer.isRunning());
        assertTrue(mOnResetCalled);
        assertTrue(mOnPauseCalled);
        assertFalse(mOnStartCalled);
    }

    private void assertListenersNotCalled() {
        assertFalse(mOnStartCalled);
        assertFalse(mOnPauseCalled);
        assertFalse(mOnResetCalled);
    }
}
