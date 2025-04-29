package com.alim.greennote.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // Drawing state
    private var drawPath = Path()
    private lateinit var drawPaint: Paint
    private lateinit var canvasPaint: Paint
    private var paintColor = Color.BLACK
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var brushSize: Float = 10f
    private var erase = false
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private val TOUCH_TOLERANCE = 4f

    // Stroke history for undo/redo
    class DrawingAction(
        val path: Path,
        val paint: Paint,
        val isErase: Boolean
    ) {
        var points = ArrayList<PathPoint>()
    }

    // Make PathPoint class for serialization
    class PathPoint(
        val x: Float,
        val y: Float,
        val isQuadTo: Boolean // true if this is a quadratic curve point
    )

    // Make the actions list public so it can be accessed for serialization
    val actions = ArrayList<DrawingAction>()
    private val undoneActions = ArrayList<DrawingAction>()
    private var stateListener: DrawingStateListener? = null

    // Current points for the active stroke
    private val currentPoints = ArrayList<PathPoint>()

    // Interface for state change notifications
    interface DrawingStateListener {
        fun onDrawingChanged()
        fun onUndoRedoStateChanged(canUndo: Boolean, canRedo: Boolean)
    }

    init {
        setupDrawing()
    }

    // Method to set actions from loaded data
    fun setActions(loadedActions: List<DrawingAction>) {
        // Clear current drawing
        actions.clear()
        undoneActions.clear()

        // Add loaded actions
        actions.addAll(loadedActions)

        // Redraw
        redrawAll()
        updateUndoRedoState()
        notifyDrawingChanged()
    }

    fun setDrawingStateListener(listener: DrawingStateListener) {
        this.stateListener = listener
    }

    private fun setupDrawing() {
        brushSize = 10f
        drawPath = Path()
        drawPaint = Paint().apply {
            color = paintColor
            isAntiAlias = true
            strokeWidth = brushSize
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        // Canvas paint for bitmap drawing
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Create bitmap and canvas only if they don't exist or size changed
        if (!::canvasBitmap.isInitialized || w != oldw || h != oldh) {
            if (::canvasBitmap.isInitialized) {
                canvasBitmap.recycle() // Properly recycle old bitmap
            }
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap)
            drawCanvas.drawColor(Color.WHITE) // Set initial background
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start a new line
                touchStart(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // Draw the line
                touchMove(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // Save line and prepare for next one
                touchUp()
                invalidate()
            }
            else -> return false
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        // Clear redo history when starting new drawing
        if (undoneActions.isNotEmpty()) {
            undoneActions.clear()
            updateUndoRedoState()
        }

        // Start new path
        drawPath.reset()
        drawPath.moveTo(x, y)
        lastTouchX = x
        lastTouchY = y
    }

    private fun touchMove(x: Float, y: Float) {
        // Only draw if moved far enough (prevents small unwanted dots)
        val dx = Math.abs(x - lastTouchX)
        val dy = Math.abs(y - lastTouchY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // Create a smooth curve through points
            drawPath.quadTo(lastTouchX, lastTouchY, (x + lastTouchX)/2, (y + lastTouchY)/2)
            lastTouchX = x
            lastTouchY = y
        }
    }

    private fun touchUp() {
        drawPath.lineTo(lastTouchX, lastTouchY)
        drawCanvas.drawPath(drawPath, drawPaint)

        // Save action for undo
        val savedPaint = Paint(drawPaint)
        val savedPath = Path(drawPath)
        actions.add(DrawingAction(savedPath, savedPaint, erase))

        // Reset path for next drawing
        drawPath.reset()

        // Notify state changes
        updateUndoRedoState()
        notifyDrawingChanged()
    }

    fun setColor(newColor: Int) {
        paintColor = newColor
        if (!erase) {
            drawPaint.color = newColor
            drawPaint.xfermode = null
        }
    }

    fun setBrushSize(newSize: Float) {
        brushSize = newSize
        drawPaint.strokeWidth = newSize
    }

    fun setErase(isErase: Boolean) {
        erase = isErase
        if (erase) {
            // Use clear paint mode for transparent eraser effect
            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            drawPaint.xfermode = null
            drawPaint.color = paintColor
        }
    }

    fun clearDrawing() {
        drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
        drawCanvas.drawColor(Color.WHITE)
        actions.clear()
        undoneActions.clear()
        updateUndoRedoState()
        notifyDrawingChanged()
        invalidate()
    }

    fun undo() {
        if (actions.isNotEmpty()) {
            val undoneAction = actions.removeAt(actions.size - 1)
            undoneActions.add(undoneAction)
            redrawAll()
            updateUndoRedoState()
            notifyDrawingChanged()
        }
    }

    fun redo() {
        if (undoneActions.isNotEmpty()) {
            val redoneAction = undoneActions.removeAt(undoneActions.size - 1)
            actions.add(redoneAction)
            redrawAll()
            updateUndoRedoState()
            notifyDrawingChanged()
        }
    }

    private fun redrawAll() {
        // Clear canvas and redraw all actions
        canvasBitmap.eraseColor(Color.WHITE)

        for (action in actions) {
            drawCanvas.drawPath(action.path, action.paint)
        }

        invalidate()
    }

    private fun updateUndoRedoState() {
        stateListener?.onUndoRedoStateChanged(actions.isNotEmpty(), undoneActions.isNotEmpty())
    }

    private fun notifyDrawingChanged() {
        stateListener?.onDrawingChanged()
    }

    fun getBitmap(): Bitmap = canvasBitmap

    fun hasDrawing(): Boolean = actions.isNotEmpty()

    fun canUndo(): Boolean = actions.isNotEmpty()

    fun canRedo(): Boolean = undoneActions.isNotEmpty()

    fun getImageBase(): String {
        val outputStream = ByteArrayOutputStream()
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    @Throws(IOException::class)
    fun saveToFile(directory: File, filename: String): String {
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)
        val fos = FileOutputStream(file)
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        return file.absolutePath
    }

    fun loadFromActions(ac: List<DrawingAction>) {
        actions.clear()
        actions.addAll(ac)
        updateUndoRedoState()
        notifyDrawingChanged()
        invalidate()
    }

    fun loadFromBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            val width = width
            val height = height

            Log.e("BITMAP", width.toString())

            if (width > 0 && height > 0) {
                val scaledBitmap = Bitmap.createScaledBitmap(it, width, height, true)

                actions.clear()
                undoneActions.clear()

                drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
                drawCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)

                if (bitmap != scaledBitmap) {
                    scaledBitmap.recycle()
                }

                updateUndoRedoState()
                notifyDrawingChanged()
                invalidate()
            }
        }
    }
}