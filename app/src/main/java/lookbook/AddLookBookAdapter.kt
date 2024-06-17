package lookbook

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.lookatme.R

class AddLookBookAdapter(
    private val context: Context,
    private val items: List<LookBookClothesItem>,
    private val category: String,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AddLookBookAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: LookBookClothesItem, category: String)
    }

    private val activatedItems: MutableSet<LookBookClothesItem> = mutableSetOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.add_lookbook_clothes_image)
        val checkView: ImageView = view.findViewById(R.id.check_lookbook_clothes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lookbook_clothes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
            item.url
        } else {
            "https://${item.url}"
        }

        Glide.with(context)
            .load(imageUrl)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item, category)
        }

        if (activatedItems.contains(item)) {
            holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.border_red_white_button)
            holder.checkView.visibility = View.VISIBLE
        } else {
            holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.border_gray_white_button)
            holder.checkView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun activateItem(item: LookBookClothesItem) {
        activatedItems.add(item)
        notifyDataSetChanged()
    }

    fun deactivateItem(item: LookBookClothesItem) {
        activatedItems.remove(item)
        notifyDataSetChanged()
    }
}
