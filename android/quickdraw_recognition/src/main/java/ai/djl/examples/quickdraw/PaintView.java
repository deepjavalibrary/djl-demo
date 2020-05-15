/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package ai.djl.examples.quickdraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import ai.djl.modality.Classifications;

public final class PaintView extends View {

    public static int BRUSH_SIZE = 20;
    public static final int DEFAULT_PAINT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    private float x, y;
    private Path path;
    private Paint paint;
    private ArrayList<Path> paths = new ArrayList<>();
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Toast messageToast;
    private ImageView imageView;
    private Bound maxBound;
    private DoodleModel model;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(DEFAULT_PAINT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics, ImageView imageView, java.nio.file.Path path) {
        int width = metrics.widthPixels;
        int height = Math.min(width, metrics.heightPixels);
        this.imageView = imageView;

        maxBound = new Bound();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        try {
            model = new DoodleModel(path);
        } catch (Exception e) {
            throw new IllegalArgumentException("Model load failed", e);
        }
    }

    public void clear() {
        paths.clear();
        maxBound = new Bound();
        imageView.invalidate();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        this.canvas.drawColor(DEFAULT_BG_COLOR);

        for (Path path : paths) {
            paint.setColor(DEFAULT_PAINT_COLOR);
            paint.setStrokeWidth(BRUSH_SIZE);
            this.canvas.drawPath(path, paint);
        }
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        path = new Path();
        paths.add(path);
        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp() {
        path.lineTo(this.x, this.y);
        maxBound.add(new Path(path));
    }

    public void runInference() {
        RectF bound = maxBound.getBound();
        int x = (int) Math.floor(bound.left);
        int y = (int) Math.floor(bound.top);
        int width = (int) Math.ceil(bound.width());
        int height = (int) Math.ceil(bound.height());
        // do crop
        Bitmap bmp = Bitmap.createBitmap(bitmap, x - 10, y - 10, width + 10, height + 10);
        // do scaling
        bmp = Bitmap.createScaledBitmap(bmp, 64, 64, true);
        if (messageToast != null) {
            messageToast.cancel();
        }
        Classifications classifications = model.predict(bmp);
        Bitmap present = Bitmap.createScaledBitmap(bmp, imageView.getWidth(), imageView.getHeight(), true);
        imageView.setImageBitmap(present);

        messageToast = Toast.makeText(getContext(), classifications.toString(), Toast.LENGTH_SHORT);
        messageToast.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                runInference();
                invalidate();
                break;
        }

        return true;
    }
}