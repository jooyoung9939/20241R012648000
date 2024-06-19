package scrap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.lookatme.R

class ScrapFragment : Fragment() {

    private lateinit var scrapViewModel: ScrapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_scrap, container, false)

        scrapViewModel = ViewModelProvider(this).get(ScrapViewModel::class.java)

        // Observe the lookBooksLiveData to update UI when data changes
        scrapViewModel.lookBooksLiveData.observe(viewLifecycleOwner, Observer { lookBooks ->
            // Populate the lookbook views with the data
            lookBooks.take(6).forEachIndexed { index, lookBook ->
                when (index) {
                    0 -> populateLookBook(root, lookBook, R.id.saved_lookbook_1)
                    1 -> populateLookBook(root, lookBook, R.id.saved_lookbook_2)
                    2 -> populateLookBook(root, lookBook, R.id.saved_lookbook_3)
                    3 -> populateLookBook(root, lookBook, R.id.saved_lookbook_4)
                    4 -> populateLookBook(root, lookBook, R.id.saved_lookbook_5)
                    5 -> populateLookBook(root, lookBook, R.id.saved_lookbook_6)
                }
            }
        })

        // Observe clothes data and update UI for each category
        observeCategory(root, "tops", scrapViewModel.topsLiveData, R.id.saved_tops_1, R.id.saved_tops_2)
        observeCategory(root, "pants", scrapViewModel.pantsLiveData, R.id.saved_pants_1, R.id.saved_pants_2)
        observeCategory(root, "shoes", scrapViewModel.shoesLiveData, R.id.saved_shoes_1, R.id.saved_shoes_2)
        observeCategory(root, "accessories", scrapViewModel.accessoriesLiveData, R.id.saved_accessories_1, R.id.saved_accessories_2)

        // Fetch saved lookbooks and clothes
        scrapViewModel.getSavedLookBook()
        scrapViewModel.getSavedClothes("tops")
        scrapViewModel.getSavedClothes("pants")
        scrapViewModel.getSavedClothes("shoes")
        scrapViewModel.getSavedClothes("accessories")

        // Set up the button click listener to navigate to SavedLookBookActivity
        val seeSavedLookbookButton: TextView = root.findViewById(R.id.to_see_saved_lookbook_button)
        seeSavedLookbookButton.setOnClickListener {
            val intent = Intent(activity, SavedLookBookActivity::class.java)
            startActivity(intent)
        }

        val seeSavedClothesButton: TextView = root.findViewById(R.id.to_see_saved_clothes_button)
        seeSavedClothesButton.setOnClickListener {
            val intent = Intent(activity, SavedClothesActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun populateLookBook(root: View, lookBook: LookBook, viewId: Int) {
        val lookBookView = root.findViewById<View>(viewId)

        val topsImageView1 = lookBookView.findViewById<ImageView>(R.id.lookbook_tops_1_image_view)
        val topsImageView2 = lookBookView.findViewById<ImageView>(R.id.lookbook_tops_2_image_view)
        val pantsImageView = lookBookView.findViewById<ImageView>(R.id.lookbook_pants_image_view)
        val shoesImageView = lookBookView.findViewById<ImageView>(R.id.lookbook_shoes_image_view)
        val accessoriesImageView1 = lookBookView.findViewById<ImageView>(R.id.lookbook_accessories_1_image_view)
        val accessoriesImageView2 = lookBookView.findViewById<ImageView>(R.id.lookbook_accessories_2_image_view)
        val accessoriesImageView3 = lookBookView.findViewById<ImageView>(R.id.lookbook_accessories_3_image_view)

        if (lookBook.tops.urls.isNotEmpty()) {
            Glide.with(this).load("https://${lookBook.tops.urls[0]}").into(topsImageView1)
            topsImageView1.visibility = View.VISIBLE
        }
        if (lookBook.tops.urls.size > 1) {
            Glide.with(this).load("https://${lookBook.tops.urls[1]}").into(topsImageView2)
            topsImageView2.visibility = View.VISIBLE
        }

        Glide.with(this).load("https://${lookBook.pant.url}").into(pantsImageView)
        pantsImageView.visibility = View.VISIBLE

        Glide.with(this).load("https://${lookBook.shoe.url}").into(shoesImageView)
        shoesImageView.visibility = View.VISIBLE

        if (lookBook.accessories.urls.isNotEmpty()) {
            Glide.with(this).load("https://${lookBook.accessories.urls[0]}").into(accessoriesImageView1)
            accessoriesImageView1.visibility = View.VISIBLE
        }
        if (lookBook.accessories.urls.size > 1) {
            Glide.with(this).load("https://${lookBook.accessories.urls[1]}").into(accessoriesImageView2)
            accessoriesImageView2.visibility = View.VISIBLE
        }
        if (lookBook.accessories.urls.size > 2) {
            Glide.with(this).load("https://${lookBook.accessories.urls[2]}").into(accessoriesImageView3)
            accessoriesImageView3.visibility = View.VISIBLE
        }
    }

    private fun observeCategory(root: View, category: String, liveData: LiveData<List<SavedClothesResponse>>, firstFrameId: Int, secondFrameId: Int) {
        liveData.observe(viewLifecycleOwner, Observer { clothesList ->
            if (clothesList.isNotEmpty()) {
                updateClothesViews(root, clothesList, firstFrameId, secondFrameId)
            }
        })
    }

    private fun updateClothesViews(root: View, clothesList: List<SavedClothesResponse>, firstFrameId: Int, secondFrameId: Int) {
        if (clothesList.isNotEmpty()) {
            val firstImageView = root.findViewById<View>(firstFrameId).findViewById<ImageView>(R.id.add_lookbook_clothes_image)
            Glide.with(this).load("https://${clothesList[0].url}").into(firstImageView)
            firstImageView.visibility = View.VISIBLE
        }
        if (clothesList.size > 1) {
            val secondImageView = root.findViewById<View>(secondFrameId).findViewById<ImageView>(R.id.add_lookbook_clothes_image)
            Glide.with(this).load("https://${clothesList[1].url}").into(secondImageView)
            secondImageView.visibility = View.VISIBLE
        }
    }
}
