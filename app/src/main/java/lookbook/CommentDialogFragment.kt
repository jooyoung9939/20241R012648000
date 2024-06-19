package lookbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lookatme.R
import api.TokenManager
import profile.ProfileFragment

class CommentDialogFragment : DialogFragment(), CommentAdapter.OnProfileImageClickListener {

    private var lookbookId: Int = 0
    private lateinit var comments: List<Comment>
    private lateinit var viewModel: LookBookViewModel

    companion object {
        private const val ARG_LOOKBOOK_ID = "lookbook_id"
        private const val ARG_COMMENTS = "comments"

        fun newInstance(lookbookId: Int, comments: List<Comment>): CommentDialogFragment {
            val fragment = CommentDialogFragment()
            val args = Bundle()
            args.putInt(ARG_LOOKBOOK_ID, lookbookId)
            args.putParcelableArrayList(ARG_COMMENTS, ArrayList(comments))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lookbookId = it.getInt(ARG_LOOKBOOK_ID)
            comments = it.getParcelableArrayList<Comment>(ARG_COMMENTS) ?: emptyList()
        }

        viewModel = ViewModelProvider(requireActivity()).get(LookBookViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_comment, container, false)

        // Set up RecyclerView and other UI elements as needed
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.comments_recyclerview)
        commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (comments.isNotEmpty()) {
            commentsRecyclerView.adapter = CommentAdapter(comments, this)
        } else {
            commentsRecyclerView.visibility = View.GONE
            val noCommentsView = view.findViewById<TextView>(R.id.no_comments_view)
            noCommentsView.visibility = View.VISIBLE
        }

        // Close button to close the dialog
        view.findViewById<View>(R.id.exit_comment_dialog).setOnClickListener {
            dismiss()
        }

        // Send comment button click listener
        val sendCommentButton = view.findViewById<ImageButton>(R.id.send_comment_button)
        val sendCommentText = view.findViewById<EditText>(R.id.send_comment_text)

        sendCommentButton.setOnClickListener {
            val content = sendCommentText.text.toString()
            if (content.isNotBlank()) {
                viewModel.addComment(lookbookId, content, null)
                sendCommentText.text.clear()
            }
        }

        viewModel.commentResponse.observe(viewLifecycleOwner, Observer {
            // Reload comments after a new comment is added
            viewModel.getDetailLookBook(1, lookbookId, "")
        })

        viewModel.lookBookDetailData.observe(viewLifecycleOwner, Observer { response ->
            response?.lookBookDetail?.firstOrNull()?.comments?.let { updatedComments ->
                commentsRecyclerView.adapter = CommentAdapter(updatedComments, this)
                commentsRecyclerView.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.no_comments_view).visibility = View.GONE
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()
        // Set the dialog to be full screen
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onProfileImageClick(writerUUID: String) {
        val isCurrentUser = TokenManager.getUuid(requireContext()) == writerUUID
        val fragment = ProfileFragment.newInstance(writerUUID, isCurrentUser)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
        dismiss()
    }
}
