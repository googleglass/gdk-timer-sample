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

import java.lang.Runnable;

/**
 * Activity showing the options menu.
 */
public class MenuActivity extends Activity {

    /** Request code for setting the timer, visible for testing. */
    static final int SET_TIMER = 100;

    private final Handler mHandler = new Handler();

    private Timer mTimer;
    private boolean mAttachedToWindow;
    private boolean mOptionsMenuOpen;
    private boolean mSettingTimer;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof TimerService.TimerBinder) {
                mTimer = ((TimerService.TimerBinder) service).getTimer();
                openOptionsMenu();
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
        bindService(new Intent(this, TimerService.class), mConnection, 0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        openOptionsMenu();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    @Override
    public void openOptionsMenu() {
        if (!mOptionsMenuOpen && mAttachedToWindow && mTimer != null) {
            mOptionsMenuOpen = true;
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean timeSet = mTimer.getDurationMillis() != 0;

        setOptionsMenuGroupState(menu, R.id.no_time_set, !timeSet);
        setOptionsMenuGroupState(menu, R.id.time_set, timeSet);
        if (timeSet) {
            setOptionsMenuState(
                    menu.findItem(R.id.start), !mTimer.isRunning() && !mTimer.isStarted());
            setOptionsMenuState(
                    menu.findItem(R.id.resume), !mTimer.isRunning() && mTimer.isStarted());
            setOptionsMenuState(
                    menu.findItem(R.id.pause),
                    mTimer.isRunning() && mTimer.getRemainingTimeMillis() > 0);
            setOptionsMenuState(menu.findItem(R.id.reset), mTimer.isStarted());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.set_timer:
                // Start the new Activity at the end of the message queue for proper options menu
                // animation. This is only needed when starting a new Activity or stopping a Service
                // that published a LiveCard.
                post(new Runnable() {

                    @Override
                    public void run() {
                        Intent setTimerIntent =
                                new Intent(MenuActivity.this, SetTimerActivity.class);

                        setTimerIntent.putExtra(
                                SetTimerActivity.EXTRA_DURATION_MILLIS, mTimer.getDurationMillis());
                        startActivityForResult(setTimerIntent, SET_TIMER);
                    }
                });
                mTimer.reset();
                mSettingTimer = true;
                return true;
            case R.id.stop:
                // Stop the service at the end of the message queue for proper options menu
                // animation. This is only needed when starting a new Activity or stopping a Service
                // that published a LiveCard.
                post(new Runnable() {

                    @Override
                    public void run() {
                        stopService(new Intent(MenuActivity.this, TimerService.class));
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mOptionsMenuOpen = false;
        if (!mSettingTimer) {
            // Nothing else to do, closing the Activity.
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SET_TIMER) {
            mTimer.setDurationMillis(data.getLongExtra(SetTimerActivity.EXTRA_DURATION_MILLIS, 0));
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
