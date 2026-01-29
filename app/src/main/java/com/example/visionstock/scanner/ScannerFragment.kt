package com.example.visionstock.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button // Or ImageView, depending on your XML
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.visionstock.LoadingActivity // Import LoadingActivity
import com.example.visionstock.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private var imageCapture: ImageCapture? = null // 1. Variable to hold capture capability
    private lateinit var cameraExecutor: ExecutorService

    // GALLERY LAUNCHER
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                goToResultPage(uri)
            }
        }
    }

    // HELPER: Send to Loading Screen -> Result Screen
    private fun goToResultPage(uri: Uri) {
        val intent = Intent(requireContext(), LoadingActivity::class.java)
        intent.putExtra("scanned_image_uri", uri.toString())
        startActivity(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()


        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }


        view.findViewById<View>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto()
        }

        // START CAMERA PERMISSION CHECK
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        // Get a stable reference to the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            requireContext().externalCacheDir,
            "scanned_image_${System.currentTimeMillis()}.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("ScannerFragment", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Capture Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Success! Convert file to URI and go to Loading Screen
                    val savedUri = Uri.fromFile(photoFile)
                    goToResultPage(savedUri)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 1. Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(requireView().findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            // 2. ImageCapture (This is new!)
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                // Bind Preview AND ImageCapture to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                Log.e("ScannerFragment", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}