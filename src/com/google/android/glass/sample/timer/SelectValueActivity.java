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
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.content.Intent;
import android.os.Bundle;

/**
 * Activity to select a timer component value.
 */
public class SelectValueActivity extends BaseScrollActivity {

    public static final String EXTRA_COUNT = "count";
    public static final String EXTRA_INITIAL_VALUE = "initial_value";
    public static final String EXTRA_SELECTED_VALUE = "selected_value";

    private static final int DEFAULT_COUNT = 60;

    @Override
    public void onResume() {
        super.onResume();
        getView().setSelection(getIntent().getIntExtra(EXTRA_INITIAL_VALUE, 0));
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_VALUE, getView().getSelectedItemPosition());
                setResultInternal(RESULT_OK, resultIntent);
                playSoundEffect(Sounds.TAP);
                finish();
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

    @Override
    protected void setAdapter(CardScrollView view) {
        SelectValueScrollAdapter adapter = new SelectValueScrollAdapter(
                this, getIntent().getIntExtra(EXTRA_COUNT, DEFAULT_COUNT));
        view.setAdapter(adapter);
    }
}
