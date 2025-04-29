package com.alim.greennote.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.alim.greennote.R
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ActivityAddTaskBinding
import com.alim.greennote.di.Injection
import com.alim.greennote.utils.scheduleNotification
import com.nelu.ncbase.base.BaseActivity
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : BaseActivity<ActivityAddTaskBinding>() {

    private var selectedDateMillis: Long = System.currentTimeMillis()

    private var selectedColor: Int = R.color.colorPrimary

    override fun ActivityAddTaskBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()

        back.back()

        val categories = listOf("Personal", "Work", "School", "Leisure")
        spinnerCategory.adapter = ArrayAdapter(this@AddTaskActivity, android.R.layout.simple_spinner_dropdown_item, categories)

        setupColorOptions()

        btnDueDate.setOnClickListener {
            showDateTimePicker()
        }

        save.setOnClickListener {
            saveTask()
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

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString()
        val description = binding.etTaskDescription.text.toString()
        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            R.id.rb_priority_low -> "Low"
            R.id.rb_priority_medium -> "Medium"
            R.id.rb_priority_high -> "High"
            else -> "Medium"
        }
        val category = binding.spinnerCategory.selectedItem.toString()
        val autoArchiveDays = binding.etAutoArchive.text.toString().toIntOrNull() ?: 0

        if (title.isBlank()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val task = ModelTask(
            title = title,
            description = description,
            dueDateMillis = selectedDateMillis,
            priority = priority,
            category = category,
            color = selectedColor,
            autoArchiveDays = autoArchiveDays
        )

        scheduleNotification(task)

        Injection.taskDao.insertTask(task)
        Toast.makeText(this@AddTaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupColorOptions() {
        colors.forEach { colorResId ->
            // Create a FrameLayout to wrap each color circle and check icon
            val colorFrame = FrameLayout(this).apply {
                val size = resources.getDimensionPixelSize(R.dimen.color_circle_size)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(8, 8, 8, 8)
                }

                // Create the color circle view
                val colorView = View(this@AddTaskActivity).apply {
                    setBackgroundResource(R.drawable.circle_background)
                    background.setTint(resources.getColor(colorResId, null))
                }
                addView(colorView)

                // Set the click listener to highlight the selected color and show the check mark
                colorView.setOnClickListener {
                    selectedColor = resources.getColor(colorResId, null)
                    highlightSelectedColor(this) // `this` is the FrameLayout
                }
            }

            // Add the FrameLayout (color circle with the check icon) to the layout
            binding.llColors.addView(colorFrame)
        }
    }

    private fun highlightSelectedColor(selectedView: FrameLayout) {
        // Loop through all color views and remove the check mark from them
        for (i in 0 until binding.llColors.childCount) {
            val child = binding.llColors.getChildAt(i)
            val checkIcon = child.findViewWithTag<ImageView>("check_icon")

            // Hide the check icon on all color views
            checkIcon?.visibility = View.GONE
        }

        // Add the check icon to the selected color view
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
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Show the DatePickerDialog first
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Set the date based on user selection
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Now show the TimePickerDialog to pick the time
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        // Show the TimePickerDialog
        val timePicker = android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Set the time based on user selection
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Combine the selected date and time into milliseconds
                selectedDateMillis = calendar.timeInMillis

                // Format and set the combined date and time on the button
                val sdf = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
                binding.btnDueDate.text = sdf.format(Date(selectedDateMillis))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // false for 24-hour format, true for 12-hour format
        )
        timePicker.show()
    }

    val colors = listOf(
        R.color.colorPrimary,
        android.R.color.holo_red_light,
        android.R.color.holo_green_light,
        android.R.color.holo_blue_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_purple,
        android.R.color.holo_red_dark,
        android.R.color.holo_green_dark,
        android.R.color.holo_blue_dark,
        android.R.color.holo_orange_dark,
        android.R.color.holo_purple,
        android.R.color.white,
        android.R.color.black,
        android.R.color.darker_gray
    )
}