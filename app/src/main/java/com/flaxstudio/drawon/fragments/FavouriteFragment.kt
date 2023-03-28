package com.flaxstudio.drawon.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.adapters.FavouriteRecyclerViewAdapter
import com.flaxstudio.drawon.adapters.HomeRecyclerViewAdapter
import com.flaxstudio.drawon.adapters.SpaceItemDecoration
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.databinding.FragmentFavouriteBinding
import com.flaxstudio.drawon.utils.CustomDateTime
import com.flaxstudio.drawon.utils.FragmentType
import com.flaxstudio.drawon.utils.Project
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavouriteFragment : Fragment() {

    private lateinit var adapter: FavouriteRecyclerViewAdapter
    private lateinit var binding: FragmentFavouriteBinding
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }
    private lateinit var contextApp: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)
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
            binding.recyclerView.visibility = View.VISIBLE

        }
    }





    private fun settingData() {

        mainActivityViewModel.getAllFavouriteProjectsTask {projects ->
            for (project in projects){
                adapter.addProject(project)
                adapter.notifyItemInserted(adapter.itemCount - 1)
            }
        }
    }

    private fun settingUp() {
        adapter = FavouriteRecyclerViewAdapter(contextApp)
        val gridLayoutManager = GridLayoutManager(contextApp, 2)

        // set adapter to recyclerview
        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(spacing))

        adapter.setOnClickListener { position, project ->

            // open selected project
            openProject(project)
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

    private fun openProject(project: Project) {

        mainActivityViewModel.openedProject = project
        findNavController().navigate(R.id.action_favouriteFragment_to_drawFragment)
    }

}