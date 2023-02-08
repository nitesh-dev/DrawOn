package com.flaxstudio.drawon.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.utils.Brush
import com.flaxstudio.drawon.utils.BrushRaw
import com.flaxstudio.drawon.utils.ShapeType
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

        addListeners()
        loadUiData()
    }


    private fun addListeners(){
        binding.saveButton.setOnClickListener {

            viewModel.saveProject(requireContext() , binding.drawingView.getDrawnShapes(), binding.drawingView.getToolData())

            // creating & saving thumbnail
            val bitmap = binding.drawingView.getThumbnail()

            // saving canvas thumbnail
            viewModel.saveBitmap(requireContext(), bitmap, true)
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }
    }


    // ui update work here
    private fun loadUiData(){

        val projectData = viewModel.loadProject(requireContext())
        for (rawShape in projectData.allSavedShapes){

            // do same for all the shapes which store path data
            if(rawShape.shapeType == ShapeType.Brush){
                binding.drawingView.setDrawnShapes((rawShape as BrushRaw).toBrush())
            }else{
                binding.drawingView.setDrawnShapes(rawShape)
            }
        }

        binding.fileName.text = viewModel.openedProject.projectName
        binding.favCheckBox.isChecked = viewModel.openedProject.isFavourite

    }

}