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

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;

/**
 * Unit tests for {@link BaseScrollActivity}.
 */
public class BaseScrollActivityTest extends ActivityUnitTestCase<MockBaseScrollActivity> {

    private Intent mActivityIntent;

    public BaseScrollActivityTest() {
        super(MockBaseScrollActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mActivityIntent =
                new Intent(getInstrumentation().getTargetContext(), BaseScrollActivity.class);
    }

    @UiThreadTest
    public void testOnCreate() {
        MockBaseScrollActivity activity = startActivity(mActivityIntent, null, null);

        assertNotNull(activity.getView());
        assertFalse(activity.getView().isActivated());
        assertTrue(activity.getView().isHorizontalScrollBarEnabled());
        assertNotNull(activity.getView().getAdapter());
    }

    @UiThreadTest
    public void testOnResumeActivatesView() throws Throwable {
        MockBaseScrollActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        assertTrue(activity.getView().isActivated());
    }

    @UiThreadTest
    public void testOnPauseDeactivatesView() throws Throwable {
        MockBaseScrollActivity activity = startActivity(mActivityIntent, null, null);

        activity.onResume();
        assertTrue(activity.getView().isActivated());
        activity.onPause();
        assertFalse(activity.getView().isActivated());
    }
}
