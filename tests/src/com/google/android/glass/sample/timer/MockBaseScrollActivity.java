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

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/**
 * Implementation of {@link BaseScrollActivity} for testing.
 */
public class MockBaseScrollActivity extends BaseScrollActivity {

    private final CardScrollAdapter mAdapter = new CardScrollAdapter() {

        @Override
        public int getPosition(Object item) {
            return AdapterView.INVALID_POSITION;
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    };

    @Override
    protected void setAdapter(CardScrollView view) {
        view.setAdapter(mAdapter);
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        return false;
    }

}
