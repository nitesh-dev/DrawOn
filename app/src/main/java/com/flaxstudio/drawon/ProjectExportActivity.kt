package com.flaxstudio.drawon

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.registerForActivityResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flaxstudio.drawon.databinding.ActivityProjectExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
                saveBitmap(bitmap)
                Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resetButton.setOnClickListener {
            binding.cropView.resetPointerToDefault()
        }
    }
    private fun saveBitmap(bitmap:Bitmap){

        try {
            val outputStream:OutputStream?
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                val resolver = contentResolver
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME,"default.jpeg")
                values.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
                val resolverUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
                outputStream = resolverUri?.let { resolver.openOutputStream(it) }
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
                outputStream?.flush()
                outputStream?.close()
            }
        }catch (e:Exception){
            e.message
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