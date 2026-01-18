package com.example.barcode_scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarcodeOverlayView extends View {

    private final Paint boxPaint = new Paint();
    private List<RectF> rects = new ArrayList<>();

    public BarcodeOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8f);
    }

    // Receive already mapped rectangles
    public void setMappedRects(List<RectF> mappedRects) {
        if (mappedRects == null) return;
        this.rects = mappedRects;
        postInvalidate(); // redraw overlay
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF rectF : rects) {
            canvas.drawRect(rectF, boxPaint);
        }
    }
}
