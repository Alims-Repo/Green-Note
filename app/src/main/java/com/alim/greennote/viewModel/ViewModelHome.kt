package com.alim.greennote.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alim.greennote.data.Dummy
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.di.Injection

class ViewModelHome(
    private val dao: TaskDao = Injection.taskDao
) : ViewModel() {

    val filter = MutableLiveData<List<ModelTask>>()
    val allNotes: LiveData<List<ModelTask>> = dao.getAllTasksLive()

    fun query(text: String) {
        val currentList = allNotes.value ?: emptyList()
        val result = currentList.filter {
            it.title.contains(text, true) || it.description.contains(text, true)
        }
        filter.postValue(result)
    }
}