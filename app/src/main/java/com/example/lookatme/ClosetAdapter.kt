package com.example.lookatme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ClosetAdapter(private val context: Context, private val items: List<ClothesItem>) : BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.clothes_item, parent, false)
        } else {
            view = convertView
        }

        val imageView: ImageView = view.findViewById(R.id.clothes_image)
        val item = items[position]

        // Ensure the URL starts with "https://"
        val imageUrl = if (item.url.startsWith("http://") || item.url.startsWith("https://")) {
            item.url
        } else {
            "https://${item.url}"
        }

        Glide.with(context)
            .load(imageUrl)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(imageView)

        return view
    }
}
