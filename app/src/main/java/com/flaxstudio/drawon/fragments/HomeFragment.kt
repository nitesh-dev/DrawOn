package com.flaxstudio.drawon.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.Adapters.HomePagerAdapter
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentHomeBinding
import com.flaxstudio.drawon.utils.*
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mainActivityViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }


    private val tabTitles = arrayListOf("All" , "Today" , "Week" , "Month")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupLayoutWithViewPager()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivityViewModel.allProjects.observe(viewLifecycleOwner){

            // use this for recycler view
            println(it.size)
        }

    }

    private fun addListeners(){
        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

    }


    // the below function will load app data
    private fun setUpAppData(){

        //createNewProject("Untitled", Size(500, 500))
    }
    private fun setupLayoutWithViewPager() {
        binding.viewPager.adapter = HomePagerAdapter(this)
        TabLayoutMediator(binding.tabBarLayout , binding.viewPager){ tab , position ->
            tab.text = tabTitles[position]
        }.attach()
    }



    private fun createNewProject(projectName: String, whiteBoardSize: Size){
//        val emptyShapeData = ArrayList<Shape>()
//        val projectId = viewModel.generateUniqueId()
//        val newProject = Project(0, projectId, projectName, false, "", whiteBoardSize)
//        viewModel.projectDao.addProject(newProject)
//
//        viewModel.openedProject = viewModel.projectDao.getProjectById(projectId)
//        viewModel.saveProject(requireContext(), emptyShapeData, defaultToolbarProperties)
//
//        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)

    }

    private fun openProject(projectIndex: Int){
        // do something here....
        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
    }



    val defaultToolbarProperties = ArrayList<ToolProperties>().apply {
        // add brush
        add(ToolProperties(ShapeType.Brush, Color.TRANSPARENT, Color.BLACK, 4f))

        // add rectangle
        add(ToolProperties(ShapeType.Rectangle, Color.RED, Color.BLACK, 4f))

        // do more...
    }

}