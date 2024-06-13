package mannequin

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.lookatme.R

class EditMannequinActivity : AppCompatActivity() {

    private lateinit var toEditBodySpecButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var editMannequinButton: Button

    private lateinit var changeSexButton: Button
    private lateinit var changeHairButton: Button
    private lateinit var changeSkinButton: Button

    private lateinit var changeMannequinSexText: TextView
    private lateinit var changeMannequinHairText: TextView
    private lateinit var changeMannequinSkinText: TextView

    private lateinit var mannequinHead: ImageView
    private lateinit var mannequinBody: ImageView
    private lateinit var mannequinLeftArm: ImageView
    private lateinit var mannequinRightArm: ImageView
    private lateinit var mannequinLegs: ImageView
    private lateinit var mannequinHair: ImageView

    private var currentSexIndex = 0
    private var currentHairIndex = 0
    private var currentSkinIndex = 0

    private var height = 175
    private var body = 65
    private var arm = 58
    private var leg = 90

    private lateinit var viewModel: MannequinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mannequin)

        viewModel = ViewModelProvider(this).get(MannequinViewModel::class.java)

        toEditBodySpecButton = findViewById(R.id.to_edit_body_spec_button)
        backButton = findViewById(R.id.back_button_from_edit_mannequin)
        editMannequinButton = findViewById(R.id.edit_mannequin_button)

        toEditBodySpecButton.setOnClickListener { showEditBodySpecDialog() }
        backButton.setOnClickListener { onBackPressed() }

        editMannequinButton.setOnClickListener {
            viewModel.editMannequin(currentSexIndex, currentHairIndex, currentSkinIndex, height, body, arm, leg)
        }

        changeSexButton = findViewById(R.id.change_sex_button)
        changeHairButton = findViewById(R.id.change_hair_button)
        changeSkinButton = findViewById(R.id.change_skin_button)

        changeMannequinSexText = findViewById(R.id.change_mannequin_sex_text)
        changeMannequinHairText = findViewById(R.id.change_mannequin_hair_text)
        changeMannequinSkinText = findViewById(R.id.change_mannequin_skin_text)

        mannequinHead = findViewById(R.id.mannequin_head)
        mannequinBody = findViewById(R.id.mannequin_body)
        mannequinLeftArm = findViewById(R.id.mannequin_left_arm)
        mannequinRightArm = findViewById(R.id.mannequin_right_arm)
        mannequinLegs = findViewById(R.id.mannequin_legs)
        mannequinHair = findViewById(R.id.mannequin_hair)

        updateButtonStates(changeSexButton)
        updateMannequinImages()

        changeSexButton.setOnClickListener {
            updateButtonStates(changeSexButton)
            updateTextViewVisibility(changeMannequinSexText)
        }

        changeHairButton.setOnClickListener {
            updateButtonStates(changeHairButton)
            updateTextViewVisibility(changeMannequinHairText)
        }

        changeSkinButton.setOnClickListener {
            updateButtonStates(changeSkinButton)
            updateTextViewVisibility(changeMannequinSkinText)
        }

        val changeDetailLeftButton: ImageButton = findViewById(R.id.change_mannequin_detail_left_button)
        val changeDetailRightButton: ImageButton = findViewById(R.id.change_mannequin_detail_right_button)

        changeDetailLeftButton.setOnClickListener { updateDetailTextView(-1) }
        changeDetailRightButton.setOnClickListener { updateDetailTextView(1) }

        viewModel.editResponse.observe(this, { response ->
            if (response != null) {
                Toast.makeText(this, "마네킹 수정 성공", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        viewModel.mannequinData.observe(this, { mannequin ->
            if (mannequin != null) {
                currentSexIndex = mannequin.sex
                currentHairIndex = mannequin.hair
                currentSkinIndex = mannequin.skinColor
                height = mannequin.height
                body = mannequin.body
                arm = mannequin.arm
                leg = mannequin.leg

                updateTexts()
                updateMannequinImages()
            }
        })

        viewModel.fetchMannequinData()
    }

    private fun showEditBodySpecDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_body_spec)

        val cancelEditBodySpecButton: ImageButton = dialog.findViewById(R.id.cancel_edit_body_spec_button)
        val heightEditText: EditText = dialog.findViewById(R.id.edit_height_text)
        val bodyEditText: EditText = dialog.findViewById(R.id.edit_body_text)
        val armEditText: EditText = dialog.findViewById(R.id.edit_arm_text)
        val legEditText: EditText = dialog.findViewById(R.id.edit_leg_text)
        val editBodySpecButton: Button = dialog.findViewById(R.id.edit_body_spec_button)

        heightEditText.setText(height.toString())
        bodyEditText.setText(body.toString())
        armEditText.setText(arm.toString())
        legEditText.setText(leg.toString())

        cancelEditBodySpecButton.setOnClickListener { dialog.dismiss() }

        editBodySpecButton.setOnClickListener {
            height = heightEditText.text.toString().toIntOrNull() ?: 175
            body = bodyEditText.text.toString().toIntOrNull() ?: 65
            arm = armEditText.text.toString().toIntOrNull() ?: 58
            leg = legEditText.text.toString().toIntOrNull() ?: 90

            dialog.dismiss()
            updateMannequinHeights()
        }

        dialog.window?.setDimAmount(0.5f)
        dialog.show()
    }

    private fun updateButtonStates(selectedButton: Button) {
        val buttons = listOf(changeSexButton, changeHairButton, changeSkinButton)

        for (button in buttons) {
            if (button == selectedButton) {
                button.setTextColor(resources.getColor(R.color.white))
                button.setBackgroundResource(R.drawable.border_mannequin_red)
                updateDetailTextView(0)
            } else {
                button.setTextColor(resources.getColor(R.color.gray))
                button.setBackgroundResource(R.drawable.border_mannequin_gray)
            }
        }
    }

    private fun updateTextViewVisibility(visibleTextView: TextView) {
        val textViews = listOf(changeMannequinSexText, changeMannequinHairText, changeMannequinSkinText)

        for (textView in textViews) {
            textView.visibility = if (textView == visibleTextView) TextView.VISIBLE else TextView.GONE
        }
    }

    private fun updateDetailTextView(direction: Int) {
        when {
            changeMannequinSexText.visibility == TextView.VISIBLE -> {
                currentSexIndex = (currentSexIndex + direction + 2) % 2
                changeMannequinSexText.text = if (currentSexIndex == 0) "남자" else "여자"
            }
            changeMannequinHairText.visibility == TextView.VISIBLE -> {
                currentHairIndex = (currentHairIndex + direction + 8) % 8
                changeMannequinHairText.text = "머리 ${currentHairIndex + 1}"
            }
            changeMannequinSkinText.visibility == TextView.VISIBLE -> {
                currentSkinIndex = (currentSkinIndex + direction + 4) % 4
                changeMannequinSkinText.text = "피부색 ${currentSkinIndex + 1}"
            }
        }
        updateMannequinImages()
    }

    private fun updateTexts() {
        changeMannequinSexText.text = if (currentSexIndex == 0) "남자" else "여자"
        changeMannequinHairText.text = "머리 ${currentHairIndex + 1}"
        changeMannequinSkinText.text = "피부색 ${currentSkinIndex + 1}"
    }

    private fun updateMannequinImages() {
        val sexPrefix = if (currentSexIndex == 0) "man" else "woman"
        val hairResource = resources.getIdentifier("${sexPrefix}_hair_${currentHairIndex + 1}", "drawable", packageName)
        val headResource = resources.getIdentifier("${sexPrefix}_head", "drawable", packageName)
        val bodyResource = resources.getIdentifier("${sexPrefix}_body", "drawable", packageName)
        val leftArmResource = resources.getIdentifier("${sexPrefix}_left_arm", "drawable", packageName)
        val rightArmResource = resources.getIdentifier("${sexPrefix}_right_arm", "drawable", packageName)
        val legsResource = resources.getIdentifier("${sexPrefix}_legs", "drawable", packageName)

        mannequinHair.setImageResource(hairResource)
        mannequinHead.setImageResource(headResource)
        mannequinBody.setImageResource(bodyResource)
        mannequinLeftArm.setImageResource(leftArmResource)
        mannequinRightArm.setImageResource(rightArmResource)
        mannequinLegs.setImageResource(legsResource)

        updateMannequinColors()
        updateMannequinHeights()
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

        mannequinBody.updateSize(bodyRatio)
        mannequinLeftArm.updateSize(armRatio)
        mannequinRightArm.updateSize(armRatio)
        mannequinLegs.updateSize(legRatio)
        mannequinHead.updateSize(headRatio)

        adjustPositions()
    }

    private fun adjustPositions() {
        val headParams = mannequinHead.layoutParams as RelativeLayout.LayoutParams
        headParams.topMargin = dpToPx(16)
        mannequinHead.layoutParams = headParams

        val bodyParams = mannequinBody.layoutParams as RelativeLayout.LayoutParams
        bodyParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        mannequinBody.layoutParams = bodyParams

        val leftArmParams = mannequinLeftArm.layoutParams as RelativeLayout.LayoutParams
        leftArmParams.addRule(RelativeLayout.START_OF, R.id.mannequin_body)
        leftArmParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        leftArmParams.topMargin = dpToPx(10)
        mannequinLeftArm.layoutParams = leftArmParams

        val rightArmParams = mannequinRightArm.layoutParams as RelativeLayout.LayoutParams
        rightArmParams.addRule(RelativeLayout.END_OF, R.id.mannequin_body)
        rightArmParams.addRule(RelativeLayout.BELOW, R.id.mannequin_head)
        rightArmParams.topMargin = dpToPx(10)
        mannequinRightArm.layoutParams = rightArmParams

        val legsParams = mannequinLegs.layoutParams as RelativeLayout.LayoutParams
        legsParams.addRule(RelativeLayout.BELOW, R.id.mannequin_body)
        mannequinLegs.layoutParams = legsParams

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
        this.layoutParams = layoutParams
        this.requestLayout()
    }

}
