package org.pakicek.monoforecast.presentation.ble

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.domain.model.dto.ble.WheelDevice
import org.pakicek.monoforecast.databinding.ItemDeviceBinding

class DevicesAdapter(
    private val onDeviceClick: (WheelDevice) -> Unit
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    private var devices = listOf<WheelDevice>()

    fun submitList(newDevices: List<WheelDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceClick: (WheelDevice) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: WheelDevice) {
            binding.tvDeviceName.text = device.name.ifEmpty { "Unknown Device" }
            binding.tvDeviceAddress.text = device.address
            binding.tvDeviceRssi.text = "${device.rssi} dBm"

            itemView.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
}