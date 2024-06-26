package com.nawasena.pemandoo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "landmarks")
data class Landmark(
    @PrimaryKey var id: Int = 0,
    var name: String,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val description: String,
    val geofenceRadius: Float
)

