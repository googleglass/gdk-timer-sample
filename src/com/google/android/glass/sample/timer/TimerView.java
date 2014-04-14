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

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * View used to draw a running timer.
 */
public class TimerView extends FrameLayout {

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface ChangeListener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    private static final int SOUND_PRIORITY = 1;
    private static final int MAX_STREAMS = 1;
    // Visible for testing.
    static final long DELAY_MILLIS = 1000;

    private final SoundPool mSoundPool;
    private final int mTimerFinishedSoundId;

    private final TextView mHoursView;
    private final TextView mMinutesView;
    private final TextView mSecondsView;
    private final TextView mTipView;

    private final int mWhiteColor;
    private final int mRedColor;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTextRunnable = new Runnable() {

        @Override
        public void run() {
            if (mRunning) {
                postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
                updateText();
            }
        }
    };

    private final Timer mTimer;
    private final Timer.TimerListener mTimerListener = new Timer.TimerListener() {

        @Override
        public void onStart() {
            mRunning = true;
            long delayMillis = Math.abs(mTimer.getRemainingTimeMillis()) % DELAY_MILLIS;
            if (delayMillis == 0) {
                delayMillis = DELAY_MILLIS;
            }
            postDelayed(mUpdateTextRunnable, delayMillis);
        }

        @Override
        public void onPause() {
            mRunning = false;
            removeCallbacks(mUpdateTextRunnable);
        }

        @Override
        public void onReset() {
            mTipView.setVisibility(View.INVISIBLE);
            updateText(mTimer.getRemainingTimeMillis(), mWhiteColor);
        }
    };

    private boolean mStarted;
    private boolean mRunning;
    private boolean mRedText;

    private ChangeListener mChangeListener;

    public TimerView(Context context) {
        this(context, null, 0);
    }

    public TimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int style) {
        this(context, attrs, style, new Timer());
    }

    public TimerView(Context context, AttributeSet attrs, int style, Timer timer) {
        super(context, attrs, style);

        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        mTimerFinishedSoundId = mSoundPool.load(context, R.raw.timer_finished, SOUND_PRIORITY);

        LayoutInflater.from(context).inflate(R.layout.card_timer, this);

        mHoursView = (TextView) findViewById(R.id.hours);
        mMinutesView = (TextView) findViewById(R.id.minutes);
        mSecondsView = (TextView) findViewById(R.id.seconds);
        mTipView = (TextView) findViewById(R.id.tip);
        mTipView.setText(context.getResources().getString(R.string.timer_finished));
        mTipView.setVisibility(View.INVISIBLE);

        mWhiteColor = context.getResources().getColor(R.color.white);
        mRedColor = context.getResources().getColor(R.color.red);

        mTimer = timer;
        mTimer.setListener(mTimerListener);
        updateText(mTimer.getRemainingTimeMillis(), mWhiteColor);
    }

    /** Returns the {@link Timer} model backing up the view. */
    public Timer getTimer() {
        return mTimer;
    }

    /**
     * Sets a {@link ChangeListener}.
     */
    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    /**
     * Returns the set {@link ChangeListener}.
     */
    public ChangeListener getListener() {
        return mChangeListener;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        mHandler.removeCallbacks(action);
        return true;
    }

    /**
     * Updates the text from the Timer's value, overridable for testing.
     */
    protected void updateText() {
        long remainingTimeMillis = mTimer.getRemainingTimeMillis();

        if (remainingTimeMillis > 0) {
            mRedText = false;
            // Round up: x001 to (x + 1)000 milliseconds should resolve to x seconds.
            remainingTimeMillis -= 1;
            remainingTimeMillis += TimeUnit.SECONDS.toMillis(1);
            mTipView.setVisibility(View.INVISIBLE);
        } else {
            mRedText = !mRedText;
            remainingTimeMillis = Math.abs(remainingTimeMillis);
            mTipView.setVisibility(View.VISIBLE);
        }

        if (mRedText) {
            // Sync the sound with the red text.
            playSound();
        }

        updateText(remainingTimeMillis, mRedText ? mRedColor : mWhiteColor);
    }

    /**
     * Updates the displayed text with the provided values, overridable for testing.
     */
    protected void updateText(long timeMillis, int textColor) {
        mHoursView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toHours(timeMillis)));
        mHoursView.setTextColor(textColor);
        timeMillis %= TimeUnit.HOURS.toMillis(1);
        mMinutesView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
        mMinutesView.setTextColor(textColor);
        timeMillis %= TimeUnit.MINUTES.toMillis(1);
        mSecondsView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(timeMillis)));
        mSecondsView.setTextColor(textColor);
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }

    /**
     * Plays the "timer finishd" sound once, overridable for testing.
     */
    protected void playSound() {
        mSoundPool.play(mTimerFinishedSoundId,
                        1 /* leftVolume */,
                        1 /* rightVolume */,
                        SOUND_PRIORITY,
                        0 /* loop */,
                        1 /* rate */);
    }
}
