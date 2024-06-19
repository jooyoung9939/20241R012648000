package closet

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.lookatme.MainActivity
import com.example.lookatme.R
import java.io.File
import java.io.FileOutputStream

class AddClothesActivity : AppCompatActivity() {

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
    private lateinit var cancelButton: ImageButton

    private lateinit var viewModel: ClosetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_clothes)

        viewModel = ViewModelProvider(this).get(ClosetViewModel::class.java)

        val imageView: ImageView = findViewById(R.id.add_clothes_image)

        intent?.getStringExtra("image_uri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            imageView.setImageBitmap(bitmap)
        }

        topsType = findViewById(R.id.tops_type)
        pantsType = findViewById(R.id.select_pants_type)
        shoesType = findViewById(R.id.select_shoes_type)
        accessoriesType = findViewById(R.id.select_accessories_type)
        defaultType = findViewById(R.id.default_type)
        addClothesButton = findViewById(R.id.add_clothes_button)
        addClothesMemo = findViewById(R.id.add_clothes_memo)
        addClothesImage = findViewById(R.id.add_clothes_image)
        cancelButton = findViewById(R.id.add_clothes_cancel_button)

        val selectTopsButton: Button = findViewById(R.id.select_tops_button)
        val selectPantsButton: Button = findViewById(R.id.select_pants_button)
        val selectShoesButton: Button = findViewById(R.id.select_shoes_button)
        val selectAccessoriesButton: Button = findViewById(R.id.select_accessories_button)

        categoryButtons = listOf(selectTopsButton, selectPantsButton, selectShoesButton, selectAccessoriesButton)
        setupCategoryButton(selectTopsButton, topsType, "tops")
        setupCategoryButton(selectPantsButton, pantsType, "pants")
        setupCategoryButton(selectShoesButton, shoesType, "shoes")
        setupCategoryButton(selectAccessoriesButton, accessoriesType, "accessories")

        val selectTShirtsButton: Button = findViewById(R.id.select_t_shirts_button)
        val selectKaraTButton: Button = findViewById(R.id.select_kara_t_button)
        val selectManToManButton: Button = findViewById(R.id.select_man_to_man_button)
        val selectShirtsButton: Button = findViewById(R.id.select_shirts_button)
        val selectCottonPantsButton: Button = findViewById(R.id.select_cotton_pants_button)
        val selectJeansButton: Button = findViewById(R.id.select_jeans_button)
        val selectSlacksButton: Button = findViewById(R.id.select_slacks_button)
        val selectTrainingPantsButton: Button = findViewById(R.id.select_training_pants_button)
        val selectRunningShoesButton: Button = findViewById(R.id.select_running_shoes_button)
        val selectSnikersButton: Button = findViewById(R.id.select_snikers_button)
        val selectShoesShoesButton: Button = findViewById(R.id.select_shoes_shoes_button)
        val selectSandalButton: Button = findViewById(R.id.select_sandal_button)
        val selectCapButton: Button = findViewById(R.id.select_cap_button)
        val selectNecklaceButton: Button = findViewById(R.id.select_necklace_button)
        val selectBagButton: Button = findViewById(R.id.select_bag_button)
        val selectBraceletButton: Button = findViewById(R.id.select_bracelet_button)

        typeButtons = listOf(
            selectTShirtsButton, selectKaraTButton, selectManToManButton, selectShirtsButton,
            selectCottonPantsButton, selectJeansButton, selectSlacksButton, selectTrainingPantsButton,
            selectRunningShoesButton, selectSnikersButton, selectShoesShoesButton, selectSandalButton,
            selectCapButton, selectNecklaceButton, selectBagButton, selectBraceletButton
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
        setupTypeButton(selectBagButton)
        setupTypeButton(selectBraceletButton)

        addClothesButton.setOnClickListener {
            uploadClothes()
        }

        cancelButton.setOnClickListener {
            cancelAndReturn()
        }

        viewModel.uploadResponse.observe(this, Observer { response ->
            if (response != null) {
                Toast.makeText(this, "옷 업로드 성공", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("fragment_type", "closet")
                }
                startActivity(intent)
                finish()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
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
        button.setBackgroundResource(R.drawable.border_red_white_button)
        button.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    private fun resetButtonState(button: Button) {
        button.setBackgroundResource(R.drawable.border_gray_white_button)
        button.setTextColor(ContextCompat.getColor(this, R.color.gray))
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
            Toast.makeText(this, "옷 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val typeButton = typeButtons.find { it.isSelected }
        if (typeButton == null) {
            Toast.makeText(this, "옷 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val category = categoryButton.tag as String
        val type = typeButton.text.toString()
        val memo = addClothesMemo.text.toString()

        val bitmap = (addClothesImage.drawable as BitmapDrawable).bitmap
        val file = File(this.cacheDir, "clothes_image.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        viewModel.uploadClothes(category, file.path, type, memo)
    }

    private fun cancelAndReturn() {
        finish()
    }
}
