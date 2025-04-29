package com.alim.greennote.ui.activity

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alim.greennote.R
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.databinding.ActivityDrawingBinding
import com.alim.greennote.di.Injection
import com.alim.greennote.ui.views.DrawingView
import com.google.android.material.slider.Slider
import com.nelu.ncbase.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrawingActivity : BaseActivity<ActivityDrawingBinding>(), DrawingView.DrawingStateListener {

    companion object {
        private const val PREFS_DRAWING = "drawing_prefs"
        private const val KEY_CURRENT_COLOR = "current_color"
        private const val KEY_CURRENT_BRUSH_SIZE = "current_brush_size"
        private const val KEY_TEMP_DRAWING_PATH = "temp_drawing_path"
        private const val ARG_TASK_ID = "task_id"
        private const val ARG_DRAWING_PATH = "drawing_path"
    }

    private var taskId: Long = 0
    private var drawingPath: String? = null
    private var currentColor = Color.BLACK
    private var currentBrushSize = 10f
    private lateinit var preferences: SharedPreferences

    private val hasUnsavedChanges: Boolean
        get() = binding.drawingView.hasDrawing()

    private val colorPalette = intArrayOf(
        Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
        Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.parseColor("#FF4500"),
        Color.parseColor("#800080"), Color.parseColor("#008080"),
        Color.parseColor("#A52A2A"), Color.parseColor("#000080")
    )

    private val brushSizes = floatArrayOf(5f, 10f, 15f, 20f, 25f, 30f)

    override fun ActivityDrawingBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()

        preferences = getSharedPreferences(PREFS_DRAWING, Context.MODE_PRIVATE)

        // Retrieve saved values
        currentColor = preferences.getInt(KEY_CURRENT_COLOR, Color.BLACK)
        currentBrushSize = preferences.getFloat(KEY_CURRENT_BRUSH_SIZE, 10f)

        // Get arguments
        taskId = intent.getLongExtra(ARG_TASK_ID, 0)
        drawingPath = intent.getStringExtra(ARG_DRAWING_PATH)

        // Setup back button
        back.setOnClickListener { onBackPressed() }

        // Setup drawing view
        drawingView.setDrawingStateListener(this@DrawingActivity)
        drawingView.setColor(currentColor)
        drawingView.setBrushSize(currentBrushSize)

        setupColorPalette()
        setupDrawingTools()
        updateUndoRedoButtons(false, false)

        // Load existing drawing if available
        if (drawingPath != null) {
            loadDrawingFromPath(drawingPath)
        } else {
            val tempPath = preferences.getString(KEY_TEMP_DRAWING_PATH, null)
            if (tempPath != null) {
                val tempFile = File(tempPath)
                if (tempFile.exists()) {
                    showRestoreDialog(tempFile)
                }
            }
        }
    }

    fun base64ToBitmap(base64String: String?): Bitmap {
        val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun showRestoreDialog(tempFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Restore Drawing")
            .setMessage("Would you like to restore your unsaved drawing?")
            .setPositiveButton("Yes") { _, _ ->
                try {
                    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                    binding.drawingView.loadFromBitmap(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to restore drawing", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No") { _, _ ->
                tempFile.delete()
                preferences.edit().remove(KEY_TEMP_DRAWING_PATH).apply()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupColorPalette() {
        val colorLayout = binding.llColors
        colorLayout.removeAllViews()

        for (color in colorPalette) {
            val colorView = FrameLayout(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.width = 80 // Width in pixels
            params.height = 80 // Height in pixels
            params.setMargins(8, 0, 8, 0)
            colorView.layoutParams = params

            // Set the drawable first
            val drawableId = if (color == currentColor) {
                R.drawable.color_selected_background
            } else {
                R.drawable.color_background
            }
            colorView.background = ContextCompat.getDrawable(this, drawableId)

            // Now apply the color as a tint instead of replacing the background
            val colorDrawable = colorView.background.mutate()
            DrawableCompat.setTint(colorDrawable, color)

            colorView.setOnClickListener {
                // Update selected color
                updateSelectedColor(colorView, color)

                // Switch back to brush mode if in eraser mode
                if (binding.btnEraser.isSelected) {
                    binding.btnEraser.isSelected = false
                    binding.btnBrush.isSelected = true
                    binding.drawingView.setErase(false)
                }

                binding.drawingView.setColor(color)
            }

            colorLayout.addView(colorView)

            if (preferences.getInt(KEY_CURRENT_COLOR, colorPalette[0]) == color) {
                updateSelectedColor(colorView, color)
            }
        }
    }


    private fun updateSelectedColor(selectedView: FrameLayout, newColor: Int) {
        currentColor = newColor

        for (i in 0 until binding.llColors.childCount) {
            val child = binding.llColors.getChildAt(i)
            val checkIcon = child.findViewWithTag<ImageView>("check_icon")

            checkIcon?.visibility = View.GONE
        }

        var checkIcon = selectedView.findViewWithTag<ImageView>("check_icon")

        if (checkIcon == null) {
            // If the check icon doesn't exist yet, create and add it dynamically
            checkIcon = ImageView(this).apply {
                tag = "check_icon" // Use tag to identify the check icon
                setImageResource(R.drawable.round_check_24) // The check mark drawable
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER // Center the check mark in the color circle
                }
            }
            selectedView.addView(checkIcon)
        }

        // Make sure the check icon is visible on the selected view
        checkIcon.visibility = View.VISIBLE

        // Save selected color
        preferences.edit().putInt(KEY_CURRENT_COLOR, newColor).apply()
    }

    private fun setupDrawingTools() {
        // Undo button
        binding.btnUndo.setOnClickListener { binding.drawingView.undo() }

        // Redo button
        binding.btnRedo.setOnClickListener { binding.drawingView.redo() }

        // Clear button
        binding.btnClear.setOnClickListener {
            showClearConfirmationDialog()
        }

        // Brush button
        binding.btnBrush.setOnClickListener {
            binding.btnBrush.isSelected = true
            binding.btnEraser.isSelected = false
            binding.drawingView.setErase(false)
            showBrushSizeDialog()
        }

        // Eraser button
        binding.btnEraser.setOnClickListener {
            binding.btnBrush.isSelected = false
            binding.btnEraser.isSelected = true
            binding.drawingView.setErase(true)
            showBrushSizeDialog()
        }

        // Save button
        binding.btnSave.setOnClickListener { saveDrawing() }

        // Initial selection state
        binding.btnBrush.isSelected = true
    }

    private fun showClearConfirmationDialog() {
        if (!binding.drawingView.hasDrawing()) return

        AlertDialog.Builder(this)
            .setTitle("Clear Drawing")
            .setMessage("Are you sure you want to clear the drawing?")
            .setPositiveButton("Yes") { _, _ ->
                binding.drawingView.clearDrawing()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showBrushSizeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_brush_size, null)
        val slider = dialogView.findViewById<Slider>(R.id.slider_brush_size)
        val brushPreview = dialogView.findViewById<View>(R.id.brush_preview)

        // Set initial values
        slider.value = currentBrushSize
        updateBrushPreview(brushPreview, currentBrushSize)

        // Set up slider listener
        slider.addOnChangeListener { _, value, _ ->
            currentBrushSize = value
            updateBrushPreview(brushPreview, value)
        }

        AlertDialog.Builder(this)
            .setTitle(/*if (binding.drawingView.setErase(false)) "Eraser Size" else*/ "Brush Size")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                binding.drawingView.setBrushSize(currentBrushSize)
                preferences.edit().putFloat(KEY_CURRENT_BRUSH_SIZE, currentBrushSize).apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBrushPreview(previewView: View, size: Float) {
        val params = previewView.layoutParams
        params.width = size.toInt() * 2
        params.height = size.toInt() * 2
        previewView.layoutParams = params

        if (binding.btnEraser.isSelected) {
            previewView.setBackgroundResource(R.drawable.eraser_preview)
        } else {
            previewView.setBackgroundColor(currentColor)
        }
    }

    private fun loadDrawingFromPath(path: String?) {
        if (path == null) return

        try {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                binding.drawingView.loadFromBitmap(bitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load drawing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDrawing() {
        try {
            // Create directory if it doesn't exist
            val storageDir = File(getExternalFilesDir(null), "drawings")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            // Generate unique filename based on timestamp

            // Save the drawing to file
            val filePath = binding.drawingView.saveToFile(storageDir, "${System.currentTimeMillis()}.png")

            CoroutineScope(Dispatchers.IO).launch {
                Injection.drawingDao.insertDrawing(
                    DrawingEntity(
                        drawingData = binding.drawingView.getImageBase()
                    )
                )
            }

            // Return the file path to the calling activity
            val returnIntent = intent
            returnIntent.putExtra(ARG_DRAWING_PATH, filePath)
            setResult(RESULT_OK, returnIntent)

            // Clear temp drawing
            clearTempDrawing()

            Toast.makeText(this, "Drawing saved", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save drawing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveTempDrawing() {
        if (!hasUnsavedChanges) return

        try {
            val cacheDir = cacheDir
            val tempFile = File(cacheDir, "temp_drawing.png")

            FileOutputStream(tempFile).use { out ->
                binding.drawingView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            preferences.edit()
                .putString(KEY_TEMP_DRAWING_PATH, tempFile.absolutePath)
                .apply()
        } catch (e: Exception) {
            // Silently fail - this is just a temporary backup
        }
    }

    private fun clearTempDrawing() {
        val tempPath = preferences.getString(KEY_TEMP_DRAWING_PATH, null)
        if (tempPath != null) {
            val tempFile = File(tempPath)
            if (tempFile.exists()) {
                tempFile.delete()
            }
            preferences.edit().remove(KEY_TEMP_DRAWING_PATH).apply()
        }
    }

    private fun initSystemPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.status)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    override fun onDrawingChanged() {
        // Save current state temporarily
        saveTempDrawing()
    }

    override fun onUndoRedoStateChanged(canUndo: Boolean, canRedo: Boolean) {
        updateUndoRedoButtons(canUndo, canRedo)
    }

    private fun updateUndoRedoButtons(canUndo: Boolean, canRedo: Boolean) {
        binding.btnUndo.isEnabled = canUndo
        binding.btnUndo.alpha = if (canUndo) 1.0f else 0.5f

        binding.btnRedo.isEnabled = canRedo
        binding.btnRedo.alpha = if (canRedo) 1.0f else 0.5f
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("Do you want to save your drawing before exiting?")
            .setPositiveButton("Save") { _, _ -> saveDrawing() }
            .setNegativeButton("Discard") { _, _ ->
                clearTempDrawing()
                super.onBackPressed()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        // Save current state to restore later if needed
        saveTempDrawing()
    }
}