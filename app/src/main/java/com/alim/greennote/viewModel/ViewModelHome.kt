package com.alim.greennote.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alim.greennote.data.local.dao.DrawingDao
import com.alim.greennote.data.local.dao.NoteDao
import com.alim.greennote.data.local.dao.TaskDao
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.di.Injection

class ViewModelHome(
    private val daoTask: TaskDao = Injection.taskDao,
    private val daoNote: NoteDao = Injection.noteDao,
    private val daoDrawing: DrawingDao = Injection.drawingDao
) : ViewModel() {

    val filter = MutableLiveData<List<Any>>()

    val allTasks: LiveData<List<ModelTask>> = daoTask.getAllTasksLive()
    val allNotes: LiveData<List<ModelNote>> = daoNote.getAllNotesLive()
    val allDrawings: LiveData<List<DrawingEntity>> = daoDrawing.getDrawingsLive()


    fun query(text: String) {
        val currentList = allNotes.value ?: emptyList()
        val result = currentList.filter {
            it.title.contains(text, true) || it.description.contains(text, true)
        }
        filter.postValue(result)
    }
}