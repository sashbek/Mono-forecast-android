package org.pakicek.monoforecast.presentation.logs.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.data.local.entity.LogWithDetails
import org.pakicek.monoforecast.databinding.ItemLogDetailBinding
import org.pakicek.monoforecast.domain.model.settings.LogType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogDetailsAdapter : ListAdapter<LogWithDetails, LogDetailsAdapter.VH>(Diff()) {
    class VH(val binding: ItemLogDetailBinding) : RecyclerView.ViewHolder(binding.root)
    class Diff : DiffUtil.ItemCallback<LogWithDetails>() {
        override fun areItemsTheSame(o: LogWithDetails, n: LogWithDetails) = o.log.id == n.log.id
        override fun areContentsTheSame(o: LogWithDetails, n: LogWithDetails) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemLogDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.log.timestamp))
        holder.binding.tvTypeTime.text = "${item.log.type} • $time"

        holder.binding.tvContent.text = when (item.log.type) {
            LogType.WEATHER -> item.weather?.let { "Temp: ${it.tempC}°C, Wind: ${it.windSpeedMs}" } ?: item.log.message
            LogType.LOCATION -> item.location?.let { "Lat: ${it.latitude}, Lon: ${it.longitude}" } ?: item.log.message
            LogType.SETTINGS -> item.settings?.let { "${it.setting} -> ${it.value}" } ?: item.log.message
            else -> item.log.message
        }

        holder.binding.ivIcon.setImageResource(when (item.log.type) {
            LogType.WEATHER -> R.drawable.ic_forecast_button
            LogType.LOCATION -> R.drawable.ic_location_button
            LogType.SETTINGS -> R.drawable.ic_settings
            else -> R.drawable.ic_logs_button
        })
    }
}