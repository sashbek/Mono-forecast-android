package org.pakicek.monoforecast.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ItemLogDetailBinding
import org.pakicek.monoforecast.domain.model.dto.enums.LogType
import org.pakicek.monoforecast.domain.model.dto.logs.LogWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogDetailsAdapter : ListAdapter<LogWithDetails, LogDetailsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemLogDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: LogWithDetails) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val time = sdf.format(Date(item.log.timestamp))

            binding.tvTypeTime.text = "${item.log.type.name} • $time"

            when (item.log.type) {
                LogType.WEATHER -> {
                    binding.ivIcon.setImageResource(R.drawable.ic_forecast_button)
                    val w = item.weather
                    if (w != null) {
                        binding.tvContent.text = "Temp: ${w.tempC}°C, Wind: ${w.windSpeedMs} m/s"
                    } else {
                        binding.tvContent.text = item.log.message
                    }
                }
                LogType.SETTINGS -> {
                    binding.ivIcon.setImageResource(R.drawable.ic_settings)
                    val s = item.settings
                    if (s != null) {
                        binding.tvContent.text = "${s.setting} changed to ${s.value}"
                    } else {
                        binding.tvContent.text = item.log.message
                    }
                }
                LogType.LOCATION -> {
                    binding.ivIcon.setImageResource(R.drawable.ic_location_button)
                    binding.tvContent.text = item.log.message
                }
                else -> {
                    binding.ivIcon.setImageResource(R.drawable.ic_logs_button)
                    binding.tvContent.text = item.log.message
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLogDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LogWithDetails>() {
        override fun areItemsTheSame(oldItem: LogWithDetails, newItem: LogWithDetails) = oldItem.log.id == newItem.log.id
        override fun areContentsTheSame(oldItem: LogWithDetails, newItem: LogWithDetails) = oldItem == newItem
    }
}