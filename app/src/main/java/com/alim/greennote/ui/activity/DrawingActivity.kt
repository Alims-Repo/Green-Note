package com.alim.greennote.ui.activity

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alim.greennote.R
import com.alim.greennote.databinding.ActivityDrawingBinding
import com.nelu.ncbase.base.BaseActivity

class DrawingActivity : BaseActivity<ActivityDrawingBinding>() {

    companion object {
        private const val currentBrushSize = 1
        private const val ARG_TASK_ID: String = "task_id"
        private const val ARG_DRAWING_PATH: String = "drawing_path"
    }

    private var taskId: Long = 0
    private var drawingPath: String? = null

    private val colorPalette = intArrayOf(
        Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
        Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.DKGRAY
    )

    private val brushSizes = floatArrayOf(5f, 10f, 15f, 20f, 25f)

    private var saveListener: OnDrawingSaveListener? = null

    interface OnDrawingSaveListener {
        fun onDrawingSaved(filePath: String?)
    }

    override fun ActivityDrawingBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()

        back.back()

        //        drawingViewModel = new ViewModelProvider(requireActivity()).get(DrawingViewModel.class);
        setupColorPalette()
        setupDrawingTools()


        // Load existing drawing if available
        if (drawingPath != null) {
            loadDrawing(drawingPath)
        }
    }

    private fun setupColorPalette() {
        val colorLayout = binding.llColors

        for (color in colorPalette) {
            val colorView = View(this)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.width = 80 // Width in pixels
            params.setMargins(5, 0, 5, 0)
            colorView.setLayoutParams(params)
            colorView.setBackgroundColor(color)

            colorView.setOnClickListener(View.OnClickListener { v: View? ->
                binding.drawingView.setColor(
                    color
                )
            })

            colorLayout.addView(colorView)
        }
    }

    private fun setupDrawingTools() {
        // Undo button
        binding.btnUndo.setOnClickListener({ v -> binding.drawingView.undo() })

        // Redo button
        binding.btnRedo.setOnClickListener({ v -> binding.drawingView.redo() })

        // Clear button
        binding.btnClear.setOnClickListener({ v ->
            // Show confirmation dialog (implementation omitted)
            binding.drawingView.clearDrawing()
        })

        // Brush button (with size selector)
        binding.btnBrush.setOnClickListener({ v ->
            binding.drawingView.setErase(false)
            showBrushSizeDialog()
        })

        // Eraser button
        binding.btnEraser.setOnClickListener({ v ->
            binding.drawingView.setErase(true)
            showBrushSizeDialog()
        })

        // Save button
//        binding.btnSave.setOnClickListener(v -> saveDrawing());
    }

    private fun showBrushSizeDialog() {
        // Dialog implementation omitted for brevity
        // This would show a dialog with different brush sizes to choose from
        binding.drawingView.setBrushSize(brushSizes[currentBrushSize])
    }

    private fun loadDrawing(path: String?) {
        // Implementation to load the drawing from a file path
        // Would use BitmapFactory to decode the file and set it to the drawing view
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
}