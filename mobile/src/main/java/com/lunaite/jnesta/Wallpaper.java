package com.lunaite.jnesta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Random;


public class Wallpaper extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new LiveWallpaperEngine();
    }

    private class LiveWallpaperEngine extends Engine {
        Bitmap pic, up0, up1, up2, up3, down0, down1, down2, down3, left0, left1, left2, left3, right0, right1, right2, right3, back0, back1;
        float x, y;
        Random random = new Random();
        Canvas canvas = null;
        private final Handler handler = new Handler();
        private final Runnable drawer = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private boolean visible = true;
        private int width;
        private int height;

        private boolean created = false;

        Paint paint = new Paint();

        public LiveWallpaperEngine() {
            handler.post(drawer);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            x = 500;
            y = 500;
            back0 = BitmapFactory.decodeResource(getResources(), R.drawable.back0);
            back1 = BitmapFactory.decodeResource(getResources(), R.drawable.back1);
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
            pic = down1;
            paint.setAntiAlias(false);
            paint.setDither(true);
            paint.setFilterBitmap(false);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawer);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawer);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawer);
            } else {
                handler.removeCallbacks(drawer);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }

        int i = 2;

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
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

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawImage(canvas, x, y);
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(drawer);
            if (visible) {
                handler.postDelayed(drawer, 10);
            }

            if (!created) {
                double ratioWidth = 1080.0 / this.width;
                double ratioHeight = 1920.0 / this.height;
                back0 = Bitmap.createScaledBitmap(back0, this.width, this.height, false);
                back1 = Bitmap.createScaledBitmap(back1, this.width, this.height, false);
                up0 = Bitmap.createScaledBitmap(up0, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                up1 = Bitmap.createScaledBitmap(up1, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                up2 = Bitmap.createScaledBitmap(up2, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                up3 = Bitmap.createScaledBitmap(up3, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                down0 = Bitmap.createScaledBitmap(down0, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                down1 = Bitmap.createScaledBitmap(down1, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                down2 = Bitmap.createScaledBitmap(down2, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                down3 = Bitmap.createScaledBitmap(down3, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                right0 = Bitmap.createScaledBitmap(right0, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                right1 = Bitmap.createScaledBitmap(right1, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                right2 = Bitmap.createScaledBitmap(right2, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                right3 = Bitmap.createScaledBitmap(right3, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                left0 = Bitmap.createScaledBitmap(left0, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                left1 = Bitmap.createScaledBitmap(left1, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                left2 = Bitmap.createScaledBitmap(left2, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                left3 = Bitmap.createScaledBitmap(left3, (int) (160.0 / ratioWidth), (int) (((int) (160.0 / ratioWidth)) * 1.1875), false);
                created = true;
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
                    y = height + 10;
                    break;
                case 3:
                    randomY();
                    x = width + 10;
                    break;
            }
        }

        private void randomY() {
            int idY = random.nextInt(5);

            switch (idY) {
                case 0:
                    y = height - 210;
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

        private void drawImage(Canvas canvas, float x, float y) {
            id += 0.1;

            canvas.drawBitmap(back0, 0, 0, paint);

            switch (i) {
                case 0:
                    if (id < 1) {
                        canvas.drawBitmap(down0, x, y, paint);
                    } else if (id < 2) {
                        canvas.drawBitmap(down1, x, y, paint);
                    } else if (id < 3) {
                        canvas.drawBitmap(down2, x, y, paint);
                    } else if (id < 4) {
                        canvas.drawBitmap(down3, x, y, paint);
                    } else if (id >= 4) {
                        canvas.drawBitmap(down3, x, y, paint);
                        id = 0;
                    }
                    if (y > height + 100) {
                        randomJNesta();
                    }
                    break;
                case 1:
                    if (id < 1) {
                        canvas.drawBitmap(right0, x, y, paint);
                    } else if (id < 2) {
                        canvas.drawBitmap(right1, x, y, paint);
                    } else if (id < 3) {
                        canvas.drawBitmap(right2, x, y, paint);
                    } else if (id < 4) {
                        canvas.drawBitmap(right3, x, y, paint);
                    } else if (id >= 4) {
                        canvas.drawBitmap(right3, x, y, paint);
                        id = 0;
                    }
                    if (x > width + 100) {
                        randomJNesta();
                    }
                    break;
                case 2:
                    if (id < 1) {
                        canvas.drawBitmap(up0, x, y, paint);
                    } else if (id < 2) {
                        canvas.drawBitmap(up1, x, y, paint);
                    } else if (id < 3) {
                        canvas.drawBitmap(up2, x, y, paint);
                    } else if (id < 4) {
                        canvas.drawBitmap(up3, x, y, paint);
                    } else if (id > 4) {
                        canvas.drawBitmap(up3, x, y, paint);
                        id = 0;
                    }
                    if (y < -pic.getHeight() - 100) {
                        randomJNesta();
                    }
                    break;
                case 3:
                    if (id < 1) {
                        canvas.drawBitmap(left0, x, y, paint);
                    } else if (id < 2) {
                        canvas.drawBitmap(left1, x, y, paint);
                    } else if (id < 3) {
                        canvas.drawBitmap(left2, x, y, paint);
                    } else if (id < 4) {
                        canvas.drawBitmap(left3, x, y, paint);
                    } else if (id >= 4) {
                        canvas.drawBitmap(left3, x, y, paint);
                        id = 0;
                    }
                    if (x < -pic.getWidth() - 100) {
                        randomJNesta();
                    }
                    break;
            }

            canvas.drawBitmap(back1, 0, 0, paint);
        }
    }
}
