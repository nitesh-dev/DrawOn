package com.flaxstudio.drawon

import android.app.Application
import com.flaxstudio.drawon.utils.AppProjectDatabase
import com.flaxstudio.drawon.utils.ProjectRepository

class ProjectApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { AppProjectDatabase.getDatabase(this) }
    val repository by lazy { ProjectRepository(database.projectDao()) }
}