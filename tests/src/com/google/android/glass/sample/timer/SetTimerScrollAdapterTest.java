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
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link SetTimerScrollAdapter}.
 */
public class SetTimerScrollAdapterTest extends AndroidTestCase {

    private SetTimerScrollAdapter mAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAdapter = new SetTimerScrollAdapter(getContext());
    }

    public void testSetGetDurationMillis() {
        long expectedDuration = TimeUnit.MINUTES.toMillis(30);

        assertEquals(0, mAdapter.getDurationMillis());
        mAdapter.setDurationMillis(expectedDuration);
        assertEquals(expectedDuration, mAdapter.getDurationMillis());
    }

    public void testSetTimeComponent() {
        long initialExpectedDuration = 0;
        long firstExpectedDuration = TimeUnit.SECONDS.toMillis(30);
        long secondExpectedDuration = TimeUnit.MINUTES.toMillis(2)
                + TimeUnit.SECONDS.toMillis(30);
        long thirdExpectedDuration = TimeUnit.HOURS.toMillis(5)
                + TimeUnit.MINUTES.toMillis(2)
                + TimeUnit.SECONDS.toMillis(30);

        assertEquals(initialExpectedDuration, mAdapter.getDurationMillis());

        // Set seconds component to 30, total time is 00h00m30s.
        mAdapter.setTimeComponent(SetTimerScrollAdapter.TimeComponents.SECONDS, 30);
        assertEquals(firstExpectedDuration, mAdapter.getDurationMillis());

        // Set minutes component to 2, total time is 00h02m30s.
        mAdapter.setTimeComponent(SetTimerScrollAdapter.TimeComponents.MINUTES, 2);
        assertEquals(secondExpectedDuration, mAdapter.getDurationMillis());

        // Set hours component to 5, total time is 05h02m30s;
        mAdapter.setTimeComponent(SetTimerScrollAdapter.TimeComponents.HOURS, 5);
        assertEquals(thirdExpectedDuration, mAdapter.getDurationMillis());
    }

    public void testGetTimeComponent() {
        long expectedHours = 5;
        long expectedMinutes = 2;
        long expectedSeconds = 30;
        long duration = TimeUnit.HOURS.toMillis(expectedHours)
                + TimeUnit.MINUTES.toMillis(expectedMinutes)
                + TimeUnit.SECONDS.toMillis(expectedSeconds);

        assertEquals(0, mAdapter.getDurationMillis());
        assertEquals(0, mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.HOURS));
        assertEquals(0, mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.MINUTES));
        assertEquals(0, mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.SECONDS));

        mAdapter.setDurationMillis(duration);
        assertEquals(
                expectedHours,
                mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.HOURS));
        assertEquals(
                expectedMinutes,
                mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.MINUTES));
        assertEquals(
                expectedSeconds,
                mAdapter.getTimeComponent(SetTimerScrollAdapter.TimeComponents.SECONDS));
    }

    public void testGetCount() {
        assertEquals(3, mAdapter.getCount());
    }

    public void testGetItem() {
        assertEquals(SetTimerScrollAdapter.TimeComponents.HOURS, mAdapter.getItem(0));
        assertEquals(SetTimerScrollAdapter.TimeComponents.MINUTES, mAdapter.getItem(1));
        assertEquals(SetTimerScrollAdapter.TimeComponents.SECONDS, mAdapter.getItem(2));
        assertNull(mAdapter.getItem(4));
    }

    public void testGetViewHours() {
        assertGetView(0, R.string.hours);
    }

    public void testGetViewMinutes() {
        assertGetView(1, R.string.minutes);
    }

    public void testGetViewSeconds() {
        assertGetView(2, R.string.seconds);
    }

    public void testGetPosition() {
        assertEquals(0, mAdapter.getPosition(SetTimerScrollAdapter.TimeComponents.HOURS));
        assertEquals(1, mAdapter.getPosition(SetTimerScrollAdapter.TimeComponents.MINUTES));
        assertEquals(2, mAdapter.getPosition(SetTimerScrollAdapter.TimeComponents.SECONDS));
        assertEquals(AdapterView.INVALID_POSITION, mAdapter.getPosition(this));
    }

    private void assertGetView(int position, int expectedLabel) {
        long expectedHours = 5;
        long expectedMinutes = 2;
        long expectedSeconds = 30;
        long duration = TimeUnit.HOURS.toMillis(expectedHours)
                + TimeUnit.MINUTES.toMillis(expectedMinutes)
                + TimeUnit.SECONDS.toMillis(expectedSeconds);
        int colorGray = getContext().getResources().getColor(R.color.gray);
        int colorWhite = getContext().getResources().getColor(R.color.white);
        String label = getContext().getResources().getString(expectedLabel);

        mAdapter.setDurationMillis(duration);

        View view = mAdapter.getView(position, null, null);
        assertNotNull(view);
        TextView[] views = new TextView[] {
            (TextView) view.findViewById(R.id.hours),
            (TextView) view.findViewById(R.id.minutes),
            (TextView) view.findViewById(R.id.seconds)
        };
        TextView tipView = (TextView) view.findViewById(R.id.tip);

        assertEquals("05", views[0].getText());
        assertEquals("02", views[1].getText());
        assertEquals("30", views[2].getText());

        for (int i = 0; i < 3; ++i) {
            int textColor = views[i].getTextColors().getDefaultColor();
            assertEquals(i == position ? colorWhite : colorGray, textColor);
        }

        assertNotNull(tipView);
        assertEquals(label, tipView.getText());
    }
}
