package com.example.quickdraw;

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

public class PaintView extends View {

    public static int BRUSH_SIZE = 20;
    public static final int DEFAULT_PAINT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Path> paths = new ArrayList<>();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Toast messageToast;
    private ImageView imageView;
    private Bound maxBound;
    private DoodleModel model;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_PAINT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics, ImageView imageView, java.nio.file.Path path) {
        int width = metrics.widthPixels;
        int height = Math.min(width, metrics.heightPixels);
        this.imageView = imageView;

        maxBound = new Bound();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
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
        mCanvas.drawColor(DEFAULT_BG_COLOR);

        for (Path path : paths) {
            mPaint.setColor(DEFAULT_PAINT_COLOR);
            mPaint.setStrokeWidth(BRUSH_SIZE);
            mCanvas.drawPath(path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        paths.add(mPath);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        maxBound.add(new Path(mPath));
    }

    public void runInference() {
        RectF bound = maxBound.getBound();
        int x = (int) Math.floor(bound.left);
        int y = (int) Math.floor(bound.top);
        int width = (int) Math.ceil(bound.width());
        int height = (int) Math.ceil(bound.height());
        // do crop
        Bitmap bmp = Bitmap.createBitmap(mBitmap, x - 10, y - 10, width + 10, height + 10);
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