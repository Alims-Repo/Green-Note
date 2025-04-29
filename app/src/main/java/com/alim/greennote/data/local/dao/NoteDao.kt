package com.alim.greennote.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alim.greennote.data.model.ModelNote

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(task: ModelNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(tasks: List<ModelNote>)

    @Update
    suspend fun updateNote(task: ModelNote)

    @Delete
    suspend fun deleteNote(task: ModelNote)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<ModelNote>

    @Query("SELECT * FROM notes")
    fun getAllNotesLive(): LiveData<List<ModelNote>>

    @Query("SELECT * FROM notes WHERE id = :taskId LIMIT 1")
    suspend fun getNoteById(taskId: Int): ModelNote?

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}