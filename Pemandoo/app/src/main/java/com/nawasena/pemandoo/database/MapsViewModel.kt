package com.nawasena.pemandoo.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.nawasena.pemandoo.api.LandmarkResponseItem
import kotlinx.coroutines.Dispatchers

class MapsViewModel(private val repository: MapsRepository) : ViewModel() {

    private val _currentLandmark = MutableLiveData<LandmarkResponseItem>()
    val currentLandmark: LiveData<LandmarkResponseItem> get() = _currentLandmark

    fun fetchAllLandmarks() = liveData(Dispatchers.IO) {
        try {
            val landmarks = repository.getAllLandmarks()
            emit(landmarks ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun createLandmark(landmark: LandmarkResponseItem) = liveData(Dispatchers.IO) {
        try {
            val createdLandmark = repository.createLandmark(landmark)
            emit(createdLandmark)
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun getLandmarkById(id: String) = liveData(Dispatchers.IO) {
        try {
            val landmark = repository.getLandmarkById(id)
            emit(landmark)
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun fetchLandmarkDetails(landmarkId: String) = liveData(Dispatchers.IO) {
        try {
            val landmark = repository.getLandmarkById(landmarkId)
            emit(landmark)
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun setCurrentLandmark(landmark: LandmarkResponseItem) {
        _currentLandmark.postValue(landmark)
    }
}
