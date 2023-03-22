package com.flaxstudio.drawon.utils

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "project_id") var projectId: String,
    @ColumnInfo(name = "project_bitmap_id") var projectBitmapId: String,
    @ColumnInfo(name = "project_name") var projectName: String,
    @ColumnInfo(name = "is_fav") var isFavourite: Boolean,
    @ColumnInfo(name = "project_created") var projectCreated: String,
    @ColumnInfo(name = "last_modified") var lastModified: String,
    @ColumnInfo(name = "whiteboard_width") var whiteboardWidth: Int,
    @ColumnInfo(name = "whiteboard_height") var whiteboardHeight: Int,

    ){

}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): List<Project>

    @Query("SELECT * FROM projects WHERE project_id = :id LIMIT 1")
    fun getProjectById(id: String): Project

    @Update
    fun updateProject(project: Project)

    @Insert
    fun addProject(project: Project)

    @Delete
    fun deleteProject(project: Project)
}
class ProjectRepository(private val projectDao: ProjectDao) {


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(project: Project) {
        projectDao.addProject(project)
    }

    @WorkerThread
    suspend fun getAllProjects(): List<Project>{
        return projectDao.getAllProjects()
    }

    @WorkerThread
    suspend fun update(project: Project) {
        projectDao.updateProject(project)
    }


    @WorkerThread
    suspend fun delete(project: Project) {
        projectDao.deleteProject(project)
    }

    @WorkerThread
    suspend fun getProjectById(projectId: String): Project {
        return projectDao.getProjectById(projectId)
    }

}
@Database(entities = [Project::class], version = 1, exportSchema = false)
abstract class AppProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppProjectDatabase? = null

        fun getDatabase(context: Context): AppProjectDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppProjectDatabase::class.java,
                    "database-project"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}