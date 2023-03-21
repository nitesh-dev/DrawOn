package com.flaxstudio.drawon.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.flaxstudio.drawon.adapters.HomeRecyclerViewAdapter
import com.flaxstudio.drawon.adapters.SpaceItemDecoration
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentHomeAllBinding
import com.flaxstudio.drawon.utils.*
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory

class HomeFragmentAll(fragmentType: FragmentType) : Fragment(R.layout.fragment_home_all) {

    private lateinit var binding: FragmentHomeAllBinding
    private lateinit var adapter: HomeRecyclerViewAdapter
    private val fragmentType: FragmentType
    private lateinit var contextApp: Context

    init {
        this.fragmentType = fragmentType
    }

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // used to handle back press
        requireActivity().onBackPressedDispatcher.addCallback(this) {

            // Handle the back button event
            if(adapter.longPressSelectedView == null){
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }else{

                adapter.longPressSelectedView!!.visibility = View.INVISIBLE
                adapter.longPressSelectedView = null
            }
        }
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

        contextApp = requireContext()
        settingUp()
        settingData()
    }




    @SuppressLint("NotifyDataSetChanged")
    private fun settingData() {

        adapter.clearProjects()

        mainActivityViewModel.getAllProjects {
            for (project in it) {

                when (fragmentType) {
                    FragmentType.Today -> {

                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Today) {
                            adapter.addProject(project)
                        }
                    }

                    FragmentType.Week -> {
                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Week) {
                            adapter.addProject(project)
                        }
                    }

                    FragmentType.Month -> {
                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Month) {
                            adapter.addProject(project)
                        }
                    }

                    FragmentType.All -> {
                        adapter.addProject(project)
                    }
                }

            }
            adapter.notifyDataSetChanged()
        }

    }

    private fun settingUp() {
        adapter = HomeRecyclerViewAdapter(contextApp)
        val gridLayoutManager = GridLayoutManager(contextApp, 2)

        // set adapter to recyclerview
        binding.recyclerview.layoutManager = gridLayoutManager
        binding.recyclerview.adapter = adapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.recyclerview.addItemDecoration(SpaceItemDecoration(spacing))

        adapter.setOnClickListener { position, project ->

            if (position == 0) {
                // create new project
                openCreateProjectDialog()

            } else {
                // open selected project
                openProject(project)
            }
        }

        adapter.setOnProjectDeleteListener { position, project ->
            mainActivityViewModel.deleteProject(contextApp, project) {
                adapter.removeProject(project)
                adapter.notifyItemRemoved(position)
            }
        }

        adapter.setOnFavClickListener { _, project ->

            mainActivityViewModel.updateProject(project)
        }
    }

    private fun openCreateProjectDialog() {
        createNewProject("Draw 1", Size(720, 1280))
    }

    private fun openProject(project: Project) {

        mainActivityViewModel.openedProject = project
        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
    }


    private val cDateTime = CustomDateTime()

    private fun createNewProject(projectName: String, whiteBoardSize: Size) {

        val projectId = mainActivityViewModel.generateUniqueId()
        val dateTime = cDateTime.getDateTimeString()
        Log.e("============", dateTime)
        val newProject = Project(
            0,
            projectId,
            projectName,
            false,
            dateTime,
            whiteBoardSize.width,
            whiteBoardSize.height
        )

        mainActivityViewModel.createProject(newProject) {

            mainActivityViewModel.openedProject = newProject
            mainActivityViewModel.saveProject(
                contextApp,
                null,
                mainActivityViewModel.defaultToolbarProperties
            )

            findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
            //createProjectAlertDialog()
        }
    }

    // the below function is to create a alert dialog box for new project
    private fun createProjectAlertDialog(){
        val alertBuilder = AlertDialog.Builder(requireContext())
        val customLayout : View = layoutInflater.inflate(R.layout.create_project_alert_box ,null)
        alertBuilder.setView(customLayout)
        val dialog = alertBuilder.create()
        val etLength = customLayout.findViewById<EditText>(R.id.et_length)
        val etWidth = customLayout.findViewById<EditText>(R.id.et_width)
        val etTitle = customLayout.findViewById<EditText>(R.id.et_title)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        customLayout.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            // TODO handle the negative button click here
            dialog.dismiss()
        }
        customLayout.findViewById<Button>(R.id.btn_create).setOnClickListener {

            if (etTitle.text.isNullOrEmpty()){
                Toast.makeText(requireContext() , "Please Enter a valid Title." , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                val title = etTitle.text.toString()
                if (etLength.text.isNullOrEmpty() || etWidth.text.isNullOrEmpty()){
                    Toast.makeText(requireContext() , "Please Enter a valid Height and Width." , Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }else{
                    val length : Int  = etLength.text.toString().toInt()
                    val width  : Int  = etWidth.text.toString().toInt()
                    //createNewProject(title, Size(width , length))
                    // TODO handle the positive button click here
                    dialog.dismiss()
                }
            }

        }
    }
}