package com.flaxstudio.drawon.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.utils.BrushRaw
import com.flaxstudio.drawon.utils.EraserRaw
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
        binding.saveButton.setOnClickListener{
            findNavController().navigate(R.id.action_drawFragment_to_homeFragment)
        }

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

        binding.toolbarGroup.addOnButtonCheckedListener { toggleGroup, _, _ ->

            when (toggleGroup.checkedButtonId) {
                R.id.toolBrush -> {
                    binding.drawingView.setSelectedTool(ShapeType.Brush)
                }
                R.id.toolRect -> {
                    binding.drawingView.setSelectedTool(ShapeType.Rectangle)
                }
                R.id.toolLine -> {
                    binding.drawingView.setSelectedTool(ShapeType.Line)
                }
                R.id.toolOval -> {
                    binding.drawingView.setSelectedTool(ShapeType.Oval)
                }
                R.id.toolTriangle -> {
                    binding.drawingView.setSelectedTool(ShapeType.Triangle)
                }
                R.id.toolEraser -> {
                    binding.drawingView.setSelectedTool(ShapeType.Eraser)
                }
            }
        }
    }


    // ui update work here
    private fun loadUiData(){

        val projectData = mainActivityViewModel.loadProject(requireContext())
        for (rawShape in projectData.allSavedShapes){

            // do same for all the shapes which store path data
            if(rawShape.shapeType == ShapeType.Brush){
                binding.drawingView.setDrawnShapes((rawShape as BrushRaw).toBrush())

            }else if(rawShape.shapeType == ShapeType.Eraser){
                binding.drawingView.setDrawnShapes((rawShape as EraserRaw).toEraser())

            } else{
                binding.drawingView.setDrawnShapes(rawShape)
            }
        }

        binding.drawingView.setToolData(projectData.toolsData)

        binding.fileName.text = mainActivityViewModel.openedProject.projectName
        binding.favCheckBox.isChecked = mainActivityViewModel.openedProject.isFavourite

    }

}