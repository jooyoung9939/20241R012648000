package lookbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lookatme.R

class CommentAdapter(
    private val comments: List<Comment>,
    private val onProfileImageClickListener: OnProfileImageClickListener
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    interface OnProfileImageClickListener {
        fun onProfileImageClick(writerUUID: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentsWriter.text = comment.writer
        holder.commentsContent.text = comment.content

        holder.commentsProfileImage.setOnClickListener {
            comment.writerUUID?.let { writerUUID ->
                onProfileImageClickListener.onProfileImageClick(writerUUID)
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentsWriter: TextView = itemView.findViewById(R.id.comments_writer)
        val commentsContent: TextView = itemView.findViewById(R.id.comments_content)
        val commentsProfileImage: ImageView = itemView.findViewById(R.id.comments_profile_image)
    }
}
