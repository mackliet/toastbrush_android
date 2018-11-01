package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.support.v4.util.Pair;
import android.widget.EditText;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DrawingView extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = Color.parseColor("#f5ca62");
    private int backgroundColor = Color.parseColor("#ffecc0");
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private JSONArray mDrawingPoints;

    private int mWidth;
    private int mHeight;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        mDrawingPoints = new JSONArray();
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
        if(canvasBitmap == null) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvasBitmap.eraseColor(backgroundColor);
        }
        else
        {
            canvasBitmap.setHeight(h);
            canvasBitmap.setWidth(w);
        }
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret_val = draw_point(event.getX(), event.getY(), event.getAction());
        invalidate();
        return ret_val;
    }

    private boolean draw_point(float touchX, float touchY, int eventAction)
    {
        try
        {
            JSONObject touch_point = new JSONObject();
            touch_point.put("x", touchX);
            touch_point.put("y", touchY);
            switch (eventAction) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    mDrawingPoints.put(new JSONArray());
                    ((JSONArray)mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    ((JSONArray)mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                    break;
                case MotionEvent.ACTION_UP:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    ((JSONArray)mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
        }
        catch(Exception e)
        {

        }
        return true;
    }

    public void clear_canvas()
    {
        //numberOfPoints = 0;
        mDrawingPoints = new JSONArray();
        canvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        canvasBitmap.eraseColor(backgroundColor);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void save_canvas_local(String filename)
    {
        DatabaseHelper.saveToastImage(filename, canvasBitmap, mDrawingPoints);
    }

    public void save_canvas_database(String filename)
    {
        String data = DatabaseHelper.packageImageInfo(canvasBitmap, mDrawingPoints);
    }

    public void setImage(String image_data)
    {
        try {
            JSONObject json = new JSONObject(image_data);
            mDrawingPoints = new JSONArray(json.getString("Points"));
            canvasBitmap = DatabaseHelper.base64DecodeBitmap(json.getString("Image"));
        }
        catch (Exception e)
        {

        }
    }
    public JSONArray getToastPoints()
    {
        JSONArray ret_val = null;
        try {
            ret_val = new JSONArray(mDrawingPoints.toString());
        }
        catch(Exception e)
        {

        }
        return ret_val;
    }
}

