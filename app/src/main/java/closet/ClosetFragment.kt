package closet

import android.Manifest
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.lookatme.R
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

class ClosetFragment : Fragment(), PhotoBottomSheetDialogFragment.OnPhotoOptionClickListener {

    private lateinit var rootView: RelativeLayout
    private lateinit var callback: OnToAddClothesButtonClickListener
    private lateinit var viewModel: ClosetViewModel

    private lateinit var topsGridView: GridView
    private lateinit var pantsGridView: GridView
    private lateinit var shoesGridView: GridView
    private lateinit var accessoriesGridView: GridView

    private lateinit var topsButton: Button
    private lateinit var pantsButton: Button
    private lateinit var shoesButton: Button
    private lateinit var accessoriesButton: Button

    interface OnToAddClothesButtonClickListener {
        fun onToAddClothesButtonClicked()
        fun onBottomSheetDismissed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnToAddClothesButtonClickListener) {
            callback = context
        } else {
            throw RuntimeException("$context must implement OnAddClothesButtonClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_closet, container, false)
        rootView = view.findViewById(R.id.closet_fragment)

        viewModel = ViewModelProvider(this).get(ClosetViewModel::class.java)

        topsGridView = view.findViewById(R.id.tops_gridview)
        pantsGridView = view.findViewById(R.id.pants_gridview)
        shoesGridView = view.findViewById(R.id.shoes_gridview)
        accessoriesGridView = view.findViewById(R.id.accessories_gridview)

        topsButton = view.findViewById(R.id.choose_tops_button)
        pantsButton = view.findViewById(R.id.choose_pants_button)
        shoesButton = view.findViewById(R.id.choose_shoes_button)
        accessoriesButton = view.findViewById(R.id.choose_accessories_button)

        topsButton.setOnClickListener { onCategoryButtonClicked(topsButton, "tops", topsGridView) }
        pantsButton.setOnClickListener { onCategoryButtonClicked(pantsButton, "pants", pantsGridView) }
        shoesButton.setOnClickListener { onCategoryButtonClicked(shoesButton, "shoes", shoesGridView) }
        accessoriesButton.setOnClickListener { onCategoryButtonClicked(accessoriesButton, "accessories", accessoriesGridView) }

        // Set default selection
        onCategoryButtonClicked(topsButton, "tops", topsGridView)

        topsGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = topsGridView.adapter.getItem(position) as ClothesItem
            navigateToDetailFragment("tops", selectedItem.id, selectedItem.url)
        }

        pantsGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = pantsGridView.adapter.getItem(position) as ClothesItem
            navigateToDetailFragment("pants", selectedItem.id, selectedItem.url)
        }

        shoesGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = shoesGridView.adapter.getItem(position) as ClothesItem
            navigateToDetailFragment("shoes", selectedItem.id, selectedItem.url)
        }

        accessoriesGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = accessoriesGridView.adapter.getItem(position) as ClothesItem
            navigateToDetailFragment("accessories", selectedItem.id, selectedItem.url)
        }

        val addClothesButton: ImageButton = view.findViewById(R.id.to_add_clothes_button)
        addClothesButton.setOnClickListener {
            showPhotoBottomSheetDialog()
        }

        parentFragmentManager.setFragmentResultListener("bottomSheetDismiss", viewLifecycleOwner) { _, _ ->
            callback.onBottomSheetDismissed()
        }

        return view
    }

    private fun onCategoryButtonClicked(button: Button, category: String, gridView: GridView) {
        resetButtonStates()
        button.setBackgroundResource(R.drawable.border_red_white_button)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))

        topsGridView.visibility = if (gridView == topsGridView) View.VISIBLE else View.GONE
        pantsGridView.visibility = if (gridView == pantsGridView) View.VISIBLE else View.GONE
        shoesGridView.visibility = if (gridView == shoesGridView) View.VISIBLE else View.GONE
        accessoriesGridView.visibility = if (gridView == accessoriesGridView) View.VISIBLE else View.GONE

        loadClothes(category, gridView)
    }

    private fun resetButtonStates() {
        topsButton.setBackgroundResource(R.drawable.border_gray_white_button)
        topsButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        pantsButton.setBackgroundResource(R.drawable.border_gray_white_button)
        pantsButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        shoesButton.setBackgroundResource(R.drawable.border_gray_white_button)
        shoesButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        accessoriesButton.setBackgroundResource(R.drawable.border_gray_white_button)
        accessoriesButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
    }

    private fun loadClothes(category: String, gridView: GridView) {
        viewModel.getClothes(category).observe(viewLifecycleOwner, Observer { clothes ->
            val adapter = ClosetAdapter(requireContext(), clothes)
            gridView.adapter = adapter
        })
    }

    private fun navigateToDetailFragment(category: String, id: Int, url: String) {
        val fragment = ClothesDetailFragment().apply {
            arguments = Bundle().apply {
                putString("category", category)
                putInt("id", id)
                putString("url", url)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack("ClosetFragment")  // Add tag here
            .commit()
    }

    private fun showPhotoBottomSheetDialog() {
        val bottomSheetFragment = PhotoBottomSheetDialogFragment()
        bottomSheetFragment.setOnPhotoOptionClickListener(this)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

        callback.onToAddClothesButtonClicked()
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
        val apiKey = getApiKeyFromConfig()
        viewModel.removeBackground(apiKey, imagePath)
        viewModel.backgroundRemovalResponse.observe(viewLifecycleOwner) { bytes ->
            if (bytes != null) {
                val file = saveBytesToFile(bytes)
                val uri = FileProvider.getUriForFile(requireContext(), "com.example.lookatme.fileprovider", file)
                val intent = Intent(requireContext(), AddClothesActivity::class.java).apply {
                    putExtra("image_uri", uri.toString())
                }
                startActivity(intent)
            }
        }
    }

    private fun saveBytesToFile(bytes: ByteArray): File {
        val file = File(requireContext().cacheDir, "processed_image.png")
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
        }
        return file
    }

    private fun getApiKeyFromConfig(): String {
        val properties = Properties()
        val inputStream = requireContext().assets.open("config.properties")
        properties.load(inputStream)
        return properties.getProperty("API_KEY")
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
