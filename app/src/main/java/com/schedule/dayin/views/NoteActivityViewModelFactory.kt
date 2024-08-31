package com.schedule.dayin.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gamdestroyerr.roomnote.repository.NoteRepository
import com.gamdestroyerr.roomnote.viewmodel.NoteActivityViewModel
import com.schedule.dayin.data.memoD.repository.NoteRepository

@Suppress("UNCHECKED_CAST")
class NoteActivityViewModelFactory(private val repository: NoteRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NoteActivityViewModel(repository) as T
    }
}