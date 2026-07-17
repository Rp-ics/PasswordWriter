package com.passwordwriter.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.passwordwriter.app.R

class PasswordItemAdapter(
    private val items: List<PopupItem>,
    private val onItemClick: (PopupItem) -> Unit
) : RecyclerView.Adapter<PasswordItemAdapter.ViewHolder>() {

    data class PopupItem(
        val title: String,
        val subtitle: String = "",
        val password: String? = null,
        val passwordId: Long = 0,
        val isCategory: Boolean = false,
        val isPassword: Boolean = false,
        val isBack: Boolean = false,
        val isPlaceholder: Boolean = false
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameView.text = item.title
        holder.usernameView.text = item.subtitle

        if (item.isBack) {
            holder.nameView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_arrow, 0, 0, 0
            )
        } else {
            holder.nameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.nameText)
        val usernameView: TextView = view.findViewById(R.id.usernameText)
    }
}
