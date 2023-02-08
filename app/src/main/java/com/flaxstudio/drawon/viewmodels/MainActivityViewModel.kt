package com.flaxstudio.drawon.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.*
import com.flaxstudio.drawon.utils.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivityViewModel(private val repository: ProjectRepository): ViewModel() {

    lateinit var openedProject: Project

    //room
    val allProjects: LiveData<List<Project>> = repository.allProjects.asLiveData()


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
    fun saveProject(context: Context, shapeData: ArrayList<Shape>, toolData: ArrayList<ToolProperties>){

        val openedProjectData = ProjectData()

        // converting all class object to serializable object
        for (shapeD in shapeData){

            // do the below operation only for path having shape
            if(shapeD.shapeType == ShapeType.Brush){
                openedProjectData.allSavedShapes.add((shapeD as Brush).toBrushRaw())
            }else{
                // for pathless shape only
                openedProjectData.allSavedShapes.add(shapeD)
            }
        }

        // adding all tools properties
        openedProjectData.toolsData.addAll(toolData)

        // saving
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(typeAdapterFactory)
            .create()
        val json = gson.toJson(openedProjectData)
        Log.e("==========", json)

        saveProjectLocally(context, openedProject.projectId, json)
    }

    fun loadProject(context: Context): ProjectData {

        val gson = GsonBuilder()
            .registerTypeAdapterFactory(typeAdapterFactory)
            .create()
        val json = loadProjectFromLocal(context, openedProject.projectId)
        return gson.fromJson(json, ProjectData::class.java)
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, isThumbnail: Boolean = false, bitmapId: String = "0"){

        val fileName = if(isThumbnail){
            "${openedProject.projectId}.png"
        } else {
            "${bitmapId}.png"
        }

        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    fun getBitmap(context: Context, isThumbnail: Boolean = false, bitmapId: String = "0"): Bitmap? {

        val fileName = if(isThumbnail){
            "${openedProject.projectId}.png"
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


    // for converting inheritance class to json
    private val typeAdapterFactory = RuntimeTypeAdapterFactory.of(Shape::class.java, "type").apply {
        registerSubtype(Shape::class.java, "Shape")
        registerSubtype(Rectangle::class.java, "Rectangle")
        registerSubtype(BrushRaw::class.java, "BrushRaw")
        // do more...

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
