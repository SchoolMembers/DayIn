package com.example.dayin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dayin.R

class DiaryFragment : Fragment() {

    companion object {
        fun newInstance(): DiaryFragment {
            val args = Bundle()
            val fragment = DiaryFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.diary_fragment, container, false)
    }
}