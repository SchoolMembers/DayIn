package com.schedule.dayin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gamdestroyerr.roomnote.db.NoteDatabase
import com.gamdestroyerr.roomnote.viewmodel.NoteActivityViewModel
import com.schedule.dayin.data.memoD.repository.NoteRepository
import com.schedule.dayin.databinding.ActivityNoteBinding
import com.schedule.dayin.utils.shortToast
import com.schedule.dayin.views.NoteActivityViewModelFactory

class NoteActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelProviderFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(
                this,
                noteViewModelProviderFactory
            )[NoteActivityViewModel::class.java]
        } catch (e: Exception) {
            shortToast("error occurred")
        }
    }

}