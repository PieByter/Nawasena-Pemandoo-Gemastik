package com.nawasena.pemandoo.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapsViewModelFactory(private val repository: LandmarkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
