package com.flaxstudio.drawon.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.databinding.FragmentHomeBinding
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel


class DrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawBinding
    private lateinit var viewModel: MainActivityViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create object of MainActivityViewModel
        viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        uiObserver()

    }


    // ui update work here
    private fun uiObserver(){
        // observing the change in the message declared in SharedViewModel

        viewModel.message.observe(viewLifecycleOwner, Observer {
            // updating data in displayMsg
            binding.fileName.text = it
        })
    }

}