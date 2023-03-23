package com.flaxstudio.drawon

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flaxstudio.drawon.databinding.ActivityProjectExportBinding
import com.flaxstudio.drawon.utils.CustomDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class ProjectExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectExportBinding
    private lateinit var projectName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectName = intent.getStringExtra("project-name")!!

        startLoadingBitmap()
        addListeners()

    }

    private val dateTime = CustomDateTime()

    private fun addListeners(){

        // saving file
        binding.saveButton.setOnClickListener {
            val bitmap = binding.cropView.getCroppedBitmap()
            if(bitmap == null){
                Toast.makeText(applicationContext, "Unable to crop!", Toast.LENGTH_SHORT).show()
            }else{

                // handle here... to save bitmap to png, jpg etc
                bitmapToSave = bitmap
                val newSaveName = "$projectName ${dateTime.getDateTimeString()}.jpeg"
                saveImageLauncher.launch(newSaveName)

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

    private lateinit var bitmapToSave: Bitmap

    private val saveImageLauncher = registerForActivityResult(CreateDocument("image/jpeg")){ uri: Uri? ->
        if(uri != null){
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()

                Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()

                // calling finish after some delay for smooth animation
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 400)

            }catch (ex: IOException){
                ex.printStackTrace()
            }
        }
    }

}