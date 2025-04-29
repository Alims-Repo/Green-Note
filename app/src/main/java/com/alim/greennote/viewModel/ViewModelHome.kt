package com.alim.greennote.viewModel

import android.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alim.greennote.data.Dummy
import com.alim.greennote.data.dao.DrawingDao
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.di.Injection

class ViewModelHome(
    private val daoTask: TaskDao = Injection.taskDao,
    private val daoDrawing: DrawingDao = Injection.drawingDao
) : ViewModel() {

    val filter = MutableLiveData<List<Any>>()

    val allNotes: LiveData<List<ModelTask>> = daoTask.getAllTasksLive()
    val allDrawings: LiveData<List<DrawingEntity>> = daoDrawing.getDrawingsLive()


    fun query(text: String) {
        val currentList = allNotes.value ?: emptyList()
        val result = currentList.filter {
            it.title.contains(text, true) || it.description.contains(text, true)
        }
        filter.postValue(result)
    }
}