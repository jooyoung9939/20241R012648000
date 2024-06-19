package scrap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.lookatme.R

class SavedClothesAdapter(private val context: Context, private var clothesList: List<SavedClothesResponse>) : BaseAdapter() {

    override fun getCount(): Int {
        return clothesList.size
    }

    override fun getItem(position: Int): Any {
        return clothesList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_saved_clothes, parent, false)
        val imageView: ImageView = view.findViewById(R.id.add_lookbook_clothes_image)

        val clothes = clothesList[position]
        Glide.with(context).load("https://${clothes.url}").into(imageView)

        return view
    }

    fun updateClothesList(newClothesList: List<SavedClothesResponse>) {
        clothesList = newClothesList
        notifyDataSetChanged()
    }
}
