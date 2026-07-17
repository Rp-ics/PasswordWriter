package com.passwordwriter.app.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.passwordwriter.app.R
import com.passwordwriter.app.data.CategoryManager

class CategoryAdapter(
    private val context: Context,
    private val categories: List<String>,
    private val onRename: (String) -> Unit = {},
    private val onEdit: (String) -> Unit = {}
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.categoryIcon)
        val nameText: TextView = view.findViewById(R.id.categoryName)
        val editButton: View = view.findViewById(R.id.editButton)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onEdit(categories[pos])
            }
            editButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onEdit(categories[pos])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.nameText.text = category

        val iconName = CategoryManager.getIcon(context, category)
        val iconResId = CategoryManager.getIconDrawableId(iconName)
        holder.iconView.setImageResource(iconResId)

        val color = CategoryManager.getColor(context, category)
        if (color != 0) {
            holder.iconView.setColorFilter(color)
        } else {
            holder.iconView.setColorFilter(
                ContextCompat.getColor(context, R.color.on_surface)
            )
        }
    }

    override fun getItemCount(): Int = categories.size
}
