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

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class TimerService extends Service {

    private static final String LIVE_CARD_TAG = "timer";

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

    private TimerDrawer mTimerDrawer;

    private LiveCard mLiveCard;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimerDrawer = new TimerDrawer(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TimerBinder(mTimerDrawer.getTimer());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mTimerDrawer);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
            mTimerDrawer.getTimer().reset();
        }
        super.onDestroy();
    }
}
