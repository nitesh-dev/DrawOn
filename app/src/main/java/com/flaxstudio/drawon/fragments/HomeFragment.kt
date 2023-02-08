package com.flaxstudio.drawon.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.flaxstudio.drawon.Adapters.HomePagerAdapter
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentHomeBinding
import com.flaxstudio.drawon.utils.*
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
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



    private var tempArr = ArrayList<Project>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mainActivityViewModel.allProjects.observe(viewLifecycleOwner){ data ->
            // use this for recycler view
            println(data.size)
            tempArr = data as ArrayList<Project>

            //openProject(2)
        }

        setUpAppData()

    }

    private fun addListeners(){
        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

    }


    // the below function will load app data
    private fun setUpAppData(){

        createNewProject("Untitled", Size(500, 500))

    }
    private fun setupLayoutWithViewPager() {
        binding.viewPager.adapter = HomePagerAdapter(this)
        TabLayoutMediator(binding.tabBarLayout , binding.viewPager){ tab , position ->
            tab.text = tabTitles[position]
        }.attach()
    }



    private fun createNewProject(projectName: String, whiteBoardSize: Size){
        val emptyShapeData = ArrayList<Shape>()
        val projectId = mainActivityViewModel.generateUniqueId()
        val newProject = Project(0, projectId, projectName, false, "", whiteBoardSize.width, whiteBoardSize.height)

        mainActivityViewModel.createProject(newProject){

            mainActivityViewModel.openedProject = newProject
            mainActivityViewModel.saveProject(requireContext(), emptyShapeData, defaultToolbarProperties)

            val emptyThumbnail = Bitmap.createBitmap(128, 128, Bitmap.Config.RGB_565)
            mainActivityViewModel.saveBitmap(requireContext(), emptyThumbnail, true)
            findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
        }

    }

    private fun openProject(projectIndex: Int){

        mainActivityViewModel.openedProject = tempArr[projectIndex]
        // do something here....
        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
    }



    private val defaultToolbarProperties = ArrayList<ToolProperties>().apply {
        // add brush
        add(ToolProperties(ShapeType.Brush, Color.TRANSPARENT, Color.BLACK, 4f))

        // add rectangle
        add(ToolProperties(ShapeType.Rectangle, Color.RED, Color.BLACK, 4f))

        // do more...
    }

}