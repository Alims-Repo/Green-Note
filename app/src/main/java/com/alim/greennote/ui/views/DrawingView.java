package com.alim.greennote.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    // Drawing state
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.BLACK;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize;
    private boolean erase = false;
    private float lastTouchX, lastTouchY;
    private static final float TOUCH_TOLERANCE = 4;

    // Stroke history for undo/redo
    private static class DrawingAction {
        Path path;
        Paint paint;
        boolean isErase;

        DrawingAction(Path path, Paint paint, boolean isErase) {
            this.path = path;
            this.paint = paint;
            this.isErase = isErase;
        }
    }

    private List<DrawingAction> actions = new ArrayList<>();
    private List<DrawingAction> undoneActions = new ArrayList<>();
    private DrawingStateListener stateListener;

    // Interface for state change notifications
    public interface DrawingStateListener {
        void onDrawingChanged();
        void onUndoRedoStateChanged(boolean canUndo, boolean canRedo);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public void setDrawingStateListener(DrawingStateListener listener) {
        this.stateListener = listener;
    }

    private void setupDrawing() {
        brushSize = 10;
        drawPath = new Path();
        drawPaint = new Paint();

        // Better quality paint settings
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        // Canvas paint for bitmap drawing
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Create bitmap and canvas only if they don't exist or size changed
        if (canvasBitmap == null || w != oldw || h != oldh) {
            if (canvasBitmap != null) {
                canvasBitmap.recycle(); // Properly recycle old bitmap
            }
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            drawCanvas.drawColor(Color.WHITE); // Set initial background
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Start a new line
                touchStart(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // Draw the line
                touchMove(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // Save line and prepare for next one
                touchUp();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private void touchStart(float x, float y) {
        // Clear redo history when starting new drawing
        if (!undoneActions.isEmpty()) {
            undoneActions.clear();
            updateUndoRedoState();
        }

        // Start new path
        drawPath.reset();
        drawPath.moveTo(x, y);
        lastTouchX = x;
        lastTouchY = y;
    }

    private void touchMove(float x, float y) {
        // Only draw if moved far enough (prevents small unwanted dots)
        float dx = Math.abs(x - lastTouchX);
        float dy = Math.abs(y - lastTouchY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // Create a smooth curve through points
            drawPath.quadTo(lastTouchX, lastTouchY, (x + lastTouchX)/2, (y + lastTouchY)/2);
            lastTouchX = x;
            lastTouchY = y;
        }
    }

    private void touchUp() {
        drawPath.lineTo(lastTouchX, lastTouchY);
        drawCanvas.drawPath(drawPath, drawPaint);

        // Save action for undo
        Paint savedPaint = new Paint(drawPaint);
        Path savedPath = new Path(drawPath);
        actions.add(new DrawingAction(savedPath, savedPaint, erase));

        // Reset path for next drawing
        drawPath.reset();

        // Notify state changes
        updateUndoRedoState();
        notifyDrawingChanged();
    }

    public void setColor(int newColor) {
        paintColor = newColor;
        if (!erase) {
            drawPaint.setColor(newColor);
            drawPaint.setXfermode(null);
        }
    }

    public void setBrushSize(float newSize) {
        brushSize = newSize;
        drawPaint.setStrokeWidth(newSize);
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            // Use clear paint mode for transparent eraser effect
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
            drawPaint.setColor(paintColor);
        }
    }

    public void clearDrawing() {
        drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        drawCanvas.drawColor(Color.WHITE);
        actions.clear();
        undoneActions.clear();
        updateUndoRedoState();
        notifyDrawingChanged();
        invalidate();
    }

    public void undo() {
        if (!actions.isEmpty()) {
            DrawingAction undoneAction = actions.remove(actions.size() - 1);
            undoneActions.add(undoneAction);
            redrawAll();
            updateUndoRedoState();
            notifyDrawingChanged();
        }
    }

    public void redo() {
        if (!undoneActions.isEmpty()) {
            DrawingAction redoneAction = undoneActions.remove(undoneActions.size() - 1);
            actions.add(redoneAction);
            redrawAll();
            updateUndoRedoState();
            notifyDrawingChanged();
        }
    }

    private void redrawAll() {
        // Clear canvas and redraw all actions
        canvasBitmap.eraseColor(Color.WHITE);

        for (DrawingAction action : actions) {
            drawCanvas.drawPath(action.path, action.paint);
        }

        invalidate();
    }

    private void updateUndoRedoState() {
        if (stateListener != null) {
            stateListener.onUndoRedoStateChanged(!actions.isEmpty(), !undoneActions.isEmpty());
        }
    }

    private void notifyDrawingChanged() {
        if (stateListener != null) {
            stateListener.onDrawingChanged();
        }
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }

    public boolean hasDrawing() {
        return !actions.isEmpty();
    }

    public boolean canUndo() {
        return !actions.isEmpty();
    }

    public boolean canRedo() {
        return !undoneActions.isEmpty();
    }

    // Method to save drawing to a file
    public String saveToFile(File directory, String filename) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, filename);
        FileOutputStream fos = new FileOutputStream(file);
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();

        return file.getAbsolutePath();
    }

    // Method to load drawing from a bitmap
    public void loadFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int width = getWidth();
            int height = getHeight();

            if (width > 0 && height > 0) {
                // Scale bitmap to fit view dimensions
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

                // Clear current drawing
                actions.clear();
                undoneActions.clear();

                // Draw loaded bitmap onto canvas
                drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
                drawCanvas.drawBitmap(scaledBitmap, 0, 0, null);

                if (!bitmap.equals(scaledBitmap)) {
                    scaledBitmap.recycle();
                }

                updateUndoRedoState();
                notifyDrawingChanged();
                invalidate();
            }
        }
    }
}