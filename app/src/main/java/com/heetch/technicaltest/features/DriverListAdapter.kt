package com.heetch.technicaltest.features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heetch.technicaltest.R
import kotlinx.android.synthetic.main.item_driver.view.*

class DriverListAdapter: ListAdapter<DriverUIModel, DriverListAdapter.DriverViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return DriverViewHolder(view)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val view = holder.itemView
        val item = getItem(position)

        view.driver_avatar.setImageBitmap(item.avatar)
        view.driver_name.text = "${item.name} ${item.surname}"
        view.driver_distance.text = item.distance
    }

    class DriverViewHolder(view: View): RecyclerView.ViewHolder(view)

    private class DiffCallback : DiffUtil.ItemCallback<DriverUIModel>() {

        override fun areItemsTheSame(oldItem: DriverUIModel, newItem: DriverUIModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DriverUIModel, newItem: DriverUIModel): Boolean {
            return oldItem == newItem
        }
    }
}