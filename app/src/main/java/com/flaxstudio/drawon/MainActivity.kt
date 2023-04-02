package com.flaxstudio.drawon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flaxstudio.drawon.databinding.ActivityMainBinding
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showReviewDialog()
    }

    private fun showReviewDialog(){
        val reviewManager = ReviewManagerFactory.create(applicationContext)
        reviewManager.requestReviewFlow().addOnCompleteListener{
            if (it.isSuccessful){
                reviewManager.launchReviewFlow(this , it.result)
            }
        }

    }

}