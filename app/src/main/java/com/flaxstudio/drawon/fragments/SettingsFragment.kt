package com.flaxstudio.drawon.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
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
        // Back Clicked
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        // share Clicked
        binding.containerShare.setOnClickListener{
            val sendIntent : Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT , "Hey, I just made a really cool drawing using Draw On App .You should also download this amazing App.")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent , "Share Draw On to your Friends")
            startActivity(shareIntent)
        }

        // RateUs CLicked
        binding.containerRateUs.setOnClickListener {
            Toast.makeText(context,"Added Soon...",Toast.LENGTH_SHORT).show()
        }
        // feedback clicked
        binding.containerFeedback.setOnClickListener{
            Toast.makeText(context,"Added Soon...",Toast.LENGTH_SHORT).show()
        }

        // Privacy Policy container clicked
        binding.containerPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacyFragment)
        }
    }

}