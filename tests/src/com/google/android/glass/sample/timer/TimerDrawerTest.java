/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.test.AndroidTestCase;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.Surface;

/**
 * Unit tests for {@link TimerDrawer}.
 */
public class TimerDrawerTest extends AndroidTestCase {

    private TimerDrawer mDrawer;
    private TimerView mView;

    private int mCanvasLockedCount;
    private int mCanvasUnlockedCount;
    private int mDrawCount;

    /** Simple {@link SurfaceHolder} implementation for testing. */
    private final SurfaceHolder mHolder = new SurfaceHolder() {
        Canvas mCanvas = new Canvas();

        @Override
        public void addCallback(SurfaceHolder.Callback callback) {
            // Nothing to do here.
        }

        @Override
        public Surface getSurface() {
            return null;
        }

        @Override
        public Rect getSurfaceFrame() {
            return null;
        }

        @Override
        public boolean isCreating() {
            return false;
        }

        @Override
        public Canvas lockCanvas() {
            ++mCanvasLockedCount;
            return mCanvas;
        }

        @Override
        public Canvas lockCanvas(Rect rect) {
            return lockCanvas();
        }

        @Override
        public void removeCallback(SurfaceHolder.Callback callback) {
            // Nothing to do here.
        }

        @Override
        public void setFixedSize(int width, int height) {
            // Nothing to do here.
        }

        @Override
        public void setFormat(int format) {
            // Nothing to do here.
        }

        @Override
        public void setKeepScreenOn(boolean keepScreenOn) {
            // Nothing to do here.
        }

        @Override
        public void setSizeFromLayout() {
            // Nothing to do here.
        }

        @Override
        public void setType(int type) {
            // Nothing to do here.
        }

        @Override
        public void unlockCanvasAndPost(Canvas canvas) {
            assertEquals(mCanvas, canvas);
            ++mCanvasUnlockedCount;
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mView = new TimerView(getContext()) {

            @Override
            public void draw(Canvas canvas) {
                ++mDrawCount;
            }
        };
        mDrawer = new TimerDrawer(mView);

        mCanvasLockedCount = 0;
        mCanvasUnlockedCount = 0;
        mDrawCount = 0;
    }

    public void testConstructorSetsListener() {
        assertNotNull(mView.getListener());
    }

    public void testSurfaceChanged() {
        int width = 640;
        int height = 360;

        // Ensure the test is not a no-op.
        assertEquals(0, mView.getWidth());
        assertEquals(0, mView.getHeight());
        mDrawer.surfaceChanged(mHolder, 0, width, height);
        assertEquals(0, mDrawCount);
        assertEquals(width, mView.getWidth());
        assertEquals(height, mView.getHeight());
    }

    public void testSurfaceCreatedDrawsOnce() {
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
    }

    public void testSurfaceCreatedRenderingPaused() {
        mDrawer.renderingPaused(mHolder, true);
        assertEquals(0, mDrawCount);
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
    }

    public void testSurfaceDestroyed() {
        mDrawer.surfaceDestroyed(mHolder);
        assertEquals(0, mDrawCount);
    }

    public void testRenderingPausedFalseNoSurface() {
        mDrawer.renderingPaused(mHolder, false);
        assertEquals(0, mDrawCount);
    }

    public void testRenderingPausedFalseWithSurface() {
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
        mDrawer.renderingPaused(mHolder, false);
        assertEquals(2, mDrawCount);
    }

    public void testRenderingPausedTrue() {
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
        // Test that no other calls to mView.draw() occurred.
        mDrawer.renderingPaused(mHolder, true);
        assertEquals(1, mDrawCount);
    }

    public void testDrawProperlyLocksAndUnlocksCanvas() {
        // This also calls mDrawer.draw();
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
        assertEquals(1, mCanvasLockedCount);
        assertEquals(1, mCanvasUnlockedCount);
    }

    public void testListenerNoSurfaceDoesNotCallDraw() {
        mView.getListener().onChange();
        assertEquals(0, mDrawCount);
    }

    public void testListenerWithSurfaceCallsDraw() {
        mDrawer.surfaceCreated(mHolder);
        assertEquals(1, mDrawCount);
        mView.getListener().onChange();
        assertEquals(2, mDrawCount);
    }

}
