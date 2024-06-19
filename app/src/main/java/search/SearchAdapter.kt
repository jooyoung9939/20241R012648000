package search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lookatme.R

class SearchAdapter(
    private val items: MutableList<LookBookCollection>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<SearchAdapter.LookBookViewHolder>() {

    class LookBookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topsImages: List<ImageView> = listOf(
            view.findViewById(R.id.lookbook_tops_1_image_view),
            view.findViewById(R.id.lookbook_tops_2_image_view)
        )
        val accessoriesImages: List<ImageView> = listOf(
            view.findViewById(R.id.lookbook_accessories_1_image_view),
            view.findViewById(R.id.lookbook_accessories_2_image_view),
            view.findViewById(R.id.lookbook_accessories_3_image_view)
        )
        val pantsImage: ImageView = view.findViewById(R.id.lookbook_pants_image_view)
        val shoesImage: ImageView = view.findViewById(R.id.lookbook_shoes_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LookBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lookbook, parent, false)
        return LookBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: LookBookViewHolder, position: Int) {
        val item = items[position]

        holder.topsImages.forEach { it.visibility = View.GONE }
        holder.accessoriesImages.forEach { it.visibility = View.GONE }
        holder.pantsImage.visibility = View.GONE
        holder.shoesImage.visibility = View.GONE

        item.tops.urls.forEachIndexed { index, url ->
            if (index < holder.topsImages.size) {
                Glide.with(holder.itemView.context)
                    .load("https://$url")
                    .into(holder.topsImages[index])
                holder.topsImages[index].visibility = View.VISIBLE
            }
        }

        item.accessories.urls.forEachIndexed { index, url ->
            if (index < holder.accessoriesImages.size) {
                Glide.with(holder.itemView.context)
                    .load("https://$url")
                    .into(holder.accessoriesImages[index])
                holder.accessoriesImages[index].visibility = View.VISIBLE
            }
        }

        Glide.with(holder.itemView.context)
            .load("https://${item.pant.url}")
            .into(holder.pantsImage)
        holder.pantsImage.visibility = View.VISIBLE

        Glide.with(holder.itemView.context)
            .load("https://${item.shoe.url}")
            .into(holder.shoesImage)
        holder.shoesImage.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClicked(item.lookbookId)
        }
    }

    override fun getItemCount() = items.size

    fun addItems(newItems: List<LookBookCollection>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setItems(newItems: List<LookBookCollection>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }
}
