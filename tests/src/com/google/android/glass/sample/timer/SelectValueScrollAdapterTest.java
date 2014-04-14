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
 * Unit tests for {@link SelectValueScrollAdapter}.
 */
public class SelectValueScrollAdapterTest extends AndroidTestCase {

    private static final int COUNT = 60;
    private SelectValueScrollAdapter mAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAdapter = new SelectValueScrollAdapter(getContext(), COUNT);
    }

    public void testGetCount() {
        assertEquals(COUNT, mAdapter.getCount());
    }

    public void testGetItem() {
        Object item = mAdapter.getItem(0);

        assertTrue(item instanceof Integer);
        assertEquals(Integer.valueOf(0), item);
    }

    public void testGetView() {
        View view = mAdapter.getView(7, null, null);
        assertNotNull(view);

        TextView textView = (TextView) view.findViewById(R.id.value);
        assertNotNull(textView);
        assertEquals("07", textView.getText());
    }

    public void testGetPosition() {
        int expectedPosition = 12;
        int position = mAdapter.getPosition(Integer.valueOf(expectedPosition));
        assertEquals(expectedPosition, position);
    }

    public void testGetPositionOther() {
        int position = mAdapter.getPosition(this);
        assertEquals(AdapterView.INVALID_POSITION, position);
    }
}
