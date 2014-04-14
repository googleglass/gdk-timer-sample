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
 * Activity to set the timer.
 */
public class SetTimerActivity extends BaseScrollActivity {

    public static final String EXTRA_DURATION_MILLIS = "duration_millis";

    /** Request code for selecting a value, visible for testing. */
    static final int SELECT_VALUE = 100;

    private SetTimerScrollAdapter mAdapter;

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                int position = getView().getSelectedItemPosition();
                SetTimerScrollAdapter.TimeComponents component =
                        (SetTimerScrollAdapter.TimeComponents) mAdapter.getItem(position);
                Intent selectValueIntent = new Intent(this, SelectValueActivity.class);

                selectValueIntent.putExtra(
                    SelectValueActivity.EXTRA_COUNT, component.getMaxValue());
                selectValueIntent.putExtra(
                        SelectValueActivity.EXTRA_INITIAL_VALUE,
                        (int) mAdapter.getTimeComponent(component));
                startActivityForResult(selectValueIntent, SELECT_VALUE);
                playSoundEffect(Sounds.TAP);
                return true;
            case SWIPE_DOWN:
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_DURATION_MILLIS, mAdapter.getDurationMillis());
                setResultInternal(RESULT_OK, resultIntent);
                playSoundEffect(Sounds.DISMISSED);
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_VALUE) {
            int position = getView().getSelectedItemPosition();
            SetTimerScrollAdapter.TimeComponents component =
                    (SetTimerScrollAdapter.TimeComponents) mAdapter.getItem(position);

            mAdapter.setTimeComponent(
                    component, data.getIntExtra(SelectValueActivity.EXTRA_SELECTED_VALUE, 0));
        }
    }

    @Override
    protected void setAdapter(CardScrollView view) {
        mAdapter = new SetTimerScrollAdapter(this);
        mAdapter.setDurationMillis(getIntent().getLongExtra(EXTRA_DURATION_MILLIS, 0));
        view.setAdapter(mAdapter);
    }

}
