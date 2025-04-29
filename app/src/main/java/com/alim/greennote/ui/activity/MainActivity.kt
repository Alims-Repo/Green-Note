package com.alim.greennote.ui.activity

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.alim.greennote.R
import com.alim.greennote.data.Dummy
import com.alim.greennote.data.model.ModelId
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ActivityMainBinding
import com.alim.greennote.di.Injection
import com.alim.greennote.ui.adapter.AdapterNotes
import com.alim.greennote.viewModel.ViewModelHome
import com.nelu.ncbase.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var query = ArrayList<ModelId>()

    private var isFabMenuOpen = false

    private val viewModel: ViewModelHome by viewModels()

    private val adapterNotes by lazy { AdapterNotes() }

    override fun ActivityMainBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()
        initViews()
        initObserver()

        initPermission()
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
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
        recycler.layoutManager = LinearLayoutManager(this@MainActivity)
        recycler.adapter = adapterNotes

        clear.setOnClickListener { edit.setText("") }

        edit.doAfterTextChanged { inp->
            manage(binding.chipGroup.checkedChipId, inp.toString())
            clear.isVisible = inp?.isNotEmpty() == true
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

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId -> manage(checkedId) }
    }

    private fun ActivityMainBinding.toggleFabMenu() {
        if (isFabMenuOpen) {
            fabMain.animate().rotation(0F).scaleY(1F).scaleX(1F).start()

            fabAddNote.animate().translationY(0F).alpha(0F)
                .scaleX(0F)
                .scaleY(0F)
                .setListener(
                    SimpleAnimatorListener(fabAddNote)
                ).start()
            fabAddDrawing.animate().translationY(0F).translationX(0F)
                .scaleX(0F)
                .scaleY(0F)
                .alpha(0F).setListener(
                SimpleAnimatorListener(fabAddTask)
            ).start()
            fabAddTask.animate().translationX(0F).alpha(0F)
                .scaleX(0F)
                .scaleY(0F)
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

    private fun manage(checkedId: Int, search: String = "") {
        val filteredList = when (checkedId) {
            R.id.completed -> query.sortedByDescending { it.createdAt }
                .filter {
                    if (it.queryString.contains(search, true) && it is ModelTask) {
                        (it.completed)
                    } else false
                }
            R.id.in_completed -> query.sortedByDescending { it.createdAt }
                .filter {
                    if (it.queryString.contains(search, true) && it is ModelTask) {
                        it.completed.not()
                    } else false
                }
            R.id.notes -> query.sortedByDescending { it.createdAt }
                .filter {
                    it.queryString.contains(search, true) && it is ModelNote
                }
            R.id.tasks -> query.sortedByDescending { it.createdAt }
                .filter { it.queryString.contains(search, true) && it is ModelTask }
            R.id.archive -> query.sortedByDescending { it.createdAt }
                .filter {
                    if (it.queryString.contains(search, true) && it is ModelTask) {
                        (it.createdAt + it.autoArchiveDays * 24 * 60 * 60 * 1000) < System.currentTimeMillis()
                    } else false
                }
            else -> query.sortedByDescending { it.createdAt }
                .filter {
                    it.queryString.contains(search, true)
                }
        }
        adapterNotes.update(filteredList)
//        adapterNotes.update(query.sortedByDescending { c -> c.createdAt })
    }

    private fun initObserver() {
        viewModel.allTasks.observe(this) {
            val temp = ArrayList<ModelId>()
            temp.addAll(it)
            temp.addAll(Injection.drawingDao.getDrawings())
            temp.addAll(Injection.noteDao.getAllNotes())
//            adapterNotes.update(temp.sortedByDescending { c -> c.createdAt })

            query.clear()
            query.addAll(temp)

            manage(binding.chipGroup.checkedChipId)
        }

        viewModel.allNotes.observe(this) {
            val temp = ArrayList<ModelId>()
            temp.addAll(it)
            temp.addAll(Injection.drawingDao.getDrawings())
            temp.addAll(Injection.taskDao.getAllTasks())
//            adapterNotes.update(temp.sortedByDescending { c -> c.createdAt })

            query.clear()
            query.addAll(temp)

            manage(binding.chipGroup.checkedChipId)
        }

        viewModel.allDrawings.observe(this) {
            val temp = ArrayList<ModelId>()
            temp.addAll(it)
            temp.addAll(Injection.taskDao.getAllTasks())
            temp.addAll(Injection.noteDao.getAllNotes())
//            adapterNotes.update(temp.sortedByDescending { c -> c.createdAt })

            query.clear()
            query.addAll(temp)

            manage(binding.chipGroup.checkedChipId)
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