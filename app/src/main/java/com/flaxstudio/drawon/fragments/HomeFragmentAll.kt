package com.flaxstudio.drawon.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.flaxstudio.drawon.Adapters.HomeRecyclerViewAdapter
import com.flaxstudio.drawon.Adapters.SpaceItemDecoration
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentHomeAllBinding
import com.flaxstudio.drawon.databinding.FragmentHomeBinding
import com.flaxstudio.drawon.utils.CustomDateTime
import com.flaxstudio.drawon.utils.Project
import com.flaxstudio.drawon.utils.Shape
import com.flaxstudio.drawon.utils.Size
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory

class HomeFragmentAll : Fragment(R.layout.fragment_home_all){

    private lateinit var binding: FragmentHomeAllBinding
    private lateinit var adapter: HomeRecyclerViewAdapter

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingUp()
        uiObservers()
    }


    private fun uiObservers(){

        mainActivityViewModel.allProjects.observe(viewLifecycleOwner){ data ->
            // use this for recycler view

            adapter.clearProjects()
            for(project in data as ArrayList<Project>){

//                if(cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.All){
//
//                }
                adapter.addProject(project)
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun settingUp(){
        adapter = HomeRecyclerViewAdapter(requireContext())
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        // set adapter to recyclerview
        binding.recyclerview.layoutManager = gridLayoutManager
        binding.recyclerview.adapter = adapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.recyclerview.addItemDecoration(SpaceItemDecoration(spacing))

        adapter.setOnClickListener { position, project ->

            if(position == 0){
                // create new project
                openCreateProjectDialog()

            }else{
                // open selected project
                openProject(project)
            }
        }
    }

    private fun openCreateProjectDialog(){
        createNewProject("Draw 1", Size(720, 1280))
    }

    private fun openProject(project: Project){

        mainActivityViewModel.openedProject = project
        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
    }


    private val cDateTime = CustomDateTime()

    private fun createNewProject(projectName: String, whiteBoardSize: Size){
        val emptyShapeData = ArrayList<Shape>()
        val projectId = mainActivityViewModel.generateUniqueId()
        val dateTime = "cDateTime.getDateTimeString()"
        val newProject = Project(0, projectId, projectName, false, dateTime, whiteBoardSize.width, whiteBoardSize.height)

        mainActivityViewModel.createProject(newProject){

            mainActivityViewModel.openedProject = newProject
            mainActivityViewModel.saveProject(requireContext(), emptyShapeData, mainActivityViewModel.defaultToolbarProperties)

            findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
        }
    }
}