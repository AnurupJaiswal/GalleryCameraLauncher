package com.anurupjaiswal.gallerycamralauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anurupjaiswal.gallerycamralauncher.databinding.ActivityMainBinding
import com.anurupjaiswal.gallerycamralauncher.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // For ViewBinding

    // Declare activity result launchers
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val photo = result.data?.extras?.get("data") as Bitmap?
                binding.ivImage.setImageBitmap(photo)
                saveImage(photo)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                val photo = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                binding.ivImage.setImageBitmap(photo)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Initialize ViewBinding
        setContentView(binding.root)

        // Set listener for "Go" button
        binding.btnGo.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    // Show bottom sheet for camera/gallery selection
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // Use ViewBinding for bottom sheet layout
        val bottomSheetBinding: BottomSheetLayoutBinding =
            BottomSheetLayoutBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Set listeners for camera and gallery buttons
        bottomSheetBinding.ivCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            handlePermissionsForCamera()
        }

        bottomSheetBinding.ivGallery.setOnClickListener {
            bottomSheetDialog.dismiss()
            handlePermissionsForGallery()
        }

        bottomSheetDialog.show()
    }

    // Handle permissions and launch camera intent
    private fun handlePermissionsForCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA))
        } else {
            openCamera()
        }
    }

    private fun handlePermissionsForGallery() {
        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requiredPermissions.isNotEmpty()) {
            requestPermissions(requiredPermissions.toTypedArray())
        } else {
            openGallery()
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showBottomSheetDialog()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun saveImage(bitmap: Bitmap?) {
        bitmap?.let {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_.jpg"
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            val file = File(storageDir, imageFileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
