package com.blazo.transporteuniversitarioapp.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Notas algo extraño en tu viaje?"
    }
    val text: LiveData<String> = _text
}