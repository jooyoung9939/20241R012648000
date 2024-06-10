package com.example.lookatme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream

class AddClothesFragment : Fragment() {

    private lateinit var topsType: LinearLayout
    private lateinit var pantsType: LinearLayout
    private lateinit var shoesType: LinearLayout
    private lateinit var accessoriesType: LinearLayout
    private lateinit var defaultType: LinearLayout

    private lateinit var categoryButtons: List<Button>
    private lateinit var typeButtons: List<Button>
    private lateinit var addClothesButton: Button
    private lateinit var addClothesMemo: EditText
    private lateinit var addClothesImage: ImageView

    private lateinit var fetchDataViewModel: FetchDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_clothes, container, false)
        fetchDataViewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        val imageView: ImageView = view.findViewById(R.id.add_clothes_image)

        arguments?.getByteArray("image_bytes")?.let { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bitmap)
        }

        topsType = view.findViewById(R.id.tops_type)
        pantsType = view.findViewById(R.id.select_pants_type)
        shoesType = view.findViewById(R.id.select_shoes_type)
        accessoriesType = view.findViewById(R.id.select_accessories_type)
        defaultType = view.findViewById(R.id.default_type)
        addClothesButton = view.findViewById(R.id.add_clothes_button)
        addClothesMemo = view.findViewById(R.id.add_clothes_memo)
        addClothesImage = view.findViewById(R.id.add_clothes_image)

        val selectTopsButton: Button = view.findViewById(R.id.select_tops_button)
        val selectPantsButton: Button = view.findViewById(R.id.select_pants_button)
        val selectShoesButton: Button = view.findViewById(R.id.select_shoes_button)
        val selectAccessoriesButton: Button = view.findViewById(R.id.select_accessories_button)

        categoryButtons = listOf(selectTopsButton, selectPantsButton, selectShoesButton, selectAccessoriesButton)
        setupCategoryButton(selectTopsButton, topsType, "tops")
        setupCategoryButton(selectPantsButton, pantsType, "pants")
        setupCategoryButton(selectShoesButton, shoesType, "shoes")
        setupCategoryButton(selectAccessoriesButton, accessoriesType, "accessories")

        val selectTShirtsButton: Button = view.findViewById(R.id.select_t_shirts_button)
        val selectKaraTButton: Button = view.findViewById(R.id.select_kara_t_button)
        val selectManToManButton: Button = view.findViewById(R.id.select_man_to_man_button)
        val selectShirtsButton: Button = view.findViewById(R.id.select_shirts_button)
        val selectCottonPantsButton: Button = view.findViewById(R.id.select_cotton_pants_button)
        val selectJeansButton: Button = view.findViewById(R.id.select_jeans_button)
        val selectSlacksButton: Button = view.findViewById(R.id.select_slacks_button)
        val selectTrainingPantsButton: Button = view.findViewById(R.id.select_training_pants_button)
        val selectRunningShoesButton: Button = view.findViewById(R.id.select_running_shoes_button)
        val selectSnikersButton: Button = view.findViewById(R.id.select_snikers_button)
        val selectShoesShoesButton: Button = view.findViewById(R.id.select_shoes_shoes_button)
        val selectSandalButton: Button = view.findViewById(R.id.select_sandal_button)
        val selectCapButton: Button = view.findViewById(R.id.select_cap_button)
        val selectNecklaceButton: Button = view.findViewById(R.id.select_necklace_button)
        val selectEarringButton: Button = view.findViewById(R.id.select_earring_button)
        val selectBraceletButton: Button = view.findViewById(R.id.select_bracelet_button)

        typeButtons = listOf(
            selectTShirtsButton, selectKaraTButton, selectManToManButton, selectShirtsButton,
            selectCottonPantsButton, selectJeansButton, selectSlacksButton, selectTrainingPantsButton,
            selectRunningShoesButton, selectSnikersButton, selectShoesShoesButton, selectSandalButton,
            selectCapButton, selectNecklaceButton, selectEarringButton, selectBraceletButton
        )

        setupTypeButton(selectTShirtsButton)
        setupTypeButton(selectKaraTButton)
        setupTypeButton(selectManToManButton)
        setupTypeButton(selectShirtsButton)
        setupTypeButton(selectCottonPantsButton)
        setupTypeButton(selectJeansButton)
        setupTypeButton(selectSlacksButton)
        setupTypeButton(selectTrainingPantsButton)
        setupTypeButton(selectRunningShoesButton)
        setupTypeButton(selectSnikersButton)
        setupTypeButton(selectShoesShoesButton)
        setupTypeButton(selectSandalButton)
        setupTypeButton(selectCapButton)
        setupTypeButton(selectNecklaceButton)
        setupTypeButton(selectEarringButton)
        setupTypeButton(selectBraceletButton)

        addClothesButton.setOnClickListener {
            uploadClothes()
        }

        return view
    }

    private fun setupCategoryButton(button: Button, targetLayout: LinearLayout, category: String) {
        button.setOnClickListener {
            if (button.isSelected) {
                button.isSelected = false
                resetButtonState(button)
                targetLayout.visibility = View.GONE
                enableOtherButtons(categoryButtons)
                checkAllButtonsDisabled()
            } else {
                button.isSelected = true
                button.tag = category
                activateButtonState(button)
                showOnlySelectedLayout(targetLayout)
                disableOtherButtons(categoryButtons, button)
                defaultType.visibility = View.GONE
            }
        }
    }

    private fun setupTypeButton(button: Button) {
        button.setOnClickListener {
            if (button.isSelected) {
                button.isSelected = false
                resetButtonState(button)
                enableOtherButtons(typeButtons)
            } else {
                button.isSelected = true
                activateButtonState(button)
                disableOtherButtons(typeButtons, button)
            }
        }
    }

    private fun activateButtonState(button: Button) {
        button.setBackgroundResource(R.drawable.red_white_button_border)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }

    private fun resetButtonState(button: Button) {
        button.setBackgroundResource(R.drawable.border_gray_white_button)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
    }

    private fun disableOtherButtons(buttons: List<Button>, activeButton: Button) {
        buttons.forEach { button ->
            if (button != activeButton) {
                button.isEnabled = false
            }
        }
    }

    private fun enableOtherButtons(buttons: List<Button>) {
        buttons.forEach { button ->
            button.isEnabled = true
        }
    }

    private fun checkAllButtonsDisabled() {
        val allDisabled = categoryButtons.all { !it.isSelected }
        if (allDisabled) {
            defaultType.visibility = View.VISIBLE
        }
    }

    private fun showOnlySelectedLayout(targetLayout: LinearLayout) {
        topsType.visibility = if (targetLayout == topsType) View.VISIBLE else View.GONE
        pantsType.visibility = if (targetLayout == pantsType) View.VISIBLE else View.GONE
        shoesType.visibility = if (targetLayout == shoesType) View.VISIBLE else View.GONE
        accessoriesType.visibility = if (targetLayout == accessoriesType) View.VISIBLE else View.GONE
    }

    private fun uploadClothes() {
        val categoryButton = categoryButtons.find { it.isSelected }
        if (categoryButton == null) {
            Toast.makeText(requireContext(), "Please select a clothes category.", Toast.LENGTH_SHORT).show()
            return
        }

        val typeButton = typeButtons.find { it.isSelected }
        if (typeButton == null) {
            Toast.makeText(requireContext(), "Please select a clothes type.", Toast.LENGTH_SHORT).show()
            return
        }

        val category = categoryButton.tag as String
        val type = typeButton.text.toString()
        val memo = addClothesMemo.text.toString()

        val bitmap = (addClothesImage.drawable as BitmapDrawable).bitmap
        val file = File(requireContext().cacheDir, "clothes_image.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        fetchDataViewModel.uploadClothes(category, file.path, type, memo)
    }
}
