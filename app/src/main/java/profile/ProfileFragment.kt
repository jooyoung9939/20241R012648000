package profile

import api.TokenManager
import signin.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var profileAdapter: LookBookAdapter
    private lateinit var mannequinAdapter: MannequinAdapter

    private lateinit var nicknameTextView: TextView
    private lateinit var lookbookGridView: RecyclerView
    private lateinit var mannequinGridView: RecyclerView
    private lateinit var lookbookUnderbar: View
    private lateinit var mannequinUnderbar: View

    private var cursor: Int? = null
    private var mannequinCursor: Int? = null
    private var hasNext: Boolean = true
    private var hasNextMannequin: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nicknameTextView = view.findViewById(R.id.profile_fragment_nickname)
        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        val editMannequinButton: ImageButton = view.findViewById(R.id.to_edit_mannequin_button)

        logoutButton.setOnClickListener { logout() }
        editMannequinButton.setOnClickListener {
            val intent = Intent(requireContext(), EditMannequinActivity::class.java)
            startActivity(intent)
        }

        displayNickname()

        lookbookGridView = view.findViewById(R.id.lookbook_gridview)
        mannequinGridView = view.findViewById(R.id.mannequin_gridview)
        lookbookUnderbar = view.findViewById(R.id.my_lookbook_underbar)
        mannequinUnderbar = view.findViewById(R.id.my_mannequin_underbar)

        val lookbookButton: RelativeLayout = view.findViewById(R.id.my_lookbook_grid_button)
        val mannequinButton: RelativeLayout = view.findViewById(R.id.my_mannequin_grid_button)

        lookbookButton.setOnClickListener { showLookbook() }
        mannequinButton.setOnClickListener { showMannequin() }

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
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

        loadLookBookData()
        loadMannequinData()

        return view
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, isLookbook: Boolean) {
        val adapter = if (isLookbook) {
            profileAdapter = LookBookAdapter(requireContext(), mutableListOf())
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

        val userUUID = TokenManager.getUuid(requireContext()) ?: return

        viewModel.getProfileLookBook(userUUID, 6, cursor ?: 0, "")
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

    private fun displayNickname() {
        val nickname = TokenManager.getNickname(requireContext())
        nicknameTextView.text = nickname ?: "Guest"
    }

    private fun logout() {
        val context = requireContext()
        TokenManager.clearTokens(context)
        TokenManager.setLoggedIn(context, false)
        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }
}
