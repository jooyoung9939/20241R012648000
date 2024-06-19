package scrap

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lookatme.R

class SavedLookBookActivity : AppCompatActivity() {

    private lateinit var scrapViewModel: ScrapViewModel
    private lateinit var savedLookBookAdapter: SavedLookBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_lookbook)

        scrapViewModel = ViewModelProvider(this).get(ScrapViewModel::class.java)

        val recyclerView: RecyclerView = findViewById(R.id.saved_lookbook_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // Set GridLayoutManager with 3 columns
        savedLookBookAdapter = SavedLookBookAdapter()
        recyclerView.adapter = savedLookBookAdapter

        val backButton: ImageButton = findViewById(R.id.back_button_from_saved_lookbook)
        backButton.setOnClickListener { onBackPressed() }

        // Observe the lookBooksLiveData to update UI when data changes
        scrapViewModel.lookBooksLiveData.observe(this, Observer { lookBooks ->
            savedLookBookAdapter.submitList(lookBooks)
        })

        // Fetch saved lookbooks
        scrapViewModel.getSavedLookBook()
    }
}
