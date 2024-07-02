package com.example.projekt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class CategoryAdapter(private val context: Context, private val categories: List<NewCategory>) : BaseAdapter() {

    interface OnAddImageClickListener {
        fun onAddImageClicked()
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var addImageClickListener: OnAddImageClickListener? = null

    fun setOnAddImageClickListener(listener: OnAddImageClickListener) {
        addImageClickListener = listener
    }
    // Metoda obsługująca kliknięcie przycisku
    private fun handleAddImageClick() {
        addImageClickListener?.onAddImageClicked()
    }

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): Any = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(R.layout.category_item, parent, false)
        val category = getItem(position) as NewCategory

        val imageView = view.findViewById<ImageView>(R.id.icon)
        imageView.setImageResource(category.icon)


        return view
    }
}