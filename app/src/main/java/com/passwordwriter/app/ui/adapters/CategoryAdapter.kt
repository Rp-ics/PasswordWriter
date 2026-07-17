package com.passwordwriter.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.passwordwriter.app.R

class CategoryAdapter(
    private val categories: List<String>,
    private val onRename: (String) -> Unit = {},
    private val onDelete: (String) -> Unit = {}
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.categoryName)
        val renameButton: View = view.findViewById(R.id.renameButton)
        val deleteButton: View = android.view.View(view.context, null).also {
            it.layoutParams = ViewGroup.LayoutParams(0, 0)
        }

        init {
            renameButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onRename(categories[pos])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameText.text = categories[position]
    }

    override fun getItemCount(): Int = categories.size
}
