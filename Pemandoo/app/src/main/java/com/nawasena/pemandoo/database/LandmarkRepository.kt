package com.nawasena.pemandoo.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class LandmarkRepository(private val landmarkDao: LandmarkDao) {

    val allLandmarks: LiveData<List<Landmark>> = landmarkDao.getAllLandmarks()

    @WorkerThread
    suspend fun insert(landmark: Landmark) {
        landmarkDao.insert(landmark)
    }

    @WorkerThread
    suspend fun update(landmark: Landmark) {
        landmarkDao.update(landmark)
    }

    @WorkerThread
    suspend fun delete(id: Int) {
        landmarkDao.deleteById(id)
    }

    fun getLandmarkById(id: Int): LiveData<Landmark?> {
        return landmarkDao.getLandmarkById(id)
    }

    suspend fun getCenterLandmark(): Landmark? {
        return landmarkDao.getCenterLandmark()
    }

    suspend fun getAllLandmarksWithDetails(): List<Landmark> {
        return landmarkDao.getAllLandmarksWithDetails()
    }

    suspend fun getLastLandmarkId(): Int? {
        return landmarkDao.getLastLandmarkId()
    }
}
