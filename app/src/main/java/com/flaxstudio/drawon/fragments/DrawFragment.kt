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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.utils.Brush
import com.flaxstudio.drawon.utils.BrushRaw
import com.flaxstudio.drawon.utils.ShapeType
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory


class DrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawBinding
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        addListeners()
        loadUiData()
    }


    private fun addListeners(){
        binding.saveButton.setOnClickListener {

            mainActivityViewModel.saveProject(requireContext() , binding.drawingView.getDrawnShapes(), binding.drawingView.getToolData())

            // creating & saving thumbnail
            val bitmap = binding.drawingView.getThumbnail()

            // saving canvas thumbnail
            mainActivityViewModel.saveBitmap(requireContext(), bitmap, true)
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }
    }


    // ui update work here
    private fun loadUiData(){

        val projectData = mainActivityViewModel.loadProject(requireContext())
        for (rawShape in projectData.allSavedShapes){

            // do same for all the shapes which store path data
            if(rawShape.shapeType == ShapeType.Brush){
                binding.drawingView.setDrawnShapes((rawShape as BrushRaw).toBrush())
            }else{
                binding.drawingView.setDrawnShapes(rawShape)
            }
        }



        binding.fileName.text = mainActivityViewModel.openedProject.projectName
        binding.favCheckBox.isChecked = mainActivityViewModel.openedProject.isFavourite

    }

}