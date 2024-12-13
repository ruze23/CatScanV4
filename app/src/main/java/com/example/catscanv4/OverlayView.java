package com.example.catscanv4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class OverlayView extends View {
    private List<BoundingBox> results;
    private Paint BoxPaint;
    private Paint textBgPaint;
    private Paint textPaint;

    private final Rect bounds = new Rect();

    private static final int BOUNDING_RECT_TEXT_PADDING = 5;

    public OverlayView(Context context, AttributeSet attrs){
        super(context, attrs);
        initPaints(context);

    }
    private void initPaints(Context context){
        textBgPaint = new Paint();
        textBgPaint.setColor(Color.BLACK);
        textBgPaint.setStyle(Paint.Style.FILL);
        textBgPaint.setTextSize(30f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(30f);

        BoxPaint = new Paint();
        BoxPaint.setStrokeWidth(8F);
        BoxPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if(results != null){
            for (BoundingBox result : results){
                float left = result.getX1() * getWidth();
                float top = result.getY1() * getHeight();
                float right = result.getX2() * getWidth();
                float bottom = result.getY2() * getHeight();

                String className = result.getClsName();

                switch (className) {
                    case "Healthy":
                        BoxPaint.setColor(Color.GREEN);
                        break;
                    case "Ringworm":
                        BoxPaint.setColor(Color.RED);
                        break;
                    case "Mange":
                        BoxPaint.setColor(Color.YELLOW);
                        break;
                }

                canvas.drawRect(left, top, right, bottom, BoxPaint);

                float confVal = result.getCnf();
                String combinedText  = className + " " + String.format("%.2f", confVal);


                textBgPaint.getTextBounds(combinedText,0,combinedText.length(),bounds);
                int textWidth = bounds.width();
                int textHeight = bounds.height();

                canvas.drawRect(left,
                        top - textHeight - BOUNDING_RECT_TEXT_PADDING,
                        left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                        top, textBgPaint);
                canvas.drawText(combinedText,left,top - BOUNDING_RECT_TEXT_PADDING
                        ,textPaint);
            }
        }
    }

    public void setResults(List<BoundingBox> boundingBoxes){
        this.results = boundingBoxes;
        invalidate();
    }

    public void Clear(){
        results = null;
        invalidate();
        initPaints(getContext());
    }
}