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

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.google.android.glass.touchpad.GestureDetector.FingerListener;
import com.google.android.glass.touchpad.GestureDetector.ScrollListener;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Activity to set the timer.
 */
public class SetTimerActivity extends Activity implements BaseListener, ScrollListener,
        FingerListener {

    public static final String EXTRA_DURATION_MILLIS = "extra_duration";

    public static final String EXTRA_START_TIMER = "extra_start_timer";

    /** Maximum velocity when dragging. */
    private static final float MAX_DRAG_VELOCITY = 1;

    /** Deceleration constant for physics simulation. */
    private static final float DECELERATION_CONSTANT = 0.2f;

    /** Minimum velocity to start the inertial scrolling. */
    private static final float FLING_VELOCITY_CUTOFF = 1;

    /** Exagerate the time it takes to slow down the inertial scrolling. */
    private static final float TIME_LENGTHENING = 12;

    /** Max timer value of 24:59:00. */
    private static final long MAX_TIME_SECONDS = TimeUnit.HOURS.toSeconds(24)
            + TimeUnit.MINUTES.toSeconds(59);

    /** Animator for inertial scroll. */
    private ValueAnimator mInertialScrollAnimator;
    private float mReleaseVelocity;

    private float mTimeSeconds = 0;

    private TextView mHoursView;
    private TextView mMinutesView;
    private TextView mSecondsView;
    private TextView mTipView;

    private AudioManager mAudioManager;
    private GestureDetector mDetector;

    // Options menu flags.
    private boolean mShouldFinish;
    private boolean mOptionMenuOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimeSeconds =
                TimeUnit.MILLISECONDS.toSeconds(getIntent().getLongExtra(EXTRA_DURATION_MILLIS, 0));
        mDetector = new GestureDetector(this)
                .setBaseListener(this)
                .setFingerListener(this)
                .setScrollListener(this);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Initialize the various views.
        setContentView(R.layout.card_timer);
        mHoursView = (TextView) findViewById(R.id.hours);
        mMinutesView = (TextView) findViewById(R.id.minutes);
        mSecondsView = (TextView) findViewById(R.id.seconds);
        mTipView = (TextView) findViewById(R.id.tip);

        mSecondsView.setTextColor(getResources().getColor(R.color.gray));
        mSecondsView.setText("00");
        mTipView.setText(getResources().getString(R.string.swipe_to_set_timer));
        updateText();

        // Initialize the animator use for the intertial scrolling.
        mInertialScrollAnimator = new ValueAnimator();
        mInertialScrollAnimator.setInterpolator(new DecelerateInterpolator());
        mInertialScrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                setTimeSeconds(value);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mInertialScrollAnimator.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.set_timer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // The "set" menu item should only be visible when called from another Activity.
        menu.findItem(R.id.set).setVisible(getCallingActivity() != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mShouldFinish = true;
        switch (item.getItemId()) {
            case R.id.start:
                if (getCallingActivity() == null) {
                    startTimer();
                } else {
                    setTimer(true);
                }
                return true;
            case R.id.set:
                setTimer(false);
                return true;
            default:
                mShouldFinish = false;
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (mShouldFinish) {
            finish();
        }
        mOptionMenuOpen = false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mDetector.onMotionEvent(event);
    }

    @Override
    public void onFingerCountChanged(int previousCount, int currentCount) {
        boolean wentDown = currentCount > previousCount;

        if (currentCount == 0 && !wentDown && !mOptionMenuOpen) {
            // Only fling if the velocity is greater than the cutoff
            if (Math.abs(mReleaseVelocity) > FLING_VELOCITY_CUTOFF) {
                // Deceleration always in the opposite direction of the velocity
                final float deceleration = Math.signum(mReleaseVelocity) * -DECELERATION_CONSTANT;
                final float flingTime = -mReleaseVelocity / deceleration * TIME_LENGTHENING;
                float totalDelta = mReleaseVelocity * mReleaseVelocity / 2f / -deceleration;

                // Start the animation
                mInertialScrollAnimator.cancel();
                mInertialScrollAnimator.setFloatValues(
                        mTimeSeconds, confineTimeSeconds(mTimeSeconds + totalDelta));
                mInertialScrollAnimator.setDuration((long) flingTime);
                mInertialScrollAnimator.start();
            }
        } else {
            mInertialScrollAnimator.cancel();
        }
    }

    @Override
    public boolean onScroll(float displacement, float delta, float velocity) {
        mReleaseVelocity = velocity;
        if (!mOptionMenuOpen) {
            addTimeSeconds(delta * Math.min(Math.abs(velocity), MAX_DRAG_VELOCITY));
        }
        return true;
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                long timeMinutes = TimeUnit.SECONDS.toMinutes((long) mTimeSeconds);

                if (timeMinutes > 0) {
                    playSoundEffect(Sounds.TAP);
                    openOptionsMenu();
                    mOptionMenuOpen = true;
                } else {
                    playSoundEffect(Sounds.DISALLOWED);
                }
                return true;
            case SWIPE_DOWN:
                setResultInternal(RESULT_CANCELED, null);
                playSoundEffect(Sounds.DISMISSED);
                finish();
                return true;
            default:
                return false;
        }
    }

    /** Starts a new Timer. */
    private void startTimer() {
        Intent timerIntent = new Intent(this, TimerService.class);
        long timeMinutes = TimeUnit.SECONDS.toMinutes((long) mTimeSeconds);

        timerIntent.setAction(TimerService.ACTION_START);
        timerIntent.putExtra(
            TimerService.EXTRA_DURATION_MILLIS, TimeUnit.MINUTES.toMillis(timeMinutes));
        startService(timerIntent);
    }

    /** Returns the new timer value to the calling Activity. */
    private void setTimer(boolean startTimer) {
        Intent resultIntent = new Intent();
        long timeMinutes = TimeUnit.SECONDS.toMinutes((long) mTimeSeconds);

        resultIntent.putExtra(EXTRA_DURATION_MILLIS, TimeUnit.MINUTES.toMillis(timeMinutes));
        resultIntent.putExtra(EXTRA_START_TIMER, startTimer);
        setResultInternal(RESULT_OK, resultIntent);
    }

    /** Adds {@code delta} seconds to the Timer.*/
    private void addTimeSeconds(float delta) {
        setTimeSeconds(mTimeSeconds + delta);
    }

    /** Sets the Timer value. */
    private void setTimeSeconds(float timeSeconds) {
        float previousTimeSeconds = mTimeSeconds;

        mTimeSeconds = confineTimeSeconds(timeSeconds);
        if (TimeUnit.SECONDS.toMinutes((int) previousTimeSeconds)
            != TimeUnit.SECONDS.toMinutes((int) mTimeSeconds)) {
            playSoundEffect(Sounds.TAP);
            updateText();
        }
    }

    /** Updates the various {@link TextView} with the current Timer value. */
    private void updateText() {
        long hours = TimeUnit.SECONDS.toHours((int) mTimeSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes((int) mTimeSeconds % TimeUnit.HOURS.toSeconds(1));

        mHoursView.setText(String.format("%02d", hours));
        mMinutesView.setText(String.format("%02d", minutes));
        if (hours == 0 && minutes == 0) {
            mTipView.setVisibility(View.VISIBLE);
        } else {
            mTipView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Keeps the time between 0 and {@link MAX_TIME_SECONDS}.
     */
    private float confineTimeSeconds(float timeSeconds) {
        if (timeSeconds < 0) {
            timeSeconds = 0;
        } else if (timeSeconds > MAX_TIME_SECONDS) {
            timeSeconds = MAX_TIME_SECONDS;
        }
        return timeSeconds;
    }

    /**
     * Plays a sound effect, overridable for testing.
     */
    protected void playSoundEffect(int soundId) {
        mAudioManager.playSoundEffect(soundId);
    }

    /**
     * Sets the {@link Activity} result, overridable for testing.
     */
    protected void setResultInternal(int resultCode, Intent resultIntent) {
        setResult(resultCode, resultIntent);
    }

    /**
     * Forces any ongoing animation to immediately 'jump to the end', visible for testing.
     * This method must be called from same thread that performs the animation.
     */
    void forceEndAnimation() {
        if (mInertialScrollAnimator.isRunning()) {
            mInertialScrollAnimator.end();
        }
    }

    /** Returns the Timer current time, visible for testing. */
    float getTimeSeconds() {
        return mTimeSeconds;
    }

}
