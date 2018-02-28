package com.example.rui.mnist.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Rui on 2018/2/28.
 */

public class PrinterView extends View {

    //画笔
    private Paint paint;

    //用来存储“路径”
    private Path path;

    //屏幕宽
    private int width;

    public PrinterView(Context context) {
        super(context);
    }

    public PrinterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
        paint.setStyle(Paint.Style.STROKE);
        path = new Path();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        width = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
        }
        //刷新view
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //定制画板的宽和高
        super.onMeasure(width, width);
    }

    public void clean() {
        path.reset();
        invalidate();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public float[] getData(int width, int height) {
        float[] data = new float[height * width];
        try {
            //先让cache可以被读取(将View转化为图片都会使用cache)
            setDrawingCacheEnabled(true);
            setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            Bitmap cache = getDrawingCache();
            dealData(cache, data, width, height);
        } finally {
            setDrawingCacheEnabled(false);
        }
        return data;
    }

    private void dealData(Bitmap bm, float[] data, int newWidth, int newHeight) {
        //获得bitmap的宽和高
        int width = bm.getWidth();
        int height = bm.getHeight();

        //计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //获得目标大小的图
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                //获得每个点的像素值
                int pixel = newBm.getPixel(x, y);
                data[newWidth * y + x] = pixel == 0xffffffff ? 0 : 1;
            }
        }

    }
}
