package com.gesturebaseddrawing;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class PaintView extends View {
    public static int BRUSH_SIZE = 20;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY, minX, minY, maxX, maxY, startX, startY, endX, endY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private boolean freeDraw = true;
    private boolean square = false;
    private boolean rectangle = false;
    private boolean circle = false;
    private boolean line = false;
    private boolean triangle = false;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);


    public PaintView(Context context) {
        this(context, null);
    }


    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);


        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void setFreeDraw(){
        freeDraw = true;
        square = false;
        triangle = false;
        line = false;
        circle = false;
        rectangle = false;
    }

    public void setRectangle(){
        freeDraw = false;
        rectangle = true;
        square = false;
        triangle = false;
        line = false;
        circle = false;
    }

    public void setSquare(){
        freeDraw = false;
        square = true;
        triangle = false;
        line = false;
        circle = false;
        rectangle = false;
    }

    public void setTriangle(){
        freeDraw = false;
        square = false;
        triangle = true;
        line = false;
        circle = false;
        rectangle = false;
    }
    public void setLine(){
        freeDraw = false;
        square = false;
        triangle = false;
        line = true;
        circle = false;
        rectangle = false;
    }
    public void setCircle(){
        freeDraw = false;
        square = false;
        triangle = false;
        line = false;
        circle = true;
        rectangle = false;
    }
    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        invalidate();
    }

    public void black() {
        mPaint.setColor(Color.BLACK);
        currentColor = Color.BLACK;
    }

    public void red() {
        mPaint.setColor(Color.RED);
        currentColor = Color.RED;
    }

    public void blue() {
        mPaint.setColor(Color.BLUE);
        currentColor = Color.BLUE;
    }

    public void green() {
        mPaint.setColor(Color.GREEN);
        currentColor = Color.GREEN;
    }

    public void yellow() {
        mPaint.setColor(Color.YELLOW);
        currentColor = Color.YELLOW;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    //MANAGE USER's TOUCH
    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        minX = x;
        maxX = x;
        minY = y;
        maxY = y;
        startX = x;
        startY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        endX = x;
        endY = y;

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

            if(!freeDraw) {
                if (x > maxX) {
                    maxX = x;
                } else if (x < minX) {
                    minX = x;
                } else if (y > maxY) {
                    maxY = y;
                } else if (y < minY) {
                    minY = y;
                }
            }
        }
    }

    private void touchUp() {
        System.out.println("MaxX: "+maxX+", MinX: "+ minX + ", MaxY: " +maxY +", MinY: " + minY );
        if(freeDraw){
            mPath.lineTo(mX, mY);
        }else if(rectangle){
            paths.remove(paths.size()-1);
            mPath = new Path();
            FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
            paths.add(fp);
            mPath.reset();
            mPath.moveTo(minX, minY);
            mPath.lineTo(minX, maxY);
            mPath.lineTo(maxX, maxY);
            mPath.lineTo(maxX, minY);
            mPath.lineTo(minX, minY);

        }else if(triangle){
            paths.remove(paths.size()-1);
            mPath = new Path();
            FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
            paths.add(fp);
            mPath.reset();
            float width = (maxX-minX);
            float height = (maxY-minY);
            mPath.moveTo(startX, startY);
            if(Math.abs(startY-minY) > Math.abs(startY-maxY)){
                if(Math.abs(startX-minX) > Math.abs(startX-maxX)){
                    mPath.lineTo(startX-width, startY);
                    mPath.lineTo(startX-(width/2), startY-height);
                }else{
                    mPath.lineTo(startX+width, startY);
                    mPath.lineTo(startX+(width/2), startY-height);
                }
            }else{
                if(Math.abs(startX-minX) > Math.abs(startX-maxX)){
                    mPath.lineTo(startX-width, startY);
                    mPath.lineTo(startX-(width/2), startY+height);
                }else{
                    mPath.lineTo(startX+width, startY);
                    mPath.lineTo(startX+(width/2), startY+height);
                }
            }
            mPath.lineTo(startX, startY);
        }else if(circle){
            paths.remove(paths.size()-1);
            mPath = new Path();
            FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
            paths.add(fp);
            mPath.reset();
            mPath.moveTo((minX+maxX)/2, (minY+maxY)/2);
            float radius = (float) Math.sqrt((maxY - minY) * (maxY - minY) + (maxX - minX) * (maxX - minX))/2;
            mPath.addCircle((minX+maxX)/2, (minY+maxY)/2, radius, Path.Direction.CW);

        }else if(line){
            paths.remove(paths.size()-1);
            mPath = new Path();
            FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
            paths.add(fp);
            mPath.reset();
            mPath.moveTo(startX, startY);
            mPath.lineTo(endX, endY);

        }else if (square){
            paths.remove(paths.size()-1);
            mPath = new Path();
            FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
            paths.add(fp);
            mPath.reset();
            float slength = (maxX-minX);
            mPath.moveTo(minX, minY);
            mPath.lineTo(minX+slength, minY);
            mPath.lineTo(minX+slength, minY+slength);
            mPath.lineTo(minX, minY+slength);
            mPath.lineTo(minX, minY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //System.out.println("DOWN EVENT: " + x + ", " + y);
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                //System.out.println("MOVE EVENT: " + x + ", " + y);
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //System.out.println("UP EVENT: " + x + ", " + y);
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}
