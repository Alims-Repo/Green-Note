package com.alim.greennote.ui.activity

import android.animation.Animator
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.alim.greennote.R
import com.alim.greennote.data.Dummy
import com.alim.greennote.data.model.ModelId
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ActivityMainBinding
import com.alim.greennote.di.Injection
import com.alim.greennote.ui.adapter.AdapterNotes
import com.alim.greennote.viewModel.ViewModelHome
import com.nelu.ncbase.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var isFabMenuOpen = false

    private val viewModel: ViewModelHome by viewModels()

    private val adapterNotes by lazy { AdapterNotes() }

    override fun ActivityMainBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()
        initViews()
        initObserver()
    }

    private fun initSystemPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
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

    private fun ActivityMainBinding.initViews() {
        recycler.adapter = adapterNotes

        clear.setOnClickListener { edit.setText("") }

        edit.doAfterTextChanged {
            viewModel.query(it.toString())
            clear.isVisible = it?.isNotEmpty() == true
        }

        fabMain.setOnClickListener {
            toggleFabMenu()
        }

        fabAddNote.setOnClickListener {
            toggleFabMenu()
            startActivity(
                Intent(
                    this@MainActivity,
                    AddNoteActivity::class.java
                )
            )
        }

        fabAddDrawing.setOnClickListener {
            toggleFabMenu()
            startActivity(
                Intent(
                    this@MainActivity,
                    DrawingActivity::class.java
                )
            )
        }

        fabAddTask.setOnClickListener {
            toggleFabMenu()
            startActivity(
                Intent(
                    this@MainActivity,
                    AddTaskActivity::class.java
                )
            )
        }
    }

    private fun ActivityMainBinding.toggleFabMenu() {
        if (isFabMenuOpen) {
            fabMain.animate().rotation(0F).scaleY(1F).scaleX(1F).start()

            fabAddNote.animate().translationY(0F).alpha(0F).scaleX(0F).setListener(
                SimpleAnimatorListener(fabAddNote)
            ).start()
            fabAddDrawing.animate().translationY(0F).translationX(0F)
                .scaleX(0F)
                .alpha(0F).setListener(
                SimpleAnimatorListener(fabAddTask)
            ).start()
            fabAddTask.animate().translationX(0F).alpha(0F)
                .scaleX(0F)
                .setListener(
                    SimpleAnimatorListener(fabAddTask)
            ).start()
        } else {
            fabMain.animate().rotation(45F).scaleY(0.75F).scaleX(0.75F).start()

            fabAddNote.isVisible = true
            fabAddTask.isVisible = true
            fabAddDrawing.isVisible = true

            fabAddNote.animate().translationY(-200F)
                .scaleX(0.85F)
                .scaleY(0.85F)
                .alpha(1F).start()
            fabAddDrawing.animate().translationY(-140F)
                .translationX(-140F)
                .scaleX(0.85F)
                .scaleY(0.85F)
                .alpha(1F).start()
            fabAddTask.animate().translationX(-200F)
                .scaleX(0.85F)
                .scaleY(0.85F)
                .alpha(1F).start()
        }
        isFabMenuOpen = !isFabMenuOpen
    }

    private fun initObserver() {
        viewModel.allNotes.observe(this) {
            val temp = ArrayList<ModelId>()
            temp.addAll(it)
            temp.addAll(Injection.drawingDao.getDrawings())
            adapterNotes.update(temp.sortedByDescending { c -> c.createdAt })
        }

        viewModel.allDrawings.observe(this) {
            val temp = ArrayList<ModelId>()
            temp.addAll(it)
            temp.addAll(Injection.taskDao.getAllTasks())
            adapterNotes.update(temp.sortedByDescending { c -> c.createdAt })
        }
    }

    open inner class SimpleAnimatorListener(
        private val view: View
    ) : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {
            println("Animation started")
        }

        override fun onAnimationEnd(animation: Animator) {
            view.isVisible = isFabMenuOpen
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    }
}