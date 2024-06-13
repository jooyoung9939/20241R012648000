package closet

import api.TokenManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.lookatme.R

class ClothesDetailFragment : Fragment() {

    private lateinit var viewModel: ClosetViewModel
    private lateinit var clothesCategoryText: TextView
    private lateinit var clothesDetailImage: ImageView
    private lateinit var clothesTypeText: TextView
    private lateinit var clothesMemoText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var scrapButton: ImageButton
    private lateinit var clothesDetailNickname: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clothes_detail, container, false)

        clothesCategoryText = view.findViewById(R.id.clothes_category_text)
        clothesDetailImage = view.findViewById(R.id.clothes_detail_image)
        clothesTypeText = view.findViewById(R.id.clothes_type_text)
        clothesMemoText = view.findViewById(R.id.clothes_memo_text)
        backButton = view.findViewById(R.id.back_button_from_clothes_detail)
        scrapButton = view.findViewById(R.id.scrap_clothes_button)
        clothesDetailNickname = view.findViewById(R.id.clothes_detail_nickname)

        viewModel = ViewModelProvider(this).get(ClosetViewModel::class.java)

        val category = arguments?.getString("category") ?: ""
        val id = arguments?.getInt("id") ?: 0
        val url = arguments?.getString("url") ?: ""

        loadClothesDetail(category, id, url)
        displayNickname()

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        checkPreviousFragment()

        return view
    }

    private fun displayNickname() {
        val nickname = TokenManager.getNickname(requireContext())
        clothesDetailNickname.text = nickname ?: "Guest"
    }

    private fun checkPreviousFragment() {
        val fragmentManager = parentFragmentManager
        val backStackCount = fragmentManager.backStackEntryCount

        if (backStackCount > 0) {
            val backStackEntry = fragmentManager.getBackStackEntryAt(backStackCount - 1)
            if (backStackEntry.name == "ClosetFragment") {
                scrapButton.visibility = View.INVISIBLE
            } else {
                scrapButton.visibility = View.VISIBLE
            }
        } else {
            scrapButton.visibility = View.VISIBLE
        }
    }

    private fun loadClothesDetail(category: String, id: Int, url: String) {
        viewModel.getClothesDetail(category, id).observe(viewLifecycleOwner, { detail ->
            detail?.let {
                clothesCategoryText.text = when (detail.category) {
                    "tops" -> "상의"
                    "pants" -> "하의"
                    "shoes" -> "신발"
                    "accessories" -> "악세사리"
                    else -> ""
                }

                // Use the passed URL to load the image
                val imageUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                    url
                } else {
                    "https://$url"
                }

                imageUrl.let {
                    Glide.with(requireContext())
                        .load(it)
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(clothesDetailImage)
                }

                clothesTypeText.text = detail.type.replace("\"", "")
                clothesMemoText.text = detail.memo.replace("\"", "").replace("\\n", "\n")
            }
        })
    }
}
