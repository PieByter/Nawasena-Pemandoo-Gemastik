package com.nawasena.pemandoo.database

import com.nawasena.pemandoo.api.ApiService
import com.nawasena.pemandoo.api.LandmarkResponseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class MapsRepository(private val apiService: ApiService) {

    suspend fun getAllLandmarks(): List<LandmarkResponseItem>? {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<LandmarkResponseItem>> = apiService.getAllLandmarks().execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun createLandmark(landmark: LandmarkResponseItem): LandmarkResponseItem? {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<LandmarkResponseItem> = apiService.createLandmark(landmark).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getLandmarkById(id: String): LandmarkResponseItem? {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<LandmarkResponseItem> = apiService.getLandmarkById(id).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
