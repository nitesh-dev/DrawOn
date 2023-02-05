package com.flaxstudio.drawon.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flaxstudio.drawon.utils.ProjectData

class MainActivityViewModel: ViewModel() {

    val message =  MutableLiveData<String>()
    val allProjects = MutableLiveData<ArrayList<ProjectData>>()

    fun sendMessage(msg: String){
        message.value = msg
    }
}