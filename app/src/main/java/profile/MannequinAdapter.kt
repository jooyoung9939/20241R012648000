package profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lookatme.R

class MannequinAdapter(private val context: Context, private val items: MutableList<MannequinLookBookCollection>) : RecyclerView.Adapter<MannequinAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mannequin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItems(newItems: List<MannequinLookBookCollection>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mannequinImageView: ImageView = view.findViewById(R.id.mannequin_image)

        fun bind(item: MannequinLookBookCollection) {
            Glide.with(context).load("https://${item.url}").into(mannequinImageView)
        }
    }
}
