package com.nawasena.pemandoo.database

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nawasena.pemandoo.api.ApiConfig
import com.nawasena.pemandoo.api.Coordinates
import com.nawasena.pemandoo.api.Description
import com.nawasena.pemandoo.api.LandmarkResponseItem
import com.nawasena.pemandoo.api.VisitorInformation
import com.nawasena.pemandoo.databinding.ActivityAddLandmarkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class AddLandmarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLandmarkBinding
    private lateinit var viewModel: MapsViewModel
    private var landmarkId: String? = null
    private var selectedImageUri: Uri? = null

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                selectedImageUri = uri
                binding.ivShowImage.setImageURI(uri)
            }
        }
    }

    private val launcherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.get("data")?.let { thumbnail ->
                selectedImageUri = saveImageToGallery(thumbnail as Bitmap)
                binding.ivShowImage.setImageBitmap(thumbnail)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLandmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = MapsRepository(ApiConfig.apiService)
        viewModel = ViewModelProvider(this, MapsViewModelFactory(repository))[MapsViewModel::class.java]

        binding.btnGallery.setOnClickListener {
            showImagePicker()
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.buttonSave.setOnClickListener {
            saveLandmark()
        }

        binding.buttonDelete.setOnClickListener {
            deleteLandmark()
        }

        landmarkId = intent.getStringExtra("landmark_id")
        if (!landmarkId.isNullOrEmpty()) {
            viewModel.getLandmarkById(landmarkId!!).observe(this) { landmark ->
                landmark?.let {
                    populateFields(it)
                }
            }
        }
    }

    private fun saveLandmark() {
        val name = binding.editTextName.text.toString()
        val latitude = binding.editTextLatitude.text.toString().toDoubleOrNull()
        val longitude = binding.editTextLongitude.text.toString().toDoubleOrNull()
        val description = binding.editTextDescription.text.toString()
        val architecturalFeatures = binding.editTextArchitecturalFeatures.text.toString()
        val interestingFacts = binding.editTextInterestingFacts.text.toString()
        val historicalSignificance = binding.editTextHistoricalSignificance.text.toString()
        val hours = binding.editTextHours.text.toString()
        val entryFee = binding.editTextEntryFee.text.toString()
        val geofenceRadius = binding.editTextGeofenceRadius.text.toString().toFloatOrNull()

        if (name.isNotEmpty() && latitude != null && longitude != null && geofenceRadius != null && selectedImageUri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val landmarkResponseItem = LandmarkResponseItem(
                        name = name,
                        coordinates = Coordinates(latitude, longitude),
                        description = Description(
                            overview = description,
                            architecturalFeatures = architecturalFeatures,
                            interestingFacts = interestingFacts,
                            historicalSignificance = historicalSignificance,
                            visitorInformation = VisitorInformation(
                                hours = hours,
                                entryFee = entryFee
                            )
                        ),
                        photo = selectedImageUri.toString()
                    )

                    val result = viewModel.createLandmark(landmarkResponseItem).value

                    if (result != null) {
                        Log.d("AddLandmarkActivity", "Landmark created: $result")
                        runOnUiThread {
                            Toast.makeText(this@AddLandmarkActivity, "Landmark added", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Log.e("AddLandmarkActivity", "Error creating landmark")
                        runOnUiThread {
                            Toast.makeText(this@AddLandmarkActivity, "Error creating landmark", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AddLandmarkActivity", "Error saving landmark", e)
                    runOnUiThread {
                        Toast.makeText(this@AddLandmarkActivity, "Error saving landmark", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this@AddLandmarkActivity, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLandmark() {
        // Implement delete functionality if necessary
    }

    private fun populateFields(landmark: LandmarkResponseItem) {
        binding.editTextName.setText(landmark.name)
        binding.editTextLatitude.setText((landmark.coordinates?.latitude as Double).toString())
        binding.editTextLongitude.setText((landmark.coordinates.longitude as Double).toString())
        binding.editTextDescription.setText(landmark.description?.overview)
        binding.editTextArchitecturalFeatures.setText(landmark.description?.architecturalFeatures)
        binding.editTextInterestingFacts.setText(landmark.description?.interestingFacts)
        binding.editTextHistoricalSignificance.setText(landmark.description?.historicalSignificance)
        binding.editTextHours.setText(landmark.description?.visitorInformation?.hours)
        binding.editTextEntryFee.setText(landmark.description?.visitorInformation?.entryFee)
        selectedImageUri = Uri.parse(landmark.photo)
        binding.ivShowImage.setImageURI(selectedImageUri)
    }

    private fun showImagePicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcherGallery.launch(galleryIntent)
    }

    private fun startCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcherCamera.launch(cameraIntent)
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri {
        val savedImageURL = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Landmark Image",
            "Image of the landmark"
        )
        return Uri.parse(savedImageURL)
    }
}
