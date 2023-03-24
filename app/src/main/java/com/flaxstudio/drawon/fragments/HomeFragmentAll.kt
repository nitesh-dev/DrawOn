package com.flaxstudio.drawon.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        loadRecyclerViewDataInBackground()
    }

    private fun loadRecyclerViewDataInBackground() = lifecycleScope.launch{
        delay(800)              // wait for animation to done
        settingUp()
        settingData()

        withContext(Dispatchers.Main){
            binding.shimmerLayout.visibility = View.GONE
            binding.recyclerview.visibility = View.VISIBLE

        }
    }





    private fun settingData() {

        adapter.clearProjects()

        mainActivityViewModel.getAllProjectsTask {
            for (project in it) {

                when (fragmentType) {
                    FragmentType.Today -> {

                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Today) {
                            adapter.addProject(project)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }

                    FragmentType.Week -> {
                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Week) {
                            adapter.addProject(project)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }

                    FragmentType.Month -> {
                        if (cDateTime.getDateWithin(project.lastModified) == CustomDateTime.DateWithin.Month) {
                            adapter.addProject(project)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }

                    FragmentType.All -> {
                        adapter.addProject(project)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                    }
                }

            }
        }
    }

    private fun settingUp() {
        adapter = HomeRecyclerViewAdapter(contextApp,fragmentType)
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
            mainActivityViewModel.deleteProjectTask(contextApp, project) {
                adapter.removeProject(project)
                adapter.notifyItemRemoved(position)
            }
        }

        adapter.setOnFavClickListener { _, project ->

            mainActivityViewModel.updateProjectTask(project){
                // callback
            }
        }
    }


    private fun openCreateProjectDialog(){

        val alertBuilder = AlertDialog.Builder(requireContext())
        val customAlertBox : View = layoutInflater.inflate(R.layout.create_project_alert_box , null)
        alertBuilder.setView(customAlertBox)
        val createProjectDialog = alertBuilder.create()
        createProjectDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        createProjectDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customAlertBox.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            createProjectDialog.dismiss()
        }
        customAlertBox.findViewById<Button>(R.id.btn_create).setOnClickListener {
            val title = customAlertBox.findViewById<EditText>(R.id.et_title).text.toString()
            val width = customAlertBox.findViewById<EditText>(R.id.edit_width).text.toString()
            val height = customAlertBox.findViewById<EditText>(R.id.edit_height).text.toString()

            if(title.isBlank()) Toast.makeText(contextApp, "Name must not be empty", Toast.LENGTH_SHORT).show()
            else if(width.isBlank())Toast.makeText(contextApp, "Width must not be empty", Toast.LENGTH_SHORT).show()
            else if(height.isBlank())Toast.makeText(contextApp, "Height must not be empty", Toast.LENGTH_SHORT).show()
            else if(width.toInt() > 2000) Toast.makeText(contextApp, "Width must not be smaller than 2000", Toast.LENGTH_SHORT).show()
            else if(height.toInt() > 2000) Toast.makeText(contextApp, "Height must not be smaller than 2000", Toast.LENGTH_SHORT).show()
            else{
                createNewProject(title, Size( width.toInt(), height.toInt()))
                createProjectDialog.dismiss()
            }
        }

        createProjectDialog.show()
    }


    private fun openProject(project: Project) {

        mainActivityViewModel.openedProject = project
        findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
    }


    private val cDateTime = CustomDateTime()

    private fun createNewProject(projectName: String, whiteBoardSize: Size) {

        val projectId = mainActivityViewModel.generateUniqueId()
        val dateTime = cDateTime.getDateTimeString()

        val newProject = Project(
            0,
            projectId,
            "",
            projectName,
            false,
            dateTime,
            dateTime,
            whiteBoardSize.width,
            whiteBoardSize.height
        )

        mainActivityViewModel.openedProject = newProject
        mainActivityViewModel.createProjectTask(contextApp, newProject) {
            findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
        }

    }
}