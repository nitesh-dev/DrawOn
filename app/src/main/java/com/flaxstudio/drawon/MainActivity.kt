package com.flaxstudio.drawon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.abhishek.colorpicker.ColorPickerDialog
import com.flaxstudio.drawon.databinding.ActivityMainBinding
import com.flaxstudio.drawon.utils.Project
import com.flaxstudio.drawon.utils.Size
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val dialog = ColorPickerDialog()
//        dialog.setOnOkCancelListener { isOk, color ->
//            Log.e("======", color.toString())
//        }
//        dialog.show(supportFragmentManager)

    }
}