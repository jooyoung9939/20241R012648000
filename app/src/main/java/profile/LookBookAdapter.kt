package profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lookatme.R

class LookBookAdapter(private val context: Context, private val items: MutableList<LookBookCollection>) : RecyclerView.Adapter<LookBookAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lookbook, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItems(newItems: List<LookBookCollection>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val topsImageViews = listOf(
            view.findViewById<ImageView>(R.id.lookbook_tops_1_image_view),
            view.findViewById<ImageView>(R.id.lookbook_tops_2_image_view)
        )
        private val accessoriesImageViews = listOf(
            view.findViewById<ImageView>(R.id.lookbook_accessories_1_image_view),
            view.findViewById<ImageView>(R.id.lookbook_accessories_2_image_view),
            view.findViewById<ImageView>(R.id.lookbook_accessories_3_image_view)
        )
        private val pantImageView = view.findViewById<ImageView>(R.id.lookbook_pants_image_view)
        private val shoeImageView = view.findViewById<ImageView>(R.id.lookbook_shoes_image_view)

        fun bind(item: LookBookCollection) {
            // Reset visibility
            topsImageViews.forEach { it.visibility = View.GONE }
            accessoriesImageViews.forEach { it.visibility = View.GONE }
            pantImageView.visibility = View.GONE
            shoeImageView.visibility = View.GONE

            // Bind tops
            item.tops.urls.forEachIndexed { index, url ->
                if (index < topsImageViews.size) {
                    topsImageViews[index].visibility = View.VISIBLE
                    Glide.with(context).load("https://$url").into(topsImageViews[index])
                }
            }

            // Bind accessories
            item.accessories.urls.forEachIndexed { index, url ->
                if (index < accessoriesImageViews.size) {
                    accessoriesImageViews[index].visibility = View.VISIBLE
                    Glide.with(context).load("https://$url").into(accessoriesImageViews[index])
                }
            }

            // Bind pant
            if (item.pant.url.isNotEmpty()) {
                pantImageView.visibility = View.VISIBLE
                Glide.with(context).load("https://${item.pant.url}").into(pantImageView)
            }

            // Bind shoe
            if (item.shoe.url.isNotEmpty()) {
                shoeImageView.visibility = View.VISIBLE
                Glide.with(context).load("https://${item.shoe.url}").into(shoeImageView)
            }
        }
    }
}
