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

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Base {@link Activity} for {@link CardScrollView} based Activities.
 */
public abstract class BaseScrollActivity extends Activity implements GestureDetector.BaseListener {

    private AudioManager mAudioManager;
    private CardScrollView mView;
    private GestureDetector mDetector;

    /**
     * Initliazes and sets the underlying adapter to be used by the {@link CardScrollView}
     * instantiated in {@link onCreate}, visible for testing.
     */
    protected abstract void setAdapter(CardScrollView view);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mView = new CardScrollView(this) {
            @Override
            public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
                if (mDetector.onMotionEvent(event)) {
                    return true;
                }
                return super.dispatchGenericFocusedEvent(event);
            }
        };
        mView.setHorizontalScrollBarEnabled(true);
        setAdapter(mView);
        setContentView(mView);

        mDetector = new GestureDetector(this).setBaseListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mView.activate();
    }

    @Override
    public void onPause() {
        super.onPause();
        mView.deactivate();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mDetector.onMotionEvent(event);
    }

    /**
     * Sets the {@link Activity} result, overridable for testing.
     */
    protected void setResultInternal(int resultCode, Intent resultIntent) {
        setResult(resultCode, resultIntent);
    }

    /**
     * Plays a sound effect, overridable for testing.
     */
    protected void playSoundEffect(int soundId) {
        mAudioManager.playSoundEffect(soundId);
    }

    /**
     * Returns the {@link CardScrollView}, visible for testing.
     */
    CardScrollView getView() {
        return mView;
    }

}
