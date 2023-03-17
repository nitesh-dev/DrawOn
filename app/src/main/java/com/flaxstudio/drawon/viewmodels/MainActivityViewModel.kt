package com.flaxstudio.drawon.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.flaxstudio.drawon.utils.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel(private val repository: ProjectRepository): ViewModel() {

    lateinit var openedProject: Project

    val defaultToolbarProperties = ArrayList<ToolProperties>().apply {
        // add brush
        add(ToolProperties(ShapeType.Brush, Color.TRANSPARENT, Color.BLACK, 4f))

        // add rectangle
        add(ToolProperties(ShapeType.Rectangle, Color.RED, Color.BLACK, 4f))

        // add Line
        add(ToolProperties(ShapeType.Line, Color.TRANSPARENT, Color.BLACK, 4f))

        // add Oval
        add(ToolProperties(ShapeType.Oval, Color.GREEN, Color.BLACK, 4f))

        // add Triangle
        add(ToolProperties(ShapeType.Triangle, Color.BLUE, Color.BLACK, 4f))

        // add Eraser
        add(ToolProperties(ShapeType.Eraser, Color.TRANSPARENT, Color.WHITE, 80f))

        // do more...
    }

    //room

    fun getAllProjects(callback: (List<Project>) -> Unit) = viewModelScope.launch(Dispatchers.Default){
        val projects = repository.getAllProjects()
        withContext(Dispatchers.Main){
            callback(projects)
        }
    }
    fun createProject(project: Project, callback: () -> Unit) = viewModelScope.launch(Dispatchers.Default) {
        repository.insert(project)
        withContext(Dispatchers.Main){
            callback()
        }
    }

    fun updateProject(project: Project) = viewModelScope.launch ( Dispatchers.Default){
        repository.update(project)
    }

    fun deleteProject(context: Context , project: Project, callback: () -> Unit) = viewModelScope.launch  (Dispatchers.Default){
        repository.delete(project)
        deleteLocalFile(context, project.projectId + ".json")
        deleteLocalFile(context, project.projectId + ".png")

        withContext(Dispatchers.Main){
            callback()
        }
    }




    // save and load functions
    fun saveProject(context: Context, bitmap: Bitmap?, toolData: ArrayList<ToolProperties>){

        // saving
        val json = Gson().toJson(toolData)
        Log.e("==========", json)

        if(bitmap != null) saveBitmap(context, bitmap, false, openedProject.projectId)

        saveProjectLocally(context, openedProject.projectId, json)
    }

    fun loadProject(context: Context): ArrayList<ToolProperties>? {

        val json = loadProjectFromLocal(context, openedProject.projectId)
        return Gson().fromJson(json, object : TypeToken<ArrayList<ToolProperties>>() {}.type)
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, isThumbnail: Boolean = false, bitmapId: String = "0"){

        val fileName = if(isThumbnail){
            "${openedProject.projectId}thumb.png"
        } else {
            "${bitmapId}.png"
        }

        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }


    fun getBitmap(context: Context, isThumbnail: Boolean = false, bitmapId: String = "0"): Bitmap? {

        val fileName = if(isThumbnail){
            "${openedProject.projectId}thumb.png"
        } else {
            "${bitmapId}.png"
        }
        context.openFileInput(fileName).use {
            return BitmapFactory.decodeStream(it)
        }
    }


    // the below function will generate unique id
    fun generateUniqueId(): String{
        return System.currentTimeMillis().toString()
    }


    private fun saveProjectLocally(context: Context, jsonFileName: String, content: String){
        context.openFileOutput("$jsonFileName.json", Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
            it.close()
        }
    }

    private fun loadProjectFromLocal(context: Context, jsonFileName: String): String{
        context.openFileInput("$jsonFileName.json").use {
            return String(it.readBytes())
        }
    }

    private fun deleteLocalFile(context: Context, fileName: String){
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

}
class MainActivityViewModelFactory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
