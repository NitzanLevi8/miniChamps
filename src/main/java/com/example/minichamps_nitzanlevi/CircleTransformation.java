package com.example.minichamps_nitzanlevi;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class CircleTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        // Get the smallest dimension to make a square bitmap
        int size = Math.min(source.getWidth(), source.getHeight());

        // Calculate the starting point for cropping
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // Create a new Bitmap with a square size
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle(); // Recycle the original bitmap if it's not used anymore
        }

        // Create a new Bitmap with a circular shape
        Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        // Calculate the radius for the circle
        float radius = size / 2f;

        // Draw the circular bitmap
        canvas.drawCircle(radius, radius, radius, paint);

        squaredBitmap.recycle(); // Recycle the squared bitmap

        return circularBitmap;
    }

    @Override
    public String key() {
        return "circle()";
    }
}