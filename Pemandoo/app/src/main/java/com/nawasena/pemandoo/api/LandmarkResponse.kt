package com.nawasena.pemandoo.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LandmarkResponse(

	@field:SerializedName("LandmarkResponse")
	val landmarkResponse: List<LandmarkResponseItem?>? = null
) : Parcelable

@Parcelize
data class Description(

	@field:SerializedName("overview")
	val overview: String? = null,

	@field:SerializedName("architectural_features")
	val architecturalFeatures: String? = null,

	@field:SerializedName("interesting_facts")
	val interestingFacts: String? = null,

	@field:SerializedName("historical_significance")
	val historicalSignificance: String? = null,

	@field:SerializedName("visitor_information")
	val visitorInformation: VisitorInformation? = null
) : Parcelable

@Parcelize
data class VisitorInformation(

	@field:SerializedName("hours")
	val hours: String? = null,

	@field:SerializedName("entry_fee")
	val entryFee: String? = null
) : Parcelable

@Parcelize
data class LandmarkResponseItem(

	@field:SerializedName("__v")
	val v: Int? = null,

	@field:SerializedName("coordinates")
	val coordinates: Coordinates? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: Description? = null,

	@field:SerializedName("photo")
	val photo: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
) : Parcelable

@Parcelize
data class Coordinates(

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
) : Parcelable
