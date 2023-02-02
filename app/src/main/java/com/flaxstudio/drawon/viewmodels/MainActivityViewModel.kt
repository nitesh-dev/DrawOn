package com.flaxstudio.drawon.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    val message =  MutableLiveData<String>()

    fun sendMessage(msg: String){
        message.value = msg
    }
}