package com.flaxstudio.drawon.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel(private val repository: ProjectRepository): ViewModel() {

    lateinit var openedProject: Project
    private val cDateTime = CustomDateTime()
    private val defaultToolbarProperties = ArrayList<ToolProperties>().apply {
        // add brush
        add(ToolProperties(ShapeType.Brush, Color.TRANSPARENT, Color.BLACK, 4f))

        // add rectangle
        add(ToolProperties(ShapeType.Rectangle, Color.RED, Color.BLACK, 4f))

        // add Line
        add(ToolProperties(ShapeType.Line, Color.TRANSPARENT, Color.BLACK, 4f))

        // add Oval
        add(ToolProperties(ShapeType.Oval, Color.RED, Color.BLACK, 4f))

        // add Triangle
        add(ToolProperties(ShapeType.Triangle, Color.RED, Color.BLACK, 4f))

        // add Eraser
        add(ToolProperties(ShapeType.Eraser, Color.TRANSPARENT, Color.WHITE, 80f))

        // do more...
    }

    //room

    fun getAllProjectsTask(callback: (List<Project>) -> Unit) = viewModelScope.launch(Dispatchers.Default){
        val projects = repository.getAllProjects()
        withContext(Dispatchers.Main){
            callback(projects)
        }
    }
    fun createProjectTask(context: Context, projectName: String, width: Int, height: Int, callback: (Project) -> Unit) = viewModelScope.launch(Dispatchers.Default) {

        val projectId = generateUniqueId()
        val projectBitmapId = generateUniqueId()
        val dateTime = cDateTime.getDateTimeString()

        val newProject = Project(
            0,
            projectId,
            projectBitmapId,
            projectName,
            false,
            dateTime,
            dateTime,
            width,
            height
        )

        repository.insert(newProject)

        // creating empty bitmap for new project
        val emptyBitmap = Bitmap.createBitmap(newProject.whiteboardWidth, newProject.whiteboardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(emptyBitmap)
        canvas.drawRGB(255,255,255)

        val json = Gson().toJson(defaultToolbarProperties)                       // creating json file for saving
        saveBitmap(context, emptyBitmap, newProject.projectBitmapId)          // saving bitmap
        saveProjectLocally(context, newProject.projectId, json)               // saving json file

        //repository.update(openedProject)
        withContext(Dispatchers.Main){
            callback(newProject)
        }
    }

    fun updateProjectTask(project: Project, callback: () -> Unit) = viewModelScope.launch ( Dispatchers.Default){
        repository.update(project)
        withContext(Dispatchers.Main){
            callback()
        }
    }

    fun deleteProjectTask(context: Context , project: Project, callback: () -> Unit) = viewModelScope.launch  (Dispatchers.Default){
        repository.delete(project)
        deleteLocalFile(context, project.projectId + ".json")
        deleteLocalFile(context, project.projectBitmapId + ".png")

        withContext(Dispatchers.Main){
            callback()
        }
    }

    fun saveProjectTask(context: Context, bitmap: Bitmap, toolData: ArrayList<ToolProperties>, callback: () -> Unit) = viewModelScope.launch (Dispatchers.Default) {

        saveProject(context, bitmap, toolData)
        repository.update(openedProject)                    // updating database
        withContext(Dispatchers.Main){
            callback()
        }
    }

    fun saveBitmapTask(context: Context, bitmap: Bitmap, bitmapId: String, callback: () -> Unit) = viewModelScope.launch(Dispatchers.Default){

        saveBitmap(context, bitmap, bitmapId)
        withContext(Dispatchers.Main){
            callback()
        }
    }

    fun loadProjectDataTask(context: Context, callback: (ArrayList<ToolProperties>?) -> Unit) = viewModelScope.launch(Dispatchers.Default){

        delay(600)          // delay till animation over
        val json = loadProjectFromLocal(context, openedProject.projectId)
        withContext(Dispatchers.Main){
            callback(Gson().fromJson(json, object : TypeToken<ArrayList<ToolProperties>>() {}.type))
        }
    }

     fun getBitmapTask(context: Context, bitmapId: String, callback: (Bitmap?) -> Unit) = viewModelScope.launch(Dispatchers.Default){

         val fileName = "${bitmapId}.png"
         val bitmap: Bitmap? = try {
             context.openFileInput(fileName).use {
                 BitmapFactory.decodeStream(it)
             }
         }catch (ex: IOException){
             ex.printStackTrace()
             null
         }

         withContext(Dispatchers.Main){
             callback(bitmap)
         }
    }


    // save and load functions
    private fun saveProject(context: Context, bitmap: Bitmap, toolData: ArrayList<ToolProperties>){
        Log.e("============", "ksdjfkjsdklfjsdfsdfsdf")
        // TODO "project not saving bug" on saving project after creating initial project.

        // deleting previous saved image
        deleteLocalFile(context, "${openedProject.projectBitmapId}.png")

        val json = Gson().toJson(toolData)                                  // creating json file for saving

        openedProject.lastModified = cDateTime.getDateTimeString()          // setting current date time
        openedProject.projectBitmapId = generateUniqueId()                  // generating id for bitmap

        saveBitmap(context, bitmap, openedProject.projectBitmapId)          // saving bitmap
        saveProjectLocally(context, openedProject.projectId, json)          // saving json file
    }




    private fun saveBitmap(context: Context, bitmap: Bitmap, bitmapId: String = "0"){

        val fileName = "$bitmapId.png"
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
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
