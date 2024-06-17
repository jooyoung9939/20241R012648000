package lookbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.lookatme.MainActivity
import com.example.lookatme.R
import java.io.File
import java.io.FileOutputStream

class AddLookBookDetailActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var mannequinImageView: ImageView
    private lateinit var lookbookTops1ImageView: ImageView
    private lateinit var lookbookTops2ImageView: ImageView
    private lateinit var lookbookPantsImageView: ImageView
    private lateinit var lookbookShoesImageView: ImageView
    private lateinit var lookbookAccessories1ImageView: ImageView
    private lateinit var lookbookAccessories2ImageView: ImageView
    private lateinit var lookbookAccessories3ImageView: ImageView

    private lateinit var checkOpenButton: ImageButton
    private lateinit var checkOpenText: TextView
    private lateinit var lookbookTitleText: EditText
    private lateinit var lookbookMemoText: EditText
    private lateinit var addLookbookButton: Button

    private var show = true

    private lateinit var lookbookTypeLayout: LinearLayout
    private val buttonStateMap = mutableMapOf<Button, Boolean>()
    private lateinit var viewModel: LookBookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lookbook_detail)

        backButton = findViewById(R.id.back_button_from_add_lookbook_detail)
        mannequinImageView = findViewById(R.id.mannequin_image)
        lookbookTops1ImageView = findViewById(R.id.lookbook_tops_1_image_view)
        lookbookTops2ImageView = findViewById(R.id.lookbook_tops_2_image_view)
        lookbookPantsImageView = findViewById(R.id.lookbook_pants_image_view)
        lookbookShoesImageView = findViewById(R.id.lookbook_shoes_image_view)
        lookbookAccessories1ImageView = findViewById(R.id.lookbook_accessories_1_image_view)
        lookbookAccessories2ImageView = findViewById(R.id.lookbook_accessories_2_image_view)
        lookbookAccessories3ImageView = findViewById(R.id.lookbook_accessories_3_image_view)

        checkOpenButton = findViewById(R.id.check_open_button)
        checkOpenText = findViewById(R.id.check_open_text)
        lookbookTitleText = findViewById(R.id.lookbook_title_text)
        lookbookMemoText = findViewById(R.id.lookbook_memo_text)
        addLookbookButton = findViewById(R.id.add_loodbook_button)

        lookbookTypeLayout = findViewById(R.id.lookbook_type_layout)

        backButton.setOnClickListener { onBackPressed() }
        checkOpenButton.setOnClickListener { toggleShow() }
        addLookbookButton.setOnClickListener { validateAndUploadLookBook() }
        setupLookbookTypeButtons()

        viewModel = ViewModelProvider(this).get(LookBookViewModel::class.java)

        val base64Image = intent.getStringExtra("mannequin_image")
        val selectedTopsUrls = intent.getStringArrayExtra("selected_tops_urls")
        val selectedPantsUrl = intent.getStringExtra("selected_pants_url")
        val selectedShoesUrl = intent.getStringExtra("selected_shoes_url")
        val selectedAccessoriesUrls = intent.getStringArrayExtra("selected_accessories_urls")

        if (base64Image != null) {
            val bitmap = base64ToBitmap(base64Image)
            mannequinImageView.setImageBitmap(bitmap)
        }

        displaySelectedClothes(selectedTopsUrls, selectedPantsUrl, selectedShoesUrl, selectedAccessoriesUrls)

        viewModel.uploadResponse.observe(this, Observer { response ->
            if (response != null) {
                Toast.makeText(this, "룩북 업로드 성공", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("fragment_type", "profile")
                }
                startActivity(intent)
                finish()
            }
        })
    }


    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun displaySelectedClothes(
        topsUrls: Array<String>?,
        pantsUrl: String?,
        shoesUrl: String?,
        accessoriesUrls: Array<String>?
    ) {
        topsUrls?.let {
            if (it.isNotEmpty()) {
                loadImage(it[0], lookbookTops1ImageView)
                lookbookTops1ImageView.visibility = View.VISIBLE
            }
            if (it.size > 1) {
                loadImage(it[1], lookbookTops2ImageView)
                lookbookTops2ImageView.visibility = View.VISIBLE
            }
        }

        pantsUrl?.let {
            loadImage(it, lookbookPantsImageView)
            lookbookPantsImageView.visibility = View.VISIBLE
        }

        shoesUrl?.let {
            loadImage(it, lookbookShoesImageView)
            lookbookShoesImageView.visibility = View.VISIBLE
        }

        accessoriesUrls?.let {
            if (it.isNotEmpty()) {
                loadImage(it[0], lookbookAccessories1ImageView)
                lookbookAccessories1ImageView.visibility = View.VISIBLE
            }
            if (it.size > 1) {
                loadImage(it[1], lookbookAccessories2ImageView)
                lookbookAccessories2ImageView.visibility = View.VISIBLE
            }
            if (it.size > 2) {
                loadImage(it[2], lookbookAccessories3ImageView)
                lookbookAccessories3ImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun loadImage(url: String, imageView: ImageView) {
        val imageUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://${url}"
        }

        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(imageView)
    }

    private fun toggleShow() {
        show = !show
        if (show) {
            checkOpenButton.setImageResource(R.drawable.button_check)
            checkOpenText.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            checkOpenButton.setImageResource(R.drawable.button_uncheck)
            checkOpenText.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun setupLookbookTypeButtons() {
        val buttonIdsAndTags = listOf(
            Pair(R.id.select_street_button, "street"),
            Pair(R.id.select_hippie_button, "hippie"),
            Pair(R.id.select_normcore_button, "normcore"),
            Pair(R.id.select_grunge_button, "grunge"),
            Pair(R.id.select_punk_button, "punk"),
            Pair(R.id.select_preppy_button, "preppy"),
            Pair(R.id.select_military_button, "military"),
            Pair(R.id.select_amekaji_button, "amekaji"),
            Pair(R.id.select_cityboy_button, "cityboy"),
            Pair(R.id.select_minimal_button, "minimal"),
            Pair(R.id.select_techwear_button, "techwear"),
            Pair(R.id.select_athleisure_button, "athleisure")
        )

        buttonIdsAndTags.forEach { (id, tag) ->
            val button = findViewById<Button>(id)
            button.tag = tag
            buttonStateMap[button] = false
            button.setOnClickListener { toggleButtonState(button) }
        }
    }


    private fun toggleButtonState(button: Button) {
        val isChecked = buttonStateMap[button] ?: false
        if (isChecked) {
            button.background = ContextCompat.getDrawable(this, R.drawable.border_gray_white_button)
            button.setTextColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            button.background = ContextCompat.getDrawable(this, R.drawable.border_red_white_button)
            button.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
        buttonStateMap[button] = !isChecked
    }

    private fun validateAndUploadLookBook() {
        val selectedTopsIds = intent.getIntArrayExtra("selected_tops_ids")?.toList() ?: emptyList()
        val selectedPantsId = intent.getIntExtra("selected_pants_id", -1)
        val selectedShoesId = intent.getIntExtra("selected_shoes_id", -1)
        val selectedAccessoriesIds = intent.getIntArrayExtra("selected_accessories_ids")?.toList() ?: emptyList()
        val title = lookbookTitleText.text.toString()
        val type = buttonStateMap.filter { it.value }.keys.mapNotNull { it.tag as? String }

        if (selectedTopsIds.isEmpty()) {
            Toast.makeText(this, "상의를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPantsId == -1) {
            Toast.makeText(this, "하의를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedShoesId == -1) {
            Toast.makeText(this, "신발을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (title.isBlank()) {
            Toast.makeText(this, "룩북 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (type.isEmpty()) {
            Toast.makeText(this, "룩북 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = mannequinImageViewToBitmap(mannequinImageView)
        val imageUri = saveBitmapToFile(bitmap)

        viewModel.uploadLookBooks(
            selectedTopsIds,
            selectedPantsId,
            selectedShoesId,
            selectedAccessoriesIds,
            show,
            title,
            type,
            lookbookMemoText.text.toString(),
            imageUri.path ?: ""
        )
    }



    private fun mannequinImageViewToBitmap(imageView: ImageView): Bitmap {
        imageView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(imageView.drawingCache)
        imageView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "mannequin_image.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return Uri.fromFile(file)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}
