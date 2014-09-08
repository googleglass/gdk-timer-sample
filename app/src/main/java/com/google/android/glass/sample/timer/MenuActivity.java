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

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.view.WindowUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.lang.Runnable;

/**
 * This activity manages the options menu that appears when the user taps on the timer's live
 * card or says "ok glass" while the live card is settled.
 */
public class MenuActivity extends Activity {

    /** Request code for setting the timer, visible for testing. */
    static final int SET_TIMER = 100;

    private final Handler mHandler = new Handler();

    private Timer mTimer;
    private boolean mAttachedToWindow;
    private boolean mIsMenuClosed;
    private boolean mPreparePanelCalled;
    private boolean mIsSettingTimer;

    private boolean mFromLiveCardVoice;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TimerService.TimerBinder) {
                mTimer = ((TimerService.TimerBinder) service).getTimer();
                openMenu();
            }
            // No need to keep the service bound.
            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFromLiveCardVoice = getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
        if (mFromLiveCardVoice) {
            // When activated by voice from a live card, enable voice commands. The menu
            // will automatically "jump" ahead to the items (skipping the guard phrase
            // that was already said at the live card).
            getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        }

        // Bind to the Timer service to retrive the current timer's data.
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra(
            TimerService.EXTRA_TIMER_HASH_CODE,
            getIntent().getIntExtra(TimerService.EXTRA_TIMER_HASH_CODE, 0));
        serviceIntent.setData(getIntent().getData());
        bindService(serviceIntent, mConnection, 0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        openMenu();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (isMyMenu(featureId)) {
            getMenuInflater().inflate(R.menu.timer, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        mPreparePanelCalled = true;
        if (isMyMenu(featureId)) {
            if (mTimer == null) {
                // Can't prepare the menu as we're not yet bound to a timer.
                return false;
            } else {
                setOptionsMenuState(
                    menu.findItem(R.id.start), !mTimer.isRunning() && !mTimer.isStarted());
                setOptionsMenuState(
                    menu.findItem(R.id.resume), !mTimer.isRunning() && mTimer.isStarted());
                setOptionsMenuState(
                    menu.findItem(R.id.pause),
                    mTimer.isRunning() && mTimer.getRemainingTimeMillis() > 0);
                setOptionsMenuState(menu.findItem(R.id.reset), mTimer.isStarted());
                // Don't reopen menu once we are finishing. This is necessary
                // since voice menus reopen themselves while in focus.
                return !mIsMenuClosed;
            }
        }
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (!isMyMenu(featureId)) {
            return super.onMenuItemSelected(featureId, item);
        }
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.start:
            case R.id.resume:
                mTimer.start();
                return true;
            case R.id.pause:
                mTimer.pause();
                return true;
            case R.id.reset:
                mTimer.reset();
                return true;
            case R.id.change_timer:
                // Start the new Activity at the end of the message queue for proper options menu
                // animation. This is only needed when starting a new Activity or stopping a Service
                // that published a LiveCard.
                post(new Runnable() {

                    @Override
                    public void run() {
                        startSetTimerActivity();
                    }
                });
                mIsSettingTimer = true;
                return true;
            case R.id.stop:
                // Stop the service at the end of the message queue for proper options menu
                // animation. This is only needed when starting a new Activity or stopping a Service
                // that published a LiveCard.
                post(new Runnable() {

                    @Override
                    public void run() {
                        Intent timerIntent = new Intent(MenuActivity.this, TimerService.class);

                        timerIntent.setAction(TimerService.ACTION_STOP);
                        timerIntent.putExtra(
                                TimerService.EXTRA_TIMER_HASH_CODE, mTimer.hashCode());
                        startService(timerIntent);
                    }
                });
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        if (isMyMenu(featureId)) {
            mIsMenuClosed = true;
            if (!mIsSettingTimer) {
                // Nothing else to do, closing the Activity.
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SET_TIMER) {
            mTimer.reset();
            mTimer.setDurationMillis(data.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
            if (data.getBooleanExtra(SetTimerActivity.EXTRA_START_TIMER, false)) {
                mTimer.start();
            }
        }
        finish();
    }

    /**
     * Posts a {@link Runnable} at the end of the message loop, overridable for testing.
     */
    protected void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    /**
     * Opens the touch or voice menu iff all the conditions are satifisfied.
     */
    private void openMenu() {
        if (mAttachedToWindow && mTimer != null) {
            if (mFromLiveCardVoice) {
                if (mPreparePanelCalled) {
                    // Invalidates the previously prepared voice menu now that we can properly
                    // prepare it.
                    getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
                }
            } else {
                // Open the options menu for the touch flow.
                openOptionsMenu();
            }
        }
    }

    /**
     * Starts the {@link SetTimerActivity}.
     */
    private void startSetTimerActivity() {
        Intent setTimerIntent = new Intent(this, SetTimerActivity.class);

        setTimerIntent.putExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, mTimer.getDurationMillis());
        startActivityForResult(setTimerIntent, SET_TIMER);
    }

    /**
     * Returns {@code true} when the {@code featureId} belongs to the options menu or voice
     * menu that are controlled by this menu activity.
     */
    private boolean isMyMenu(int featureId) {
        return featureId == Window.FEATURE_OPTIONS_PANEL ||
               featureId == WindowUtils.FEATURE_VOICE_COMMANDS;
    }

    /**
     * Sets a {@code MenuItem} visible and enabled state.
     */
    private static void setOptionsMenuState(MenuItem menuItem, boolean enabled) {
        menuItem.setVisible(enabled);
        menuItem.setEnabled(enabled);
    }

    /**
     * Sets all menu items visible and enabled state that are in the given group.
     */
    private static void setOptionsMenuGroupState(Menu menu, int groupId, boolean enabled) {
        menu.setGroupVisible(groupId, enabled);
        menu.setGroupEnabled(groupId, enabled);
    }
}
