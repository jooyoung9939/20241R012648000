package scrap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lookatme.R

class SavedLookBookAdapter : ListAdapter<LookBook, SavedLookBookAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_lookbook, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lookBook = getItem(position)
        holder.bind(lookBook)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val topsImageView1: ImageView = itemView.findViewById(R.id.lookbook_tops_1_image_view)
        private val topsImageView2: ImageView = itemView.findViewById(R.id.lookbook_tops_2_image_view)
        private val pantsImageView: ImageView = itemView.findViewById(R.id.lookbook_pants_image_view)
        private val shoesImageView: ImageView = itemView.findViewById(R.id.lookbook_shoes_image_view)
        private val accessoriesImageView1: ImageView = itemView.findViewById(R.id.lookbook_accessories_1_image_view)
        private val accessoriesImageView2: ImageView = itemView.findViewById(R.id.lookbook_accessories_2_image_view)
        private val accessoriesImageView3: ImageView = itemView.findViewById(R.id.lookbook_accessories_3_image_view)

        fun bind(lookBook: LookBook) {
            if (lookBook.tops.urls.isNotEmpty()) {
                Glide.with(itemView.context).load("https://${lookBook.tops.urls[0]}").into(topsImageView1)
                topsImageView1.visibility = View.VISIBLE
            }
            if (lookBook.tops.urls.size > 1) {
                Glide.with(itemView.context).load("https://${lookBook.tops.urls[1]}").into(topsImageView2)
                topsImageView2.visibility = View.VISIBLE
            }

            Glide.with(itemView.context).load("https://${lookBook.pant.url}").into(pantsImageView)
            pantsImageView.visibility = View.VISIBLE

            Glide.with(itemView.context).load("https://${lookBook.shoe.url}").into(shoesImageView)
            shoesImageView.visibility = View.VISIBLE

            if (lookBook.accessories.urls.isNotEmpty()) {
                Glide.with(itemView.context).load("https://${lookBook.accessories.urls[0]}").into(accessoriesImageView1)
                accessoriesImageView1.visibility = View.VISIBLE
            }
            if (lookBook.accessories.urls.size > 1) {
                Glide.with(itemView.context).load("https://${lookBook.accessories.urls[1]}").into(accessoriesImageView2)
                accessoriesImageView2.visibility = View.VISIBLE
            }
            if (lookBook.accessories.urls.size > 2) {
                Glide.with(itemView.context).load("https://${lookBook.accessories.urls[2]}").into(accessoriesImageView3)
                accessoriesImageView3.visibility = View.VISIBLE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LookBook>() {
        override fun areItemsTheSame(oldItem: LookBook, newItem: LookBook): Boolean {
            return oldItem.lookbookId == newItem.lookbookId
        }

        override fun areContentsTheSame(oldItem: LookBook, newItem: LookBook): Boolean {
            return oldItem == newItem
        }
    }
}
