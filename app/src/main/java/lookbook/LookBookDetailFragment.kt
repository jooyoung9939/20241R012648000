package lookbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lookatme.R
import api.TokenManager
import closet.ClothesDetailFragment
import profile.ProfileFragment
import search.SearchFragment

class LookBookDetailFragment : Fragment(), CommentAdapter.OnProfileImageClickListener {

    private lateinit var seeClothesDetailButton: ImageButton
    private var isClothesDetailVisible = false

    private lateinit var unlikeButton: ImageButton
    private lateinit var likeButton: ImageButton
    private var isLiked = false

    private lateinit var unsaveButton: ImageButton
    private lateinit var saveButton: ImageButton
    private var isSaved = false

    private lateinit var backButton: ImageButton

    private lateinit var lookBookDetail: LookBookDetail
    private var lookBookId: Int? = null
    private var fromProfile: Boolean = false

    private lateinit var lookBookDetailUserButton: LinearLayout

    private lateinit var viewModel: LookBookViewModel

    companion object {
        private const val ARG_LOOKBOOK_DETAIL = "lookbook_detail"
        private const val ARG_LOOKBOOK_ID = "lookbook_id"
        private const val ARG_FROM_PROFILE = "from_profile"

        fun newInstance(lookBookDetail: LookBookDetail, fromProfile: Boolean): LookBookDetailFragment {
            val fragment = LookBookDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_LOOKBOOK_DETAIL, lookBookDetail)
            args.putBoolean(ARG_FROM_PROFILE, fromProfile)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(lookBookId: Int, fromProfile: Boolean): LookBookDetailFragment {
            val fragment = LookBookDetailFragment()
            val args = Bundle()
            args.putInt(ARG_LOOKBOOK_ID, lookBookId)
            args.putBoolean(ARG_FROM_PROFILE, fromProfile)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_LOOKBOOK_DETAIL)) {
                lookBookDetail = it.getParcelable(ARG_LOOKBOOK_DETAIL)!!
            }
            if (it.containsKey(ARG_LOOKBOOK_ID)) {
                lookBookId = it.getInt(ARG_LOOKBOOK_ID)
            }
            fromProfile = it.getBoolean(ARG_FROM_PROFILE, false)
        }

        viewModel = ViewModelProvider(this).get(LookBookViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lookbook_detail, container, false)

        seeClothesDetailButton = view.findViewById(R.id.see_clothes_detail_button)
        unlikeButton = view.findViewById(R.id.unlike_button)
        likeButton = view.findViewById(R.id.like_button)
        unsaveButton = view.findViewById(R.id.unsave_button)
        saveButton = view.findViewById(R.id.save_button)
        backButton = view.findViewById(R.id.back_button_from_lookbook_detail)

        lookBookDetailUserButton = view.findViewById(R.id.lookbook_detail_user_button)

        if (this::lookBookDetail.isInitialized) {
            bindDataToViews(view)
        } else {
            lookBookId?.let { id ->
                loadLookBookDetail(id, view)
            }
        }

        updateClothesDetailVisibility(view)
        updateLikeButtonVisibility()
        updateSaveButtonVisibility()
        updateLikeCount(view)

        seeClothesDetailButton.setOnClickListener {
            isClothesDetailVisible = !isClothesDetailVisible
            updateClothesDetailVisibility(view)
        }

        unlikeButton.setOnClickListener {
            isLiked = true
            lookBookDetail.lookbook.likeCnt += 1
            updateLikeButtonVisibility()
            updateLikeCount(view)
            viewModel.likeLookBook(lookBookDetail.lookbook.lookbookId)
        }

        likeButton.setOnClickListener {
            isLiked = false
            lookBookDetail.lookbook.likeCnt -= 1
            updateLikeButtonVisibility()
            updateLikeCount(view)
            viewModel.likeLookBook(lookBookDetail.lookbook.lookbookId)
        }

        unsaveButton.setOnClickListener {
            isSaved = true
            updateSaveButtonVisibility()
            viewModel.clipLookBook(lookBookDetail.lookbook.lookbookId, fromProfile)
        }

        saveButton.setOnClickListener {
            isSaved = false
            updateSaveButtonVisibility()
            viewModel.clipLookBook(lookBookDetail.lookbook.lookbookId, fromProfile)
        }

        backButton.setOnClickListener {
            if (fromProfile) {
                TokenManager.getUuid(requireContext())?.let { uuid ->
                    navigateToProfileFragment(uuid, true)
                }
            } else {
                navigateToSearchFragment()
            }
        }

        lookBookDetailUserButton.setOnClickListener {
            val userUUID = lookBookDetail.user.uuid
            navigateToUserProfile(userUUID)
        }

        view.findViewById<LinearLayout>(R.id.lookbook_detail_comment_button).setOnClickListener {
            val lookbookId = lookBookDetail.lookbook.lookbookId // Assuming lookBookDetail is already initialized
            val comments = lookBookDetail.comments // Assuming you have a list of comments in lookBookDetail
            val commentDialogFragment = CommentDialogFragment.newInstance(lookbookId, comments)
            commentDialogFragment.show(parentFragmentManager, "CommentDialogFragment")
        }

        return view
    }

    private fun loadLookBookDetail(cursor: Int, view: View) {
        val take = 1
        val keyword = ""

        if (fromProfile) {
            TokenManager.getUuid(requireContext())?.let { userUUID ->
                viewModel.getDetailProfileLookBook(userUUID, take, cursor, keyword)
            }
        } else {
            viewModel.getDetailLookBook(take, cursor, keyword)
        }

        viewModel.lookBookDetailData.observe(viewLifecycleOwner, Observer { response ->
            response?.lookBookDetail?.firstOrNull()?.let {
                lookBookDetail = it
                bindDataToViews(view)
            }
        })
    }

    private fun updateClothesDetailVisibility(view: View) {
        val visibility = if (isClothesDetailVisible) View.VISIBLE else View.GONE

        val imageViewIdsToDetailViewIds = mapOf(
            R.id.lookbook_tops_1_image_view to R.id.lookbook_tops_1_clothes_detail,
            R.id.lookbook_tops_2_image_view to R.id.lookbook_tops_2_clothes_detail,
            R.id.lookbook_pants_image_view to R.id.lookbook_pants_clothes_detail,
            R.id.lookbook_shoes_image_view to R.id.lookbook_shoes_clothes_detail,
            R.id.lookbook_accessories_1_image_view to R.id.lookbook_accessories_1_clothes_detail,
            R.id.lookbook_accessories_2_image_view to R.id.lookbook_accessories_2_clothes_detail,
            R.id.lookbook_accessories_3_image_view to R.id.lookbook_accessories_3_clothes_detail
        )

        imageViewIdsToDetailViewIds.forEach { (imageViewId, detailViewId) ->
            val imageView = view.findViewById<ImageView>(imageViewId)
            val detailView = view.findViewById<TextView>(detailViewId)
            if (imageView.visibility == View.VISIBLE) {
                detailView.visibility = visibility
            }
        }
    }

    private fun updateLikeButtonVisibility() {
        likeButton.visibility = if (isLiked) View.VISIBLE else View.GONE
        unlikeButton.visibility = if (isLiked) View.GONE else View.VISIBLE
    }

    private fun updateSaveButtonVisibility() {
        saveButton.visibility = if (isSaved) View.VISIBLE else View.GONE
        unsaveButton.visibility = if (isSaved) View.GONE else View.VISIBLE
    }

    private fun updateLikeCount(view: View) {
        if (this::lookBookDetail.isInitialized) {
            view.findViewById<TextView>(R.id.lookbook_detail_like_count_text).text = "${lookBookDetail.lookbook.likeCnt}"
        }
    }

    private fun bindDataToViews(view: View) {
        if (!this::lookBookDetail.isInitialized) return

        val types = lookBookDetail.lookbook.type.map { it.replace("\"", "") }
        val typeViews = listOf(
            view.findViewById<TextView>(R.id.lookbook_detail_type_1),
            view.findViewById<TextView>(R.id.lookbook_detail_type_2),
            view.findViewById<TextView>(R.id.lookbook_detail_type_3)
        )

        typeViews.forEachIndexed { index, textView ->
            if (index < types.size) {
                textView.text = types[index]
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }

        view.findViewById<TextView>(R.id.lookbook_detail_memo_text).text = lookBookDetail.lookbook.memo.replace("\"", "").replace("\\n", "\n")
        view.findViewById<TextView>(R.id.lookbook_detail_like_count_text).text = "${lookBookDetail.lookbook.likeCnt}"
        view.findViewById<TextView>(R.id.lookbook_detail_comment_count).text = "${lookBookDetail.lookbook.commentCnt}"

        val nickname = lookBookDetail.user.nickname
        view.findViewById<TextView>(R.id.lookbook_detail_nickname).text = nickname
        view.findViewById<TextView>(R.id.lookbook_detail_user_nickname).text = nickname
        view.findViewById<TextView>(R.id.lookbook_detail_nickname_memo).text = nickname

        isLiked = lookBookDetail.user.like
        isSaved = lookBookDetail.user.save
        updateLikeButtonVisibility()
        updateSaveButtonVisibility()

        val topsUrls = lookBookDetail.tops.map { it.url }
        val accessoriesUrls = lookBookDetail.accessories.map { it.url }
        val pantUrl = lookBookDetail.pant.url
        val shoeUrl = lookBookDetail.shoe.url

        val topsImageViews = listOf(
            view.findViewById<ImageView>(R.id.lookbook_tops_1_image_view),
            view.findViewById<ImageView>(R.id.lookbook_tops_2_image_view)
        )

        val accessoriesImageViews = listOf(
            view.findViewById<ImageView>(R.id.lookbook_accessories_1_image_view),
            view.findViewById<ImageView>(R.id.lookbook_accessories_2_image_view),
            view.findViewById<ImageView>(R.id.lookbook_accessories_3_image_view)
        )

        topsUrls.forEachIndexed { index, url ->
            if (index < topsImageViews.size) {
                topsImageViews[index].visibility = View.VISIBLE
                Glide.with(this).load("https://$url").into(topsImageViews[index])
            }
        }

        accessoriesUrls.forEachIndexed { index, url ->
            if (index < accessoriesImageViews.size) {
                accessoriesImageViews[index].visibility = View.VISIBLE
                Glide.with(this).load("https://$url").into(accessoriesImageViews[index])
            }
        }

        if (pantUrl.isNotEmpty()) {
            val pantImageView = view.findViewById<ImageView>(R.id.lookbook_pants_image_view)
            pantImageView.visibility = View.VISIBLE
            Glide.with(this).load("https://$pantUrl").into(pantImageView)
        }

        if (shoeUrl.isNotEmpty()) {
            val shoeImageView = view.findViewById<ImageView>(R.id.lookbook_shoes_image_view)
            shoeImageView.visibility = View.VISIBLE
            Glide.with(this).load("https://$shoeUrl").into(shoeImageView)
        }

        val sortedComments = lookBookDetail.comments.sortedBy { it.id }
        val commentLayout = view.findViewById<LinearLayout>(R.id.lookbook_detail_comment_content_layout)
        val commentCountTextView = view.findViewById<TextView>(R.id.lookbook_detail_comment_count)

        if (sortedComments.isNotEmpty()) {
            commentCountTextView.text = "${sortedComments.size}"

            // Populate the first comment
            val firstComment = sortedComments[0]
            val comment1NicknameTextView = view.findViewById<TextView>(R.id.lookbook_detail_comment_1_nickname)
            val comment1ContentTextView = view.findViewById<TextView>(R.id.lookbook_detail_comment_1_content)

            comment1NicknameTextView.text = firstComment.writer ?: "Anonymous"
            comment1ContentTextView.text = firstComment.content

            commentLayout.visibility = View.VISIBLE
        } else {
            commentLayout.visibility = View.GONE
            commentCountTextView.text = "0"
        }

        // Set up RecyclerView for comments
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.comments_recyclerview)
        commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (sortedComments.isNotEmpty()) {
            commentsRecyclerView.adapter = CommentAdapter(sortedComments, this)
        } else {
            commentsRecyclerView.visibility = View.GONE
            val noCommentsView = view.findViewById<TextView>(R.id.no_comments_view)
            noCommentsView.visibility = View.VISIBLE
        }

        // Now set the navigation after the detail is loaded
        setClothesDetailNavigation(view)
    }

    override fun onProfileImageClick(writerUUID: String) {
        val isCurrentUser = TokenManager.getUuid(requireContext()) == writerUUID
        val fragment = ProfileFragment.newInstance(writerUUID, isCurrentUser)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToUserProfile(userUUID: String) {
        val isCurrentUser = TokenManager.getUuid(requireContext()) == userUUID
        val fragment = ProfileFragment.newInstance(userUUID, isCurrentUser)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToProfileFragment(userUUID: String, isCurrentUser: Boolean) {
        val fragment = ProfileFragment.newInstance(userUUID, isCurrentUser)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToSearchFragment() {
        val fragment = SearchFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setClothesDetailNavigation(view: View) {
        if (!this::lookBookDetail.isInitialized) return

        val clothesDetails = mapOf(
            R.id.lookbook_tops_1_clothes_detail to Triple("tops", lookBookDetail.tops.getOrNull(0)?.id, lookBookDetail.tops.getOrNull(0)?.url),
            R.id.lookbook_tops_2_clothes_detail to Triple("tops", lookBookDetail.tops.getOrNull(1)?.id, lookBookDetail.tops.getOrNull(1)?.url),
            R.id.lookbook_pants_clothes_detail to Triple("pants", lookBookDetail.pant.id, lookBookDetail.pant.url),
            R.id.lookbook_shoes_clothes_detail to Triple("shoes", lookBookDetail.shoe.id, lookBookDetail.shoe.url),
            R.id.lookbook_accessories_1_clothes_detail to Triple("accessories", lookBookDetail.accessories.getOrNull(0)?.id, lookBookDetail.accessories.getOrNull(0)?.url),
            R.id.lookbook_accessories_2_clothes_detail to Triple("accessories", lookBookDetail.accessories.getOrNull(1)?.id, lookBookDetail.accessories.getOrNull(1)?.url),
            R.id.lookbook_accessories_3_clothes_detail to Triple("accessories", lookBookDetail.accessories.getOrNull(2)?.id, lookBookDetail.accessories.getOrNull(2)?.url)
        )

        clothesDetails.forEach { (textViewId, detail) ->
            val textView = view.findViewById<TextView>(textViewId)
            textView.setOnClickListener {
                detail.second?.let { id ->
                    detail.third?.let { url ->
                        navigateToClothesDetail(detail.first, id, url)
                    }
                }
            }
        }
    }

    private fun navigateToClothesDetail(category: String, id: Int, url: String) {
        val nickname = lookBookDetail.user.nickname
        val fragment = ClothesDetailFragment.newInstance(category, id, url, nickname)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
