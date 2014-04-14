/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.test.AndroidTestCase;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link TimerDrawer}.
 */
public class TimerViewTest extends AndroidTestCase {

    /** Initial duration of 02h30m00s. */
    private static final long INITIAL_DURATION_MILLIS = TimeUnit.HOURS.toMillis(2)
            + TimeUnit.MINUTES.toMillis(30);
    private static final String INITIAL_HOURS_COMPONENT = "02";
    private static final String INITIAL_MINUTES_COMPONENT = "30";
    private static final String INITIAL_SECONDS_COMPONENT = "00";

    private long mRemainingTimeMillis;

    private TimerView mView;
    private TextView mHoursView;
    private TextView mMinutesView;
    private TextView mSecondsView;
    private TextView mTipView;

    // Test flags.
    private boolean mMockUpdateText;
    private boolean mOnChangeCalled;
    private boolean mPlaySoundCalled;
    private boolean mUpdateTextCalled;
    private int mTextColor;
    private long mPostedCallbackDelayMillis;
    private long mTimeMillis;
    private Runnable mPostedCallback;
    private Runnable mRemovedCallback;

    /** Extension of {@link Timer} for easier testing. */
    private final Timer mTimer = new Timer() {
        @Override
        public long getRemainingTimeMillis() {
            return mRemainingTimeMillis;
        }
    };

    /** Extension of {@link TimerView} for easier testing. */
    private class MockTimerView extends TimerView {

        public MockTimerView(Context context, Timer timer) {
            super(context, null, 0, timer);
        }

        @Override
        public boolean postDelayed(Runnable action, long delayMillis) {
            mPostedCallback = action;
            mPostedCallbackDelayMillis = delayMillis;
            return true;
        }

        @Override
        public boolean removeCallbacks(Runnable action) {
            mRemovedCallback = action;
            return true;
        }

        @Override
        protected void updateText(long timeMillis, int textColor) {
            mUpdateTextCalled = true;
            mTimeMillis = timeMillis;
            mTextColor = textColor;
            if (!mMockUpdateText) {
                super.updateText(timeMillis, textColor);
            }
        }

        @Override
        protected void playSound() {
            mPlaySoundCalled = true;
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Reset the test flags.
        mMockUpdateText = true;
        mUpdateTextCalled = false;
        mPlaySoundCalled = false;
        mTimeMillis = 0;
        mTextColor = 0;
        mPostedCallback = null;
        mRemovedCallback = null;
        mPostedCallbackDelayMillis = 0;
        mOnChangeCalled = false;

        // Initialize test objects.
        mRemainingTimeMillis = INITIAL_DURATION_MILLIS;
        mView = new MockTimerView(getContext(), mTimer);
        mView.setListener(new TimerView.ChangeListener() {

            @Override
            public void onChange() {
                mOnChangeCalled = true;
            }
        });

        // Retrieve the underlying views.
        mHoursView = (TextView) mView.findViewById(R.id.hours);
        mMinutesView = (TextView) mView.findViewById(R.id.minutes);
        mSecondsView = (TextView) mView.findViewById(R.id.seconds);
        mTipView = (TextView) mView.findViewById(R.id.tip);
    }

    public void testConstructorProperlyInitializeViews() {
        assertEquals(
                getContext().getResources().getString(R.string.timer_finished),
                mTipView.getText());
        assertEquals(View.INVISIBLE, mTipView.getVisibility());
        assertTrue(mUpdateTextCalled);
    }

    public void testListenerOnStart() {
        mTimer.start();
        assertNotNull(mPostedCallback);
        assertEquals(TimerView.DELAY_MILLIS, mPostedCallbackDelayMillis);
    }

    public void testListenerOnStartWithNoneDefaultDelay() {
        // Remove 700ms from the remaining time.
        mRemainingTimeMillis -= 700;
        mTimer.start();
        assertNotNull(mPostedCallback);
        assertEquals(300, mPostedCallbackDelayMillis);
    }

    public void testListenerOnPauseRemovesCallback() {
        mTimer.start();
        mTimer.pause();
        assertNotNull(mPostedCallback);
        assertNotNull(mRemovedCallback);
        assertEquals(mPostedCallback, mRemovedCallback);
    }

    public void testListenerOnReset() {
        mTimer.reset();
        assertTrue(mUpdateTextCalled);
        assertEquals(INITIAL_DURATION_MILLIS, mTimeMillis);
        assertEquals(getContext().getResources().getColor(R.color.white), mTextColor);
        assertEquals(View.INVISIBLE, mTipView.getVisibility());
    }

    public void testUpdateTextTimerRunning() {
        long expectedDurationMillis = INITIAL_DURATION_MILLIS - 1 + TimeUnit.SECONDS.toMillis(1);
        mView.updateText();
        assertEquals(expectedDurationMillis, mTimeMillis);
        assertEquals(getContext().getResources().getColor(R.color.white), mTextColor);
        assertEquals(View.INVISIBLE, mTipView.getVisibility());
    }

    public void testUpdateTextTimerFinished() {
        mRemainingTimeMillis = TimeUnit.SECONDS.toMillis(-2);
        mView.updateText();
        assertEquals(-mRemainingTimeMillis, mTimeMillis);
        assertEquals(getContext().getResources().getColor(R.color.red), mTextColor);
        assertEquals(View.VISIBLE, mTipView.getVisibility());
    }

    public void testUpdateTextTimerFinishedChangesTextColor() {
        int colorWhite = getContext().getResources().getColor(R.color.white);
        int colorRed = getContext().getResources().getColor(R.color.red);

        mRemainingTimeMillis = TimeUnit.SECONDS.toMillis(-2);
        mView.updateText();
        assertEquals(colorRed, mTextColor);
        mView.updateText();
        assertEquals(colorWhite, mTextColor);
        mView.updateText();
        assertEquals(colorRed, mTextColor);
    }

    public void testUpdateTextWithArgs() {
        int colorRed = getContext().getResources().getColor(R.color.red);

        mMockUpdateText = false;
        mView.updateText(INITIAL_DURATION_MILLIS, colorRed);
        assertEquals(INITIAL_HOURS_COMPONENT, mHoursView.getText());
        assertEquals(INITIAL_MINUTES_COMPONENT, mMinutesView.getText());
        assertEquals(INITIAL_SECONDS_COMPONENT, mSecondsView.getText());
        assertEquals(colorRed, mHoursView.getTextColors().getDefaultColor());
        assertEquals(colorRed, mMinutesView.getTextColors().getDefaultColor());
        assertEquals(colorRed, mSecondsView.getTextColors().getDefaultColor());
        assertTrue(mOnChangeCalled);
    }
}
