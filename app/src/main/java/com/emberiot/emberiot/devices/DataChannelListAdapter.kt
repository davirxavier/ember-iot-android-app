package com.emberiot.emberiot.devices

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.EmberIotApp
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.data.DevicePropertyDefinition
import com.emberiot.emberiot.login.LoginFragment.Companion.Steps

class DataChannelListAdapter(
    private val removeItemCallback: (index: Int) -> Unit,
    private val onItemClickCallback: (item: DevicePropertyDefinition, index: Int) -> Unit
) : RecyclerView.Adapter<DataChannelListAdapter.Companion.ViewHolder>() {

    val list: MutableList<DevicePropertyDefinition> = mutableListOf()

    companion object {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.channelName)
            val type: TextView = itemView.findViewById(R.id.channelType)
            val deleteButton: ImageButton = itemView.findViewById(R.id.deleteChannelBtn)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_channel_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = "${DevicePropertyDefinition.getPropId(position)} - ${item.name}"
        holder.type.setText(item.type.labelId)

        holder.itemView.setOnClickListener {
            onItemClickCallback(item, position)
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context).apply {
                setTitle(R.string.delete_channel)
                setMessage(R.string.delete_channel_m)
                setPositiveButton(R.string.confirm_deletion) { dialog, _ ->
                    holder.itemView.post {
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, list.size - position)
                        removeItemCallback(position)
                        dialog.dismiss()
                    }
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }.show()
        }
    }
}