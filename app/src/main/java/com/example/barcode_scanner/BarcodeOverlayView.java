package com.example.barcode_scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.ArrayList;
import java.util.List;

public class BarcodeOverlayView extends View {

    private final Paint boxPaint = new Paint();
    private List<Barcode> barcodes = new ArrayList<>();
    private int imageWidth = 0;
    private int imageHeight = 0;

    public BarcodeOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8f);
    }

    // Receive barcodes + image size
    public void setBarcodes(List<Barcode> barcodes, int imageWidth, int imageHeight) {
        if (barcodes == null) return;
        this.barcodes = barcodes;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        postInvalidate(); // redraw overlay
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageWidth == 0 || imageHeight == 0 || barcodes.isEmpty()) return;

        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;

        for (Barcode barcode : barcodes) {
            if (barcode.getBoundingBox() != null) {
                RectF rectF = new RectF(barcode.getBoundingBox());

                // scale coordinates to view
                rectF.left *= scaleX;
                rectF.top *= scaleY;
                rectF.right *= scaleX;
                rectF.bottom *= scaleY;

                canvas.drawRect(rectF, boxPaint);
            }
        }
    }
}
