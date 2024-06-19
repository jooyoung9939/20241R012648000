package profile

import api.TokenManager
import lookbook.LookBookDetailFragment
import lookbook.LookBookViewModel
import signin.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lookatme.R
import mannequin.EditMannequinActivity

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var lookBookViewModel: LookBookViewModel
    private lateinit var profileAdapter: LookBookAdapter
    private lateinit var mannequinAdapter: MannequinAdapter

    private lateinit var nicknameTextView: TextView
    private lateinit var lookbookCountTextView: TextView
    private lateinit var followerCountTextView: TextView
    private lateinit var followingCountTextView: TextView
    private lateinit var lookbookGridView: RecyclerView
    private lateinit var mannequinGridView: RecyclerView
    private lateinit var lookbookUnderbar: View
    private lateinit var mannequinUnderbar: View

    private var cursor: Int? = null
    private var mannequinCursor: Int? = null
    private var hasNext: Boolean = true
    private var hasNextMannequin: Boolean = true

    private var isCurrentUser: Boolean = true
    private var userUUID: String? = null

    companion object {
        private const val ARG_USER_UUID = "user_uuid"
        private const val ARG_IS_CURRENT_USER = "is_current_user"

        fun newInstance(userUUID: String, isCurrentUser: Boolean): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_USER_UUID, userUUID)
            args.putBoolean(ARG_IS_CURRENT_USER, isCurrentUser)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isCurrentUser = it.getBoolean(ARG_IS_CURRENT_USER, true)
            userUUID = it.getString(ARG_USER_UUID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nicknameTextView = view.findViewById(R.id.profile_fragment_nickname)
        lookbookCountTextView = view.findViewById(R.id.lookbook_count_text)
        followerCountTextView = view.findViewById(R.id.follower_count_text)
        followingCountTextView = view.findViewById(R.id.following_count_text)
        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        val editMannequinButton: ImageButton = view.findViewById(R.id.to_edit_mannequin_button)
        val followButton: ImageButton = view.findViewById(R.id.follow_button)
        val unfollowButton: ImageButton = view.findViewById(R.id.unfollow_button)
        val toChattingListButton: ImageButton = view.findViewById(R.id.to_chatting_list_button)
        val toChattingUserButton: ImageButton = view.findViewById(R.id.to_chatting_user_button)
        val changeLookbookMannequinLayout: FrameLayout = view.findViewById(R.id.change_lookbook_mannequin_layout)

        logoutButton.setOnClickListener { logout() }
        editMannequinButton.setOnClickListener {
            val intent = Intent(requireContext(), EditMannequinActivity::class.java)
            startActivity(intent)
        }

        followButton.setOnClickListener {
            userUUID?.let { uuid ->
                viewModel.followUser(uuid) { isSuccess ->
                    if (isSuccess) {
                        followButton.visibility = View.GONE
                        unfollowButton.visibility = View.VISIBLE
                        updateFollowerCount(true)
                    }
                }
            }
        }

        unfollowButton.setOnClickListener {
            userUUID?.let { uuid ->
                viewModel.followUser(uuid) { isSuccess ->
                    if (isSuccess) {
                        followButton.visibility = View.VISIBLE
                        unfollowButton.visibility = View.GONE
                        updateFollowerCount(false)
                    }
                }
            }
        }



        // Adjust UI based on whether the user is the current user or not
        if (isCurrentUser) {
            logoutButton.visibility = View.VISIBLE
            editMannequinButton.visibility = View.VISIBLE
            toChattingListButton.visibility = View.VISIBLE
            followButton.visibility = View.GONE
            unfollowButton.visibility = View.GONE
            toChattingUserButton.visibility = View.GONE
            changeLookbookMannequinLayout.visibility = View.VISIBLE
        } else {
            logoutButton.visibility = View.GONE
            editMannequinButton.visibility = View.GONE
            toChattingListButton.visibility = View.GONE
            followButton.visibility = View.VISIBLE
            unfollowButton.visibility = View.VISIBLE
            toChattingUserButton.visibility = View.VISIBLE
            changeLookbookMannequinLayout.visibility = View.GONE
        }

        lookbookGridView = view.findViewById(R.id.lookbook_gridview)
        mannequinGridView = view.findViewById(R.id.mannequin_gridview)
        lookbookUnderbar = view.findViewById(R.id.my_lookbook_underbar)
        mannequinUnderbar = view.findViewById(R.id.my_mannequin_underbar)

        val lookbookButton: RelativeLayout = view.findViewById(R.id.my_lookbook_grid_button)
        val mannequinButton: RelativeLayout = view.findViewById(R.id.my_mannequin_grid_button)

        lookbookButton.setOnClickListener { showLookbook() }
        mannequinButton.setOnClickListener { showMannequin() }

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        lookBookViewModel = ViewModelProvider(this).get(LookBookViewModel::class.java)
        setupRecyclerView(lookbookGridView, true)
        setupRecyclerView(mannequinGridView, false)

        viewModel.lookBookData.observe(viewLifecycleOwner, Observer { response ->
            response?.lookBookCollection?.let { lookBookCollection ->
                profileAdapter.addItems(lookBookCollection)
                cursor = response.cursorPaginationMetaData.cursor
                hasNext = response.cursorPaginationMetaData.hasNext
            }
        })

        viewModel.mannequinData.observe(viewLifecycleOwner, Observer { response ->
            response?.mannequinLookBookCollection?.let { mannequinLookBookCollection ->
                mannequinAdapter.addItems(mannequinLookBookCollection)
                mannequinCursor = response.cursorPaginationMetaData.cursor
                hasNextMannequin = response.cursorPaginationMetaData.hasNext
            }
        })

        viewModel.myProfileData.observe(viewLifecycleOwner, Observer { profile ->
            profile?.let {
                nicknameTextView.text = it.nickname
                lookbookCountTextView.text = it.lookBookCnt.toString()
                followerCountTextView.text = it.followerCnt.toString()
                followingCountTextView.text = it.followingCnt.toString()
            }
        })

        viewModel.otherProfileData.observe(viewLifecycleOwner, Observer { profile ->
            profile?.let {
                nicknameTextView.text = it.nickname
                lookbookCountTextView.text = it.lookBookCnt.toString()
                followerCountTextView.text = it.followerCnt.toString()
                followingCountTextView.text = it.followingCnt.toString()
                followButton.visibility = if (it.followOrNot) View.GONE else View.VISIBLE
                unfollowButton.visibility = if (it.followOrNot) View.VISIBLE else View.GONE
            }
        })

        // Load data based on whether it's the current user's profile or not
        if (isCurrentUser) {
            viewModel.getMyProfile()
            loadLookBookData()
            loadMannequinData()
        } else {
            userUUID?.let { uuid ->
                viewModel.getOtherProfile(uuid)
                viewModel.getProfileLookBook(uuid, 6, cursor ?: 0, "")
            }
        }

        return view
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, isLookbook: Boolean) {
        val adapter = if (isLookbook) {
            profileAdapter = LookBookAdapter(requireContext(), mutableListOf()) { lookbookId ->
                openLookBookDetail(lookbookId)
            }
            profileAdapter
        } else {
            mannequinAdapter = MannequinAdapter(requireContext(), mutableListOf())
            mannequinAdapter
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (isLookbook) {
                    if (!hasNext) return

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (totalItemCount <= (lastVisibleItemPosition + 2)) {
                        loadLookBookData()
                    }
                } else {
                    if (!hasNextMannequin) return

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (totalItemCount <= (lastVisibleItemPosition + 2)) {
                        loadMannequinData()
                    }
                }
            }
        })
    }

    private fun loadLookBookData() {
        if (!hasNext) return

        val uuidToUse = if (isCurrentUser) {
            TokenManager.getUuid(requireContext()) ?: return
        } else {
            userUUID ?: return
        }

        viewModel.getProfileLookBook(uuidToUse, 6, cursor ?: 0, "")
    }

    private fun loadMannequinData() {
        if (!hasNextMannequin) return

        viewModel.getProfileMannequin(6, mannequinCursor ?: 0)
    }

    private fun showLookbook() {
        lookbookGridView.visibility = View.VISIBLE
        mannequinGridView.visibility = View.GONE
        lookbookUnderbar.visibility = View.VISIBLE
        mannequinUnderbar.visibility = View.GONE
    }

    private fun showMannequin() {
        lookbookGridView.visibility = View.GONE
        mannequinGridView.visibility = View.VISIBLE
        lookbookUnderbar.visibility = View.GONE
        mannequinUnderbar.visibility = View.VISIBLE
    }

    private fun logout() {
        val context = requireContext()
        TokenManager.clearTokens(context)
        TokenManager.setLoggedIn(context, false)
        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }

    private fun openLookBookDetail(lookbookId: Int) {
        val uuidToUse = if (isCurrentUser) {
            TokenManager.getUuid(requireContext()) ?: return
        } else {
            userUUID ?: return
        }

        lookBookViewModel.getDetailProfileLookBook(uuidToUse, 1, lookbookId, "")
        lookBookViewModel.lookBookDetailData.observe(viewLifecycleOwner, Observer { response ->
            response?.lookBookDetail?.firstOrNull()?.let { lookBookDetail ->
                val fragment = LookBookDetailFragment.newInstance(lookBookDetail, true)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }
    private fun updateFollowerCount(isFollowing: Boolean) {
        val currentCount = followerCountTextView.text.toString().toInt()
        val newCount = if (isFollowing) currentCount + 1 else currentCount - 1
        followerCountTextView.text = newCount.toString()
    }
}
