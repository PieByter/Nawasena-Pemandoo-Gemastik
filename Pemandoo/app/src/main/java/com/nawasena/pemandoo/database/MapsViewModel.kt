package com.nawasena.pemandoo.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: LandmarkRepository) : ViewModel() {

    val allLandmarks: LiveData<List<Landmark>> = repository.allLandmarks

    fun insert(landmark: Landmark) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(landmark)
        }
    }

    fun update(landmark: Landmark) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(landmark)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
        }
    }

    fun getLandmarkById(id: Int): LiveData<Landmark?> {
        return repository.getLandmarkById(id)
    }

    suspend fun getAllLandmarksWithDetails(): List<Landmark> {
        return repository.getAllLandmarksWithDetails()
    }

    private var currentLandmark: Landmark? = null

    // Function to get current landmark
    fun getCurrentLandmark(): Landmark? {
        return currentLandmark
    }

    // Function to update current landmark
    fun updateCurrentLandmark(landmark: Landmark) {
        currentLandmark = landmark
    }

    // Example function to update landmark in database
    fun updateLandmark(landmark: Landmark) {
        viewModelScope.launch {
            repository.update(landmark)
            updateCurrentLandmark(landmark)
        }
    }

    suspend fun getLastLandmarkId(): Int {
        return repository.getLastLandmarkId() ?: 0
    }
}

