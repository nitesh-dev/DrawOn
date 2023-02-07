package com.flaxstudio.drawon.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.adpters.HomePagerAdapter
import com.flaxstudio.drawon.databinding.FragmentHomeBinding
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
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

        // create object of MainActivityViewModel
        val viewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        viewModel.sendMessage("Drawing 1")

        //findNavController().navigate(R.id.action_homeFragment_to_drawFragment)
        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)

        }
    }

    private fun setupLayoutWithViewPager() {
        binding.viewPager.adapter = HomePagerAdapter(this)
        TabLayoutMediator(binding.tabBarLayout , binding.viewPager){ tab , position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}