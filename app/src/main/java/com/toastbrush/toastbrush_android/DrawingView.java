package com.toastbrush.toastbrush_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.toastbrush.ToastbrushApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class DrawingView extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private static int paintColor = Color.parseColor("#f5ca62");
    private static int backgroundColor = Color.parseColor("#ffecc0");
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private JSONArray mDrawingPoints;

    private int mWidth;
    private int mHeight;
    private boolean line_started;
    private float prev_x;
    private float prev_y;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
        //restoreState();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        saveState();
        return super.onSaveInstanceState();
    }

    private void saveState()
    {
        if(canvasBitmap != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            DatabaseHelper.saveToFile(getRestoreBitmapPath(), bytes.toByteArray());
        }
        if(mDrawingPoints != null) {
            DatabaseHelper.saveToFile(getRestorePointsPath(), mDrawingPoints.toString().getBytes());
        }
    }
/*
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        restoreState();
        super.onRestoreInstanceState(state);
    }
*/
    private void restoreState() {
        if(canvasBitmap == null) {
            try {
                byte[] bitmapBytes = DatabaseHelper.readInFile(getRestoreBitmapPath());
                byte[] ptsBytes = DatabaseHelper.readInFile(getRestorePointsPath());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                canvasBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);
                mDrawingPoints = new JSONArray(ptsBytes);
            } catch (Exception e) {
                Log.e("TESTING", "Exception thrown when unpackaging state:\n" + e.toString());
                canvasBitmap = null;
                mDrawingPoints = null;
            }
        }
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
        boolean line_started = false;
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
            canvasBitmap = Bitmap.createScaledBitmap(canvasBitmap, mWidth, mHeight, false);
        }
        drawPaint.setStrokeWidth((4*mWidth)/100);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);    }

    @SuppressLint("ClickableViewAccessibility")
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
            float updated_x = touchX * ((float)255.0/mWidth);
            float updated_y = touchY * ((float)255.0/mHeight);
            touch_point.put("x", updated_x);
            touch_point.put("y", updated_y);

            switch (eventAction) {
                case MotionEvent.ACTION_DOWN:
                    line_started = true;
                    drawPath.moveTo(touchX, touchY);
                    mDrawingPoints.put(new JSONArray());
                    ((JSONArray) mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(line_started)
                    {
                        if (!(updated_x > 255 || updated_x < 0 || updated_y > 255 || updated_y < 0)) {
                            drawPath.lineTo(touchX, touchY);
                            ((JSONArray) mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                            break;
                        }
                    }
                    else if(!(updated_x > 255 || updated_x < 0 || updated_y > 255 || updated_y < 0))
                    {
                        float updated_prevX = prev_x * (float)(255.0/mWidth);
                        float updated_prevY = prev_y * (float)(255.0/mWidth);
                        if(updated_prevX > 255)
                        {
                            updated_prevX = 255;
                        }
                        if (updated_prevX < 0) {
                            updated_prevX = 0;
                        }
                        if (updated_prevY > 255) {
                            updated_prevY = 255;
                        }
                        if (updated_prevY < 0) {
                            updated_prevY = 0;
                        }
                        line_started = true;
                        JSONObject prev_touch_point = new JSONObject();
                        prev_touch_point.put("x", updated_prevX);
                        prev_touch_point.put("y", updated_prevY);
                        drawPath.moveTo(prev_x, prev_y);
                        mDrawingPoints.put(new JSONArray());
                        ((JSONArray) mDrawingPoints.get(mDrawingPoints.length() - 1)).put(prev_touch_point);

                        drawPath.lineTo(touchX, touchY);
                        ((JSONArray) mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                        break;
                    }
                case MotionEvent.ACTION_UP:
                    if(line_started) {
                        if (updated_x > 255) {
                            updated_x = 255;
                        }
                        if (updated_x < 0) {
                            updated_x = 0;
                        }
                        if (updated_y > 255) {
                            updated_y = 255;
                        }
                        if (updated_y < 0) {
                            updated_y = 0;
                        }
                        drawPath.lineTo(touchX, touchY);
                        drawCanvas.drawPath(drawPath, drawPaint);
                        ((JSONArray) mDrawingPoints.get(mDrawingPoints.length() - 1)).put(touch_point);
                        drawPath.reset();
                        line_started = false;
                    }
                    break;
                default:
                    return false;
            }
            prev_x = touchX;
            prev_y = touchY;
        }
        catch(Exception e)
        {
            Log.e("TESTING", "Exception in draw_point: " + e.getMessage());
        }
        return true;
    }

    public void clear_canvas()
    {
        mDrawingPoints = new JSONArray();
        canvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        canvasBitmap.eraseColor(backgroundColor);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void save_canvas_local(String filename, String description)
    {
        DatabaseHelper.saveToastImage(filename, description, canvasBitmap, mDrawingPoints);
    }

    public void save_canvas_database(String filename, String user, String description)
    {
        String data = DatabaseHelper.packageImageInfo(canvasBitmap, mDrawingPoints);
        ToastbrushWebAPI.sendImage(filename, user, description, data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if(json.getBoolean("success"))
                    {
                        Toast.makeText(ToastbrushApplication.getAppContext(), "Successfully saved to database", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ToastbrushApplication.getAppContext(), "Error saving to database", Toast.LENGTH_SHORT).show();

                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(ToastbrushApplication.getAppContext(), "Exception parsing server response:\n" + response, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setImage(String image_data)
    {
        try {
            JSONObject json = new JSONObject(image_data);
            mDrawingPoints = new JSONArray(json.getString("Points"));
            canvasBitmap = DatabaseHelper.base64DecodeBitmap(json.getString("Image"));
            Log.d("TESTING", "Width: " + canvasBitmap.getWidth() + "\tHeight: " + canvasBitmap.getHeight());

        }
        catch (Exception e)
        {
            Log.e("TESTING", "Exception in setImage: " + e.getMessage());
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
            Log.e("TESTING", "Exception in getToastPoints: " + e.getMessage());
        }
        return ret_val;
    }

    public static Bitmap getBlankImage()
    {
        Bitmap ret_val = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        ret_val.eraseColor(backgroundColor);
        return ret_val;
    }

    private static String getRestoreBitmapPath()
    {
        return ToastbrushApplication.getAppContext().getFilesDir() + "restore_bitmap.png";
    }

    private static String getRestorePointsPath()
    {
        return ToastbrushApplication.getAppContext().getFilesDir() + "restore_points.pnts";
    }
}

