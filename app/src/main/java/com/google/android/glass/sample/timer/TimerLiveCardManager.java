/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.HashMap;

/**
 * Class to manage {@link LiveCard} for the {@link TimerService}.
 */
public class TimerLiveCardManager {

    private final Context mContext;
    private final HashMap<Timer, LiveCard> mTimers = new HashMap<Timer, LiveCard>();

    public TimerLiveCardManager(Context context) {
        mContext = context;
    }

    /** Starts a new {@link Timer}/{@link LiveCard} combination with the provided duration. */
    public Timer startNewTimer(long durationMillis) {
        Timer timer = new Timer(durationMillis);
        TimerDrawer drawer = new TimerDrawer(mContext, timer);
        LiveCard liveCard = new LiveCard(mContext, timer.toString());

        liveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(drawer);
        liveCard.setVoiceActionEnabled(true);

        Intent menuIntent = new Intent(mContext, MenuActivity.class);
        menuIntent.setData(Uri.parse("glass.timer:" + timer.hashCode()));
        menuIntent.putExtra(TimerService.EXTRA_TIMER_HASH_CODE, timer.hashCode());
        liveCard.setAction(PendingIntent.getActivity(mContext, 0, menuIntent, 0));
        if (mContext instanceof Service) {
            liveCard.attach((Service) mContext);
        }
        liveCard.publish(PublishMode.REVEAL);
        timer.start();

        mTimers.put(timer, liveCard);
        return timer;
    }

    /**
     * Stops the {@link Timer}/{@link LiveCard} and returns whether or not the manager is empty of
     * {@link Timer}.
     */
    public boolean stopTimer(int timerHashCode) {
        Timer timer = findTimer(timerHashCode);

        if (timer != null) {
            LiveCard liveCard = mTimers.get(timer);

            liveCard.unpublish();
            timer.reset();
            mTimers.remove(timer);
        }
        return mTimers.isEmpty();
    }

    /** Returns the {@link LiveCard} associated with this {@link Timer}. */
    public LiveCard getLiveCard(Timer timer) {
        return mTimers.get(timer);
    }

    /**
     * Returns the {@link Timer} identified by the provided {@code timerHashCode}.
     */
    public Timer findTimer(int timerHashCode) {
        for (Timer timer : mTimers.keySet()) {
            if (timer.hashCode() == timerHashCode) {
                return timer;
            }
        }
        return null;
    }
}
