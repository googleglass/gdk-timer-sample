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

import android.content.Intent;

import java.util.List;
import java.util.ArrayList;

/**
 * Extension of {@link SelectValueActivity} to mock out testable methods.
 */
public class MockSelectValueActivity extends SelectValueActivity {

    int mResultCode;
    Intent mResultIntent;
    List<Integer> mPlayedSoundEffects = new ArrayList<Integer>();

    @Override
    protected void setResultInternal(int code, Intent intent) {
        mResultCode = code;
        mResultIntent = intent;
    }

    @Override
    protected void playSoundEffect(int soundId) {
        mPlayedSoundEffects.add(soundId);
    }
}
