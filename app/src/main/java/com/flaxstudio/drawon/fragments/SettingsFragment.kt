package com.flaxstudio.drawon.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flaxstudio.drawon.databinding.FragmentSettingsBinding
import com.google.android.play.core.review.ReviewManagerFactory


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    private val appLink = "https://play.google.com/store/apps/details?id=com.flaxstudio.drawon"
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // share Clicked
        binding.containerShare.setOnClickListener{
            val sendIntent : Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT , "Hey, I just made a really cool drawing using Draw On App .You should also download this amazing App. \n" +
                        appLink
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent , "Share Draw On to your Friends")
            startActivity(shareIntent)
        }

        // RateUs CLicked
        binding.containerRateUs.setOnClickListener {

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appLink))
            startActivity(browserIntent)
        }

        // feedback clicked
        binding.containerFeedback.setOnClickListener{
            val intent = Intent().apply{
                action = Intent.ACTION_SENDTO
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("flaxstudiohelp@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Tell about our application")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))

        }

        // Privacy Policy
        binding.containerPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nitesh-dev.github.io/DrawOn/privacy_policy"))
            startActivity(browserIntent)
        }
    }

    private fun showReviewDialog(){
        val reviewManager = ReviewManagerFactory.create(requireContext())
        reviewManager.requestReviewFlow().addOnCompleteListener{
            if (it.isSuccessful){
                reviewManager.launchReviewFlow(requireActivity() , it.result)
            }
        }

    }

}