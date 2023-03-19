package com.flaxstudio.drawon

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.flaxstudio.drawon.databinding.ActivityMainBinding
import com.flaxstudio.drawon.databinding.ActivityProjectExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ProjectExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectExportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLoadingBitmap()
        addListeners()


    }

    private fun addListeners(){

        // saving file
        binding.saveButton.setOnClickListener {
            val bitmap = binding.cropView.getCroppedBitmap()
            if(bitmap == null){
                Toast.makeText(applicationContext, "Unable to crop!", Toast.LENGTH_SHORT).show()
            }else{

                // handle here... to save bitmap to png, jpg etc

                Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resetButton.setOnClickListener {
            binding.cropView.resetPointerToDefault()
        }
    }

    private fun startLoadingBitmap(){

        lifecycleScope.launch(Dispatchers.Default) {
            delay(500)
            val bitmap = getBitmap()
            if(bitmap != null){
                binding.cropView.loadBitmap(bitmap)
            }else{
                Toast.makeText(applicationContext, "Unable to load image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBitmap(): Bitmap? {

        openFileInput("export-bitmap.png").use {
            return BitmapFactory.decodeStream(it)
        }
    }
}