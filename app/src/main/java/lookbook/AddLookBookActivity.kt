package lookbook

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.leanback.widget.HorizontalGridView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.lookatme.R
import android.widget.Button
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class AddLookBookActivity : AppCompatActivity(), AddLookBookAdapter.OnItemClickListener {

    private lateinit var backButton: ImageButton
    private lateinit var mannequinPalette: FrameLayout
    private lateinit var topsGridView: HorizontalGridView
    private lateinit var pantsGridView: HorizontalGridView
    private lateinit var shoesGridView: HorizontalGridView
    private lateinit var accessoriesGridView: HorizontalGridView

    private lateinit var viewModel: LookBookViewModel

    private lateinit var mannequinHead: ImageView
    private lateinit var mannequinBody: ImageView
    private lateinit var mannequinLeftArm: ImageView
    private lateinit var mannequinRightArm: ImageView
    private lateinit var mannequinLegs: ImageView
    private lateinit var mannequinHair: ImageView

    private lateinit var mannequinTopsImageView1: ImageView
    private lateinit var mannequinTopsImageView2: ImageView
    private lateinit var mannequinPantsImageView: ImageView
    private lateinit var mannequinShoesImageView: ImageView
    private lateinit var mannequinAccessoriesImageView1: ImageView
    private lateinit var mannequinAccessoriesImageView2: ImageView
    private lateinit var mannequinAccessoriesImageView3: ImageView

    private var currentSexIndex = 0
    private var currentHairIndex = 0
    private var currentSkinIndex = 0

    private var height = 175
    private var body = 65
    private var arm = 58
    private var leg = 90

    private var mannequinTopsSelection: MutableList<LookBookClothesItem> = mutableListOf()
    private var mannequinPantsSelection: LookBookClothesItem? = null
    private var mannequinShoesSelection: LookBookClothesItem? = null
    private var mannequinAccessoriesSelection: MutableList<LookBookClothesItem> = mutableListOf()

    private lateinit var topsAdapter: AddLookBookAdapter
    private lateinit var pantsAdapter: AddLookBookAdapter
    private lateinit var shoesAdapter: AddLookBookAdapter
    private lateinit var accessoriesAdapter: AddLookBookAdapter

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var lastTouchedView: View? = null

    private var formState = 0 // 0 for the first state, 1 for the second state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lookbook)

        backButton = findViewById(R.id.back_button_from_add_lookbook)
        mannequinPalette = findViewById(R.id.mannequin_palette)
        topsGridView = findViewById(R.id.add_lookbook_tops_gridview)
        pantsGridView = findViewById(R.id.add_lookbook_pants_gridview)
        shoesGridView = findViewById(R.id.add_lookbook_shoes_gridview)
        accessoriesGridView = findViewById(R.id.add_lookbook_accessories_gridview)

        mannequinHead = findViewById(R.id.mannequin_head)
        mannequinBody = findViewById(R.id.mannequin_body)
        mannequinLeftArm = findViewById(R.id.mannequin_left_arm)
        mannequinRightArm = findViewById(R.id.mannequin_right_arm)
        mannequinLegs = findViewById(R.id.mannequin_legs)
        mannequinHair = findViewById(R.id.mannequin_hair)

        mannequinTopsImageView1 = findViewById(R.id.mannequin_tops_1_image_view)
        mannequinTopsImageView2 = findViewById(R.id.mannequin_tops_2_image_view)
        mannequinPantsImageView = findViewById(R.id.mannequin_pants_image_view)
        mannequinShoesImageView = findViewById(R.id.mannequin_shoes_image_view)
        mannequinAccessoriesImageView1 = findViewById(R.id.mannequin_accessories_1_image_view)
        mannequinAccessoriesImageView2 = findViewById(R.id.mannequin_accessories_2_image_view)
        mannequinAccessoriesImageView3 = findViewById(R.id.mannequin_accessories_3_image_view)

        val draggableViews = listOf(
            mannequinTopsImageView1, mannequinTopsImageView2, mannequinPantsImageView,
            mannequinShoesImageView, mannequinAccessoriesImageView1, mannequinAccessoriesImageView2, mannequinAccessoriesImageView3
        )

        draggableViews.forEach { view ->
            view.setOnLongClickListener { startDrag(it) }
            view.setOnTouchListener { v, event ->
                lastTouchedView = v
                scaleGestureDetector.onTouchEvent(event)
                false
            }
        }

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        mannequinPalette.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val droppedView = dragEvent.localState as View
                    val owner = droppedView.parent as ViewGroup
                    owner.removeView(droppedView)
                    val container = view as FrameLayout
                    val params = FrameLayout.LayoutParams(droppedView.width, droppedView.height)
                    params.leftMargin = dragEvent.x.toInt() - (droppedView.width / 2)
                    params.topMargin = dragEvent.y.toInt() - (droppedView.height / 2)
                    container.addView(droppedView, params)
                    droppedView.visibility = View.VISIBLE
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    view.invalidate()
                    true
                }
                else -> false
            }
        }

        backButton.setOnClickListener { onBackPressed() }

        findViewById<Button>(R.id.to_add_loobook_detail_button).setOnClickListener {
            goToLookBookDetail()
        }

        findViewById<ImageButton>(R.id.change_form_button).setOnClickListener {
            toggleForm()
        }

        viewModel = ViewModelProvider(this).get(LookBookViewModel::class.java)

        viewModel.mannequinData.observe(this, { mannequin ->
            if (mannequin != null) {
                currentSexIndex = mannequin.sex
                currentHairIndex = mannequin.hair
                currentSkinIndex = mannequin.skinColor
                height = mannequin.height
                body = mannequin.body
                arm = mannequin.arm
                leg = mannequin.leg

                updateMannequinImages()
            }
        })

        viewModel.fetchMannequinData()

        // Fetch and observe data
        viewModel.getClothes("tops").observe(this, Observer { data ->
            topsAdapter = AddLookBookAdapter(this, data, "tops", this)
            topsGridView.adapter = topsAdapter
        })

        viewModel.getClothes("pants").observe(this, Observer { data ->
            pantsAdapter = AddLookBookAdapter(this, data, "pants", this)
            pantsGridView.adapter = pantsAdapter
        })

        viewModel.getClothes("shoes").observe(this, Observer { data ->
            shoesAdapter = AddLookBookAdapter(this, data, "shoes", this)
            shoesGridView.adapter = shoesAdapter
        })

        viewModel.getClothes("accessories").observe(this, Observer { data ->
            accessoriesAdapter = AddLookBookAdapter(this, data, "accessories", this)
            accessoriesGridView.adapter = accessoriesAdapter
        })
    }

    private fun startDrag(view: View): Boolean {
        val item = ClipData.Item(view.tag as? CharSequence)
        val dragData = ClipData(view.tag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
        val myShadow = View.DragShadowBuilder(view)
        view.startDragAndDrop(dragData, myShadow, view, 0)
        return true
    }

    private fun toggleForm() {
        formState = (formState + 1) % 2
        updateMannequinImages()
    }

    override fun onItemClick(item: LookBookClothesItem, category: String) {
        when (category) {
            "tops" -> {
                handleMannequinTopsSelection(item)
            }
            "pants" -> {
                handleMannequinPantsSelection(item)
            }
            "shoes" -> {
                handleMannequinShoesSelection(item)
            }
            "accessories" -> {
                handleMannequinAccessoriesSelection(item)
            }
        }
    }

    private fun handleMannequinTopsSelection(item: LookBookClothesItem) {
        if (mannequinTopsSelection.contains(item)) {
            // Deactivate item
            mannequinTopsSelection.remove(item)
            topsAdapter.deactivateItem(item)
            updateMannequinTopsImageViews()
        } else {
            // Activate item
            if (mannequinTopsSelection.size < 2) {
                mannequinTopsSelection.add(item)
                topsAdapter.activateItem(item)
                updateMannequinTopsImageViews()
            }
        }
    }

    private fun updateMannequinTopsImageViews() {
        val imageViews = listOf(mannequinTopsImageView1, mannequinTopsImageView2)
        imageViews.forEach { it.visibility = View.GONE }
        mannequinTopsSelection.forEachIndexed { index, item ->
            val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
                item.url
            } else {
                "https://${item.url}"
            }
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageViews[index])
            imageViews[index].visibility = View.VISIBLE
        }
    }

    private fun handleMannequinPantsSelection(item: LookBookClothesItem) {
        if (mannequinPantsSelection == item) {
            // Deactivate item
            mannequinPantsSelection = null
            pantsAdapter.deactivateItem(item)
            mannequinPantsImageView.visibility = View.GONE
        } else {
            // Deactivate any previously selected item
            mannequinPantsSelection?.let { previousItem ->
                pantsAdapter.deactivateItem(previousItem)
            }
            // Activate new item
            mannequinPantsSelection = item
            pantsAdapter.activateItem(item)
            val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
                item.url
            } else {
                "https://${item.url}"
            }
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(mannequinPantsImageView)
            mannequinPantsImageView.visibility = View.VISIBLE
        }
    }

    private fun handleMannequinShoesSelection(item: LookBookClothesItem) {
        if (mannequinShoesSelection == item) {
            // Deactivate item
            mannequinShoesSelection = null
            shoesAdapter.deactivateItem(item)
            mannequinShoesImageView.visibility = View.GONE
        } else {
            // Deactivate any previously selected item
            mannequinShoesSelection?.let { previousItem ->
                shoesAdapter.deactivateItem(previousItem)
            }
            // Activate new item
            mannequinShoesSelection = item
            shoesAdapter.activateItem(item)
            val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
                item.url
            } else {
                "https://${item.url}"
            }
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(mannequinShoesImageView)
            mannequinShoesImageView.visibility = View.VISIBLE
        }
    }

    private fun handleMannequinAccessoriesSelection(item: LookBookClothesItem) {
        if (mannequinAccessoriesSelection.contains(item)) {
            // Deactivate item
            mannequinAccessoriesSelection.remove(item)
            accessoriesAdapter.deactivateItem(item)
            updateMannequinAccessoriesImageViews()
        } else {
            // Activate item
            if (mannequinAccessoriesSelection.size < 3) {
                mannequinAccessoriesSelection.add(item)
                accessoriesAdapter.activateItem(item)
                updateMannequinAccessoriesImageViews()
            }
        }
    }

    private fun updateMannequinAccessoriesImageViews() {
        val imageViews = listOf(mannequinAccessoriesImageView1, mannequinAccessoriesImageView2, mannequinAccessoriesImageView3)
        imageViews.forEach { it.visibility = View.GONE }
        mannequinAccessoriesSelection.forEachIndexed { index, item ->
            val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
                item.url
            } else {
                "https://${item.url}"
            }
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageViews[index])
            imageViews[index].visibility = View.VISIBLE
        }
    }

    private fun updateMannequinImages() {
        val sexPrefix = if (currentSexIndex == 0) "man" else "woman"
        val hairResource = resources.getIdentifier("${sexPrefix}_hair_${currentHairIndex + 1}", "drawable", packageName)
        val headResource = resources.getIdentifier("${sexPrefix}_head", "drawable", packageName)
        val bodyResource = resources.getIdentifier("${sexPrefix}_body", "drawable", packageName)
        val leftArmResource = resources.getIdentifier("${sexPrefix}_left_arm${if (formState == 1) "_t" else ""}", "drawable", packageName)
        val rightArmResource = resources.getIdentifier("${sexPrefix}_right_arm${if (formState == 1) "_t" else ""}", "drawable", packageName)
        val legsResource = resources.getIdentifier("${sexPrefix}_legs", "drawable", packageName)

        mannequinHair.setImageResource(hairResource)
        mannequinHead.setImageResource(headResource)
        mannequinBody.setImageResource(bodyResource)
        mannequinLeftArm.setImageResource(leftArmResource)
        mannequinRightArm.setImageResource(rightArmResource)
        mannequinLegs.setImageResource(legsResource)

        updateMannequinColors()
        updateMannequinHeights()
        halveAllImageSizes()
    }

    private fun halveAllImageSizes() {
        mannequinHead.halveSize()
        mannequinBody.halveSize()
        mannequinLeftArm.halveSize()
        mannequinRightArm.halveSize()
        mannequinLegs.halveSize()
        mannequinHair.halveSize()
    }

    private fun ImageView.halveSize() {
        val layoutParams = this.layoutParams as RelativeLayout.LayoutParams
        layoutParams.width = (layoutParams.width * 0.5).toInt()
        layoutParams.height = (layoutParams.height * 0.5).toInt()
        this.layoutParams = layoutParams
        this.requestLayout()
    }

    private fun updateMannequinColors() {
        val skinColor = when (currentSkinIndex) {
            0 -> Color.parseColor("#fdece2")
            1 -> Color.parseColor("#f4e3da")
            2 -> Color.parseColor("#f4d9cb")
            3 -> Color.parseColor("#ddc8bd")
            else -> Color.parseColor("#fdece2")
        }

        mannequinHead.setColorFilter(skinColor, PorterDuff.Mode.SRC_IN)
        mannequinBody.setColorFilter(skinColor, PorterDuff.Mode.SRC_IN)
        mannequinLeftArm.setColorFilter(skinColor, PorterDuff.Mode.SRC_IN)
        mannequinRightArm.setColorFilter(skinColor, PorterDuff.Mode.SRC_IN)
        mannequinLegs.setColorFilter(skinColor, PorterDuff.Mode.SRC_IN)
    }

    private fun updateMannequinHeights() {
        val baseBody = 65
        val baseArm = 58
        val baseLeg = 90
        val baseHead = 20

        val bodyRatio = body.toFloat() / baseBody
        val armRatio = arm.toFloat() / baseArm
        val legRatio = leg.toFloat() / baseLeg
        val headHeight = height - body - leg
        val headRatio = headHeight.toFloat() / baseHead

        mannequinHair.updateSize(headRatio)
        mannequinBody.updateSize(bodyRatio)
        mannequinLeftArm.updateSize(armRatio)
        mannequinRightArm.updateSize(armRatio)
        mannequinLegs.updateSize(legRatio)
        mannequinHead.updateSize(headRatio)

        adjustPositions()
    }

    private fun adjustPositions() {
        val hairParams = mannequinHair.layoutParams as RelativeLayout.LayoutParams
        hairParams.topMargin = dpToPx(50)
        mannequinHair.layoutParams = hairParams

        val headParams = mannequinHead.layoutParams as RelativeLayout.LayoutParams
        headParams.topMargin = dpToPx(60)
        mannequinHead.layoutParams = headParams

        val bodyParams = mannequinBody.layoutParams as RelativeLayout.LayoutParams
        bodyParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        mannequinBody.layoutParams = bodyParams

        val leftArmParams = mannequinLeftArm.layoutParams as RelativeLayout.LayoutParams
        leftArmParams.addRule(RelativeLayout.START_OF, R.id.mannequin_body)
        leftArmParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        leftArmParams.topMargin = if (formState == 1) dpToPx(0) else dpToPx(5)
        mannequinLeftArm.layoutParams = leftArmParams

        val rightArmParams = mannequinRightArm.layoutParams as RelativeLayout.LayoutParams
        rightArmParams.addRule(RelativeLayout.END_OF, R.id.mannequin_body)
        rightArmParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        rightArmParams.topMargin = if (formState == 1) dpToPx(0) else dpToPx(5)
        mannequinRightArm.layoutParams = rightArmParams

        val legsParams = mannequinLegs.layoutParams as RelativeLayout.LayoutParams
        legsParams.addRule(RelativeLayout.BELOW, R.id.mannequin_body)
        mannequinLegs.layoutParams = legsParams

        mannequinHair.requestLayout()
        mannequinHead.requestLayout()
        mannequinBody.requestLayout()
        mannequinLeftArm.requestLayout()
        mannequinRightArm.requestLayout()
        mannequinLegs.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
    }

    private fun ImageView.updateSize(ratio: Float) {
        val layoutParams = this.layoutParams as RelativeLayout.LayoutParams
        layoutParams.height = (this.drawable.intrinsicHeight * ratio).toInt()
        layoutParams.width = (this.drawable.intrinsicWidth * ratio).toInt()
        this.layoutParams = layoutParams
        this.requestLayout()
    }

    private fun goToLookBookDetail() {
        if (mannequinTopsSelection.isEmpty()) {
            Toast.makeText(this, "상의를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (mannequinPantsSelection == null) {
            Toast.makeText(this, "하의를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (mannequinShoesSelection == null) {
            Toast.makeText(this, "신발을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = captureView(mannequinPalette)
        val filePath = saveBitmapToFile(bitmap, "mannequin_image.png")

        val selectedTopsIds = mannequinTopsSelection.map { it.id }.toIntArray()
        val selectedTopsUrls = mannequinTopsSelection.map { it.url }.toTypedArray()
        val selectedPantsId = mannequinPantsSelection?.id ?: -1
        val selectedPantsUrl = mannequinPantsSelection?.url
        val selectedShoesId = mannequinShoesSelection?.id ?: -1
        val selectedShoesUrl = mannequinShoesSelection?.url
        val selectedAccessoriesIds = mannequinAccessoriesSelection.map { it.id }.toIntArray()
        val selectedAccessoriesUrls = mannequinAccessoriesSelection.map { it.url }.toTypedArray()

        val intent = Intent(this, AddLookBookDetailActivity::class.java).apply {
            putExtra("mannequin_image_path", filePath)
            putExtra("selected_tops_ids", selectedTopsIds)
            putExtra("selected_tops_urls", selectedTopsUrls)
            putExtra("selected_pants_id", selectedPantsId)
            putExtra("selected_pants_url", selectedPantsUrl)
            putExtra("selected_shoes_id", selectedShoesId)
            putExtra("selected_shoes_url", selectedShoesUrl)
            putExtra("selected_accessories_ids", selectedAccessoriesIds)
            putExtra("selected_accessories_urls", selectedAccessoriesUrls)
        }

        startActivity(intent)
    }


    private fun captureView(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): String {
        val file = File(cacheDir, fileName)
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file.absolutePath
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var scaleFactor = 1.0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

            lastTouchedView?.apply {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            return true
        }
    }
}
