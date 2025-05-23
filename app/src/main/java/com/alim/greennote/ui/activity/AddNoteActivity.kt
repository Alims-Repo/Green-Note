package com.alim.greennote.ui.activity

import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alim.greennote.R
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.databinding.ActivityAddNoteBinding
import com.alim.greennote.di.Injection
import com.nelu.ncbase.base.BaseActivity

class AddNoteActivity : BaseActivity<ActivityAddNoteBinding>() {

    override fun ActivityAddNoteBinding.afterViewCreate() {
        enableEdgeToEdge()
        initSystemPadding()

        back.back()

        save.setOnClickListener {
            Injection.noteDao.insertNote(
                ModelNote(
                    title = binding.title.text.toString(),
                    description = binding.note.text.toString(),
                )
            )
            finish()
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
}