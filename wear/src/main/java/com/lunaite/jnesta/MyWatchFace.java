package com.lunaite.jnesta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.graphics.Palette;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final float HOUR_STROKE_WIDTH = 5f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;

        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;

        private static final int SHADOW_RADIUS = 6;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        private float mCenterX;
        private float mCenterY;
        private float mSecondHandLength;
        private float sMinuteHandLength;
        private float sHourHandLength;
        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mWatchHandColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mTickAndCirclePaint;
        private Paint mBackgroundPaint;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        Bitmap pic, up0, up1, up2, up3, down0, down1, down2, down3, left0, left1, left2, left3, right0, right1, right2, right3, back0, back1;
        float x, y;
        Random random = new Random();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            initializeBackground();
            initializeWatchFace();

            back0 = BitmapFactory.decodeResource(getResources(), R.drawable.watchback0);
            back1 = BitmapFactory.decodeResource(getResources(), R.drawable.watchback1);
            up0 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaup_0);
            up1 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaup_1);
            up2 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaup_2);
            up3 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaup_3);
            down0 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestadown_0);
            down1 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestadown_1);
            down2 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestadown_2);
            down3 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestadown_3);
            left0 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaleft_0);
            left1 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaleft_1);
            left2 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaleft_2);
            left3 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaleft_3);
            right0 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaright_0);
            right1 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaright_1);
            right2 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaright_2);
            right3 = BitmapFactory.decodeResource(getResources(), R.drawable.jnestaright_3);
            up0 = Bitmap.createScaledBitmap(up0, 80, 95, false);
            up1 = Bitmap.createScaledBitmap(up1, 80, 95, false);
            up2 = Bitmap.createScaledBitmap(up2, 80, 95, false);
            up3 = Bitmap.createScaledBitmap(up3, 80, 95, false);
            down0 = Bitmap.createScaledBitmap(down0, 80, 95, false);
            down1 = Bitmap.createScaledBitmap(down1, 80, 95, false);
            down2 = Bitmap.createScaledBitmap(down2, 80, 95, false);
            down3 = Bitmap.createScaledBitmap(down3, 80, 95, false);
            right0 = Bitmap.createScaledBitmap(right0, 80, 95, false);
            right1 = Bitmap.createScaledBitmap(right1, 80, 95, false);
            right2 = Bitmap.createScaledBitmap(right2, 80, 95, false);
            right3 = Bitmap.createScaledBitmap(right3, 80, 95, false);
            left0 = Bitmap.createScaledBitmap(left0, 80, 95, false);
            left1 = Bitmap.createScaledBitmap(left1, 80, 95, false);
            left2 = Bitmap.createScaledBitmap(left2, 80, 95, false);
            left3 = Bitmap.createScaledBitmap(left3, 80, 95, false);
            pic = down1;
            back0 = Bitmap.createScaledBitmap(back0, 400, 400, false);
            back1 = Bitmap.createScaledBitmap(back1, 400, 400, false);
        }

        private void initializeBackground() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

            /* Extracts colors from background image to improve watchface style. */
            Palette.from(mBackgroundBitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    if (palette != null) {
                        mWatchHandHighlightColor = palette.getVibrantColor(Color.RED);
                        mWatchHandColor = palette.getLightVibrantColor(Color.WHITE);
                        mWatchHandShadowColor = palette.getDarkMutedColor(Color.BLACK);
                        updateWatchHandStyle();
                    }
                }
            });
        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mWatchHandColor = Color.WHITE;
            mWatchHandHighlightColor = Color.RED;
            mWatchHandShadowColor = Color.BLACK;

            mHourPaint = new Paint();
            mHourPaint.setColor(mWatchHandColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mWatchHandColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mWatchHandHighlightColor);
            mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void updateWatchHandStyle() {
            if (mAmbient) {
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondPaint.setColor(Color.WHITE);
                mTickAndCirclePaint.setColor(Color.WHITE);

                mHourPaint.setAntiAlias(false);
                mMinutePaint.setAntiAlias(false);
                mSecondPaint.setAntiAlias(false);
                mTickAndCirclePaint.setAntiAlias(false);

                mHourPaint.clearShadowLayer();
                mMinutePaint.clearShadowLayer();
                mSecondPaint.clearShadowLayer();
                mTickAndCirclePaint.clearShadowLayer();

            } else {
                mHourPaint.setColor(mWatchHandColor);
                mMinutePaint.setColor(mWatchHandColor);
                mSecondPaint.setColor(mWatchHandHighlightColor);
                mTickAndCirclePaint.setColor(mWatchHandColor);

                mHourPaint.setAntiAlias(true);
                mMinutePaint.setAntiAlias(true);
                mSecondPaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);

                mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * 0.875);
            sMinuteHandLength = (float) (mCenterX * 0.75);
            sHourHandLength = (float) (mCenterX * 0.5);


            /* Scale loaded background image (more efficient) if surface dimensions change. */
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * scale),
                    (int) (mBackgroundBitmap.getHeight() * scale), true);

            /*
             * Create a gray version of the image only if it will look nice on the device in
             * ambient mode. That means we don't want devices that support burn-in
             * protection (slight movements in pixels, not great for images going all the way to
             * edges) and low ambient mode (degrades image quality).
             *
             * Also, if your watch face will know about all images ahead of time (users aren't
             * selecting their own photos for the watch face), it will be more
             * efficient to create a black/white version (png, etc.) and load that when you need it.
             */
            if (!mBurnInProtection && !mLowBitAmbient) {
                initGrayBackgroundBitmap();
            }
        }

        private void initGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    break;
            }
            invalidate();
        }

        int i = 2;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            drawBackground(canvas);
            drawWatchFace(canvas);

            switch (i) {
                case 0:
                    y += 10;
                    break;
                case 1:
                    x += 10;
                    break;
                case 2:
                    y -= 10;
                    break;
                case 3:
                    x -= 10;
                    break;
            }
        }

        private double id = 0;

        private void randomJNesta() {
            switch (i) {
                case 0:
                    i = random.nextInt(3) + 1;
                    break;
                case 1:
                    i = random.nextInt(4);
                    if (i == 1) {
                        i++;
                    }
                    break;
                case 2:
                    i = random.nextInt(4);
                    if (i == 2) {
                        i++;
                    }
                    break;
                case 3:
                    i = random.nextInt(4);
                    if (i == 4) {
                        i = 0;
                    }
                    break;
            }

            switch (i) {
                case 0:
                    x = 500;
                    y = -pic.getHeight() - 10;
                    break;
                case 1:
                    randomY();
                    x = -pic.getWidth() - 10;
                    break;
                case 2:
                    x = 500;
                    y = 400 + 10;
                    break;
                case 3:
                    randomY();
                    x = 400 + 10;
                    break;
            }
        }

        private void randomY() {
            int idY = random.nextInt(5);

            switch (idY) {
                case 0:
                    y = 400 - 210;
                    break;
                case 1:
                    y = 20;
                    break;
                case 2:
                    y = 400;
                    break;
                case 3:
                    y = 750;
                    break;
                case 4:
                    y = 1100;
                    break;
            }
        }

        private void drawBackground(Canvas canvas) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, 0, mBackgroundPaint);
            } else {
                id += 0.1;
                canvas.drawBitmap(back0, 0, 0, null);

                switch (i) {
                    case 0:
                        if (id < 1) {
                            canvas.drawBitmap(down0, x, y, null);
                        } else if (id < 2) {
                            canvas.drawBitmap(down1, x, y, null);
                        } else if (id < 3) {
                            canvas.drawBitmap(down2, x, y, null);
                        } else if (id < 4) {
                            canvas.drawBitmap(down3, x, y, null);
                        } else if (id >= 4) {
                            canvas.drawBitmap(down3, x, y, null);
                            id = 0;
                        }
                        if (y > 400 + 100) {
                            randomJNesta();
                        }
                        break;
                    case 1:
                        if (id < 1) {
                            canvas.drawBitmap(right0, x, y, null);
                        } else if (id < 2) {
                            canvas.drawBitmap(right1, x, y, null);
                        } else if (id < 3) {
                            canvas.drawBitmap(right2, x, y, null);
                        } else if (id < 4) {
                            canvas.drawBitmap(right3, x, y, null);
                        } else if (id >= 4) {
                            canvas.drawBitmap(right3, x, y, null);
                            id = 0;
                        }
                        if (x > 400 + 100) {
                            randomJNesta();
                        }
                        break;
                    case 2:
                        if (id < 1) {
                            canvas.drawBitmap(up0, x, y, null);
                        } else if (id < 2) {
                            canvas.drawBitmap(up1, x, y, null);
                        } else if (id < 3) {
                            canvas.drawBitmap(up2, x, y, null);
                        } else if (id < 4) {
                            canvas.drawBitmap(up3, x, y, null);
                        } else if (id > 4) {
                            canvas.drawBitmap(up3, x, y, null);
                            id = 0;
                        }
                        if (y < -pic.getHeight() - 100) {
                            randomJNesta();
                        }
                        break;
                    case 3:
                        if (id < 1) {
                            canvas.drawBitmap(left0, x, y, null);
                        } else if (id < 2) {
                            canvas.drawBitmap(left1, x, y, null);
                        } else if (id < 3) {
                            canvas.drawBitmap(left2, x, y, null);
                        } else if (id < 4) {
                            canvas.drawBitmap(left3, x, y, null);
                        } else if (id >= 4) {
                            canvas.drawBitmap(left3, x, y, null);
                            id = 0;
                        }
                        if (x < -pic.getWidth() - 100) {
                            randomJNesta();
                        }
                        break;
                }

                canvas.drawBitmap(back1, 0, 0, null);
            }
        }

        private void drawWatchFace(Canvas canvas) {

            float innerTickRadius = mCenterX - 10;
            float outerTickRadius = mCenterX;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                        mCenterX + outerX, mCenterY + outerY, mTickAndCirclePaint);
            }

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            /*
             * Save the canvas state before we can begin to rotate it.
             */

            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - sHourHandLength,
                    mHourPaint);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - sMinuteHandLength,
                    mMinutePaint);

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
                canvas.drawLine(
                        mCenterX,
                        mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                        mCenterX,
                        mCenterY - mSecondHandLength,
                        mSecondPaint);

            }
            canvas.drawCircle(
                    mCenterX,
                    mCenterY,
                    CENTER_GAP_AND_CIRCLE_RADIUS,
                    mTickAndCirclePaint);

            /* Restore the canvas' original orientation. */
            canvas.restore();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
