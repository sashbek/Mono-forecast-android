package org.pakicek.monoforecast.presentation.logs.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.data.local.entity.FileEntity
import org.pakicek.monoforecast.databinding.ItemLogFileBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogFilesAdapter(private val onClick: (FileEntity) -> Unit) : ListAdapter<FileEntity, LogFilesAdapter.VH>(Diff()) {
    class VH(val binding: ItemLogFileBinding) : RecyclerView.ViewHolder(binding.root)
    class Diff : DiffUtil.ItemCallback<FileEntity>() {
        override fun areItemsTheSame(o: FileEntity, n: FileEntity) = o.id == n.id
        override fun areContentsTheSame(o: FileEntity, n: FileEntity) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemLogFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.binding.tvFileName.text = "Session: " + sdf.format(Date(item.start))
        holder.binding.tvFileDuration.text = if (item.end != null) "Finished: " + sdf.format(Date(item.end!!)) else "Active..."
        holder.itemView.setOnClickListener { onClick(item) }
    }
}