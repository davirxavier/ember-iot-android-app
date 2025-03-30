package com.emberiot.emberiot.devices

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emberiot.emberiot.R
import com.emberiot.emberiot.data.Device
import com.emberiot.emberiot.databinding.FragmentDeviceBinding

class DeviceListAdapter(
    private val context: Context
) : ListAdapter<Device, DeviceListAdapter.ViewHolder>(DeviceDiffCallback()) {

    companion object {
        class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem.id == newItem.id // Compare unique IDs
            }

            override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem == newItem && oldItem.isOnline() == newItem.isOnline() // Use data class equality check
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.deviceName.text = item.name
        holder.status.text = if (item.isOnline()) "Online" else "Offline"

        val color = ContextCompat.getColor(context, if (item.isOnline()) R.color.green else R.color.gray)
        holder.statusIndicator.backgroundTintList = ColorStateList.valueOf(color)
    }

    inner class ViewHolder(binding: FragmentDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        val deviceName: TextView = binding.deviceName
        val status: TextView = binding.deviceStatus
        val statusIndicator: View = binding.statusIndicator

        override fun toString(): String {
            return super.toString() + " '" + deviceName.text + "'"
        }
    }

}