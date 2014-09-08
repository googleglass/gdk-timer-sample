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

import com.google.android.glass.timeline.LiveCard;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link TimerLiveCardManager}.
 */
@SmallTest
public class TimerLiveCardManagerTest extends AndroidTestCase {

    /** Initial duration of 02h30m00s. */
    private static final long INITIAL_DURATION_MILLIS = TimeUnit.HOURS.toMillis(2)
            + TimeUnit.MINUTES.toMillis(30);

    private TimerLiveCardManager mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mManager = new TimerLiveCardManager(getContext());
    }

    public void testStartNewTimer() {
        Timer timer = mManager.startNewTimer(INITIAL_DURATION_MILLIS);
        LiveCard liveCard = mManager.getLiveCard(timer);

        assertNotNull(timer);
        assertNotNull(liveCard);
        assertTrue(timer.isStarted());
        assertEquals(INITIAL_DURATION_MILLIS, timer.getDurationMillis());
        assertTrue(liveCard.isPublished());
    }

    public void testStopTimer() {
        Timer timer1 = mManager.startNewTimer(INITIAL_DURATION_MILLIS);
        Timer timer2 = mManager.startNewTimer(INITIAL_DURATION_MILLIS);

        assertFalse(mManager.stopTimer(timer1.hashCode()));
        assertTrue(mManager.stopTimer(timer2.hashCode()));
        assertTrue(mManager.stopTimer(0));
    }
}
