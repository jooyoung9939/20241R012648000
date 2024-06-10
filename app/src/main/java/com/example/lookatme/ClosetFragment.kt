package com.example.lookatme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream

class ClosetFragment : Fragment(), PhotoBottomSheetDialogFragment.OnPhotoOptionClickListener {
    private lateinit var rootView: ConstraintLayout
    private lateinit var callback: OnAddClothesButtonClickListener
    private lateinit var viewModel: FetchDataViewModel

    interface OnAddClothesButtonClickListener {
        fun onAddClothesButtonClicked()
        fun onBottomSheetDismissed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAddClothesButtonClickListener) {
            callback = context
        } else {
            throw RuntimeException("$context must implement OnAddClothesButtonClickListener")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_closet, container, false)
        rootView = view.findViewById(R.id.root_layout)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        val addClothesButton: ImageButton = view.findViewById(R.id.to_add_clothes_button)
        addClothesButton.setOnClickListener {
            showPhotoBottomSheetDialog()
        }

        parentFragmentManager.setFragmentResultListener("bottomSheetDismiss", viewLifecycleOwner) { _, _ ->
            callback.onBottomSheetDismissed()
        }

        return view
    }

    private fun showPhotoBottomSheetDialog() {
        val bottomSheetFragment = PhotoBottomSheetDialogFragment()
        bottomSheetFragment.setOnPhotoOptionClickListener(this)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

        callback.onAddClothesButtonClicked()
    }

    override fun onCameraOptionClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            openCamera()
        }
    }

    override fun onGalleryOptionClicked() {
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )

        if (!requiredPermissions.all { permission ->
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, REQUEST_GALLERY_PERMISSION)
        } else {
            openGallery()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }) {
                    openGallery()
                } else {
                    Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val file = saveBitmapToFile(imageBitmap)
                    removeBackground(file.absolutePath)
                }
                REQUEST_GALLERY -> {
                    val selectedImageUri: Uri? = data?.data
                    selectedImageUri?.let { uri ->
                        val filePath = getRealPathFromURI(requireContext(), uri)
                        filePath?.let { path ->
                            removeBackground(path)
                        } ?: run {
                            Toast.makeText(requireContext(), "Unable to get file path", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(requireContext().cacheDir, "captured_image.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    private fun removeBackground(imagePath: String) {
        val apiKey = "af3bff81c8a4ec9b3edf4b7f1ca65d016ba9b3b0eab2ceca902bb15d04362c6e6b792f9515a59789efe63dceedcd85b8"
        viewModel.removeBackground(apiKey, imagePath)
        viewModel.backgroundRemovalResponse.observe(viewLifecycleOwner) { bytes ->
            if (bytes != null) {
                val bundle = bundleOf("image_bytes" to bytes)
                val fragment = AddClothesFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment) // Use the correct container ID
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_GALLERY_PERMISSION = 2
        private const val REQUEST_CAMERA = 3
        private const val REQUEST_GALLERY = 4
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(column_index ?: 0)
        } finally {
            cursor?.close()
        }
    }
}
