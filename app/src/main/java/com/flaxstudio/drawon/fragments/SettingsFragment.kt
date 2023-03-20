package com.flaxstudio.drawon.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Privacy Policy container clicked
        binding.containerPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacyFragment)
        }
        // Bck Clicked
        binding.btnBack.setOnClickListener{

        }
    }

}