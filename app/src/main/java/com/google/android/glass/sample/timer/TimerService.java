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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class TimerService extends Service {

    /** {@link TimerService} Action: start an existing {@link Timer}. */
    public static final String ACTION_START = "com.google.android.glass.sample.timer.action.START";

    /** {@link TimerService} Action: stop an existing {@link Timer}. */
    public static final String ACTION_STOP = "com.google.android.glass.sample.timer.action.STOP";

    /** Timer duration in milliseconds. */
    public static final String EXTRA_DURATION_MILLIS = "duration_millis";

    /** String extra containing the hashcode of the {@link Timer} to process. */
    public static final String EXTRA_TIMER_HASH_CODE = "timer_hash_code";

    /**
     * Binder giving access to the underlying {@code Timer}.
     */
    public static class TimerBinder extends Binder {
        private Timer mTimer;

        public TimerBinder(Timer timer) {
            mTimer = timer;
        }

        public Timer getTimer() {
            return mTimer;
        }
    }

    private final TimerLiveCardManager mManager = new TimerLiveCardManager(this);

    @Override
    public IBinder onBind(Intent intent) {
        Timer timer = mManager.findTimer(intent.getIntExtra(EXTRA_TIMER_HASH_CODE, 0));

        if (timer != null) {
            return new TimerBinder(timer);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);

        // Return START_NOT_STICKY to prevent the system from restarting the service if it is killed
        // (e.g., due to an error). It doesn't make sense to restart automatically because the timer
        // state will have been lost.
        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (action.equals(ACTION_START)) {
            long durationMillis = intent.getLongExtra(EXTRA_DURATION_MILLIS, 0);

            mManager.startNewTimer(durationMillis);
        } else if (action.equals(ACTION_STOP)) {
            if (mManager.stopTimer(intent.getIntExtra(EXTRA_TIMER_HASH_CODE, 0))) {
                stopSelf();
            }
        }
    }
}
