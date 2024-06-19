package scrap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.lookatme.R

class SavedClothesActivity : AppCompatActivity() {

    private lateinit var topsButton: Button
    private lateinit var pantsButton: Button
    private lateinit var shoesButton: Button
    private lateinit var accessoriesButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var topsGridView: GridView
    private lateinit var pantsGridView: GridView
    private lateinit var shoesGridView: GridView
    private lateinit var accessoriesGridView: GridView

    private lateinit var scrapViewModel: ScrapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_clothes)

        topsButton = findViewById(R.id.choose_tops_button)
        pantsButton = findViewById(R.id.choose_pants_button)
        shoesButton = findViewById(R.id.choose_shoes_button)
        accessoriesButton = findViewById(R.id.choose_accessories_button)

        topsGridView = findViewById(R.id.tops_gridview)
        pantsGridView = findViewById(R.id.pants_gridview)
        shoesGridView = findViewById(R.id.shoes_gridview)
        accessoriesGridView = findViewById(R.id.accessories_gridview)

        scrapViewModel = ViewModelProvider(this).get(ScrapViewModel::class.java)

        backButton = findViewById(R.id.back_button_from_saved_clothes)
        backButton.setOnClickListener { onBackPressed() }

        topsButton.setOnClickListener { onButtonClicked(topsButton, "tops", topsGridView) }
        pantsButton.setOnClickListener { onButtonClicked(pantsButton, "pants", pantsGridView) }
        shoesButton.setOnClickListener { onButtonClicked(shoesButton, "shoes", shoesGridView) }
        accessoriesButton.setOnClickListener { onButtonClicked(accessoriesButton, "accessories", accessoriesGridView) }

        // Set initial state
        setButtonState(topsButton)
        showGridView(topsGridView)
        loadSavedClothes("tops", topsGridView)
    }

    private fun onButtonClicked(button: Button, category: String, gridView: GridView) {
        setButtonState(button)
        showGridView(gridView)
        loadSavedClothes(category, gridView)
    }

    private fun setButtonState(activeButton: Button) {
        val buttons = listOf(topsButton, pantsButton, shoesButton, accessoriesButton)

        buttons.forEach { button ->
            if (button == activeButton) {
                button.setBackgroundResource(R.drawable.border_red_white_button)
                button.setTextColor(resources.getColor(R.color.red)) // Use your actual color resource
            } else {
                button.setBackgroundResource(R.drawable.border_gray_white_button)
                button.setTextColor(resources.getColor(R.color.gray)) // Use your actual color resource
            }
        }
    }

    private fun showGridView(visibleGridView: GridView) {
        val gridViews = listOf(topsGridView, pantsGridView, shoesGridView, accessoriesGridView)

        gridViews.forEach { gridView ->
            gridView.visibility = if (gridView == visibleGridView) View.VISIBLE else View.GONE
        }
    }

    private fun loadSavedClothes(category: String, gridView: GridView) {
        val savedClothesAdapter = SavedClothesAdapter(this, emptyList())
        gridView.adapter = savedClothesAdapter

        scrapViewModel.getSavedClothes(category).observe(this, Observer { clothesList ->
            savedClothesAdapter.updateClothesList(clothesList)
        })
    }
}
