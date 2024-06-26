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
import com.nawasena.pemandoo.databinding.ActivityAddLandmarkBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class AddLandmarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLandmarkBinding
    private lateinit var viewModel: MapsViewModel
    private lateinit var repository: LandmarkRepository

    private var landmarkId: Int? = null
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

        val database = AppDatabase.getDatabase(applicationContext)
        repository = LandmarkRepository(database.landmarkDao())

        val factory = MapsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]

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

        landmarkId = intent.getIntExtra("landmark_id", -1)
        if (landmarkId != -1) {
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
        val geofenceRadius = binding.editTextGeofenceRadius.text.toString().toFloatOrNull()

        if (name.isNotEmpty() && latitude != null && longitude != null && geofenceRadius != null && selectedImageUri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val newId = if (landmarkId == null) {
                        val lastId = viewModel.getLastLandmarkId()
                        lastId + 1
                    } else {
                        landmarkId!!
                    }

                    val landmark = Landmark(
                        id = newId,
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        image = selectedImageUri.toString(),
                        description = description,
                        geofenceRadius = geofenceRadius
                    )

                    if (landmarkId == null) {
                        viewModel.insert(landmark)
                        Log.d("AddLandmarkActivity", "Landmark inserted: $landmark")
                        runOnUiThread {
                            Toast.makeText(this@AddLandmarkActivity, "Landmark added", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        viewModel.update(landmark)
                        Log.d("AddLandmarkActivity", "Landmark updated: $landmark")
                        runOnUiThread {
                            Toast.makeText(this@AddLandmarkActivity, "Landmark updated", Toast.LENGTH_SHORT).show()
                            finish()
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
        landmarkId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    viewModel.delete(id)
                    Log.d("AddLandmarkActivity", "Landmark deleted with ID: $id")
                    runOnUiThread {
                        Toast.makeText(this@AddLandmarkActivity, "Landmark deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("AddLandmarkActivity", "Error deleting landmark", e)
                    runOnUiThread {
                        Toast.makeText(this@AddLandmarkActivity, "Error deleting landmark", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun populateFields(landmark: Landmark) {
        binding.editTextName.setText(landmark.name)
        binding.editTextLatitude.setText(landmark.latitude.toString())
        binding.editTextLongitude.setText(landmark.longitude.toString())
        binding.editTextDescription.setText(landmark.description)
        binding.editTextGeofenceRadius.setText(landmark.geofenceRadius.toString())
        selectedImageUri = Uri.parse(landmark.image)
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
