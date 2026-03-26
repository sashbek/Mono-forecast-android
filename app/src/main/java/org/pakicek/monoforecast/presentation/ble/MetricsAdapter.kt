package org.pakicek.monoforecast.presentation.ble

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.databinding.ItemMetricBinding
import org.pakicek.monoforecast.domain.model.dto.VehicleMetric

class MetricsAdapter(
    private val onItemClick: (VehicleMetric) -> Unit
) : ListAdapter<VehicleMetric, MetricsAdapter.MetricViewHolder>(DiffCallback) {

    class MetricViewHolder(
        private val binding: ItemMetricBinding,
        private val onItemClick: (VehicleMetric) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VehicleMetric) {
            binding.tvName.text = item.name
            binding.tvValue.text = item.value
            binding.tvUnit.text = item.unit

            // Обработка клика по элементу
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemMetricBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MetricViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<VehicleMetric>() {
        override fun areItemsTheSame(oldItem: VehicleMetric, newItem: VehicleMetric): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: VehicleMetric, newItem: VehicleMetric): Boolean {
            return oldItem == newItem
        }
    }
}