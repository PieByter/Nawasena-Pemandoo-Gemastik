package com.nawasena.pemandoo.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LandmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(landmark: Landmark)

    @Update
    suspend fun update(landmark: Landmark)

    @Query("DELETE FROM landmarks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM landmarks")
    fun getAllLandmarks(): LiveData<List<Landmark>>

    @Query("SELECT * FROM landmarks WHERE id = :id")
    fun getLandmarkById(id: Int): LiveData<Landmark?>

    @Query("SELECT * FROM landmarks ORDER BY id LIMIT 1")
    suspend fun getCenterLandmark(): Landmark?

    @Query("SELECT * FROM landmarks")
    suspend fun getAllLandmarksWithDetails(): List<Landmark>

    @Query("SELECT MAX(id) FROM landmarks")
    suspend fun getLastLandmarkId(): Int?
}

