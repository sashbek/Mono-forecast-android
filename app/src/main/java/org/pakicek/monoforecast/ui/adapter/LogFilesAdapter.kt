package org.pakicek.monoforecast.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ItemLogFileBinding
import org.pakicek.monoforecast.domain.model.dto.logs.FileEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogFilesAdapter(private val onItemClicked: (FileEntity) -> Unit) :
    ListAdapter<FileEntity, LogFilesAdapter.FileViewHolder>(DiffCallback) {

    class FileViewHolder(private val binding: ItemLogFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: FileEntity, onItemClicked: (FileEntity) -> Unit) {
            val context = binding.root.context
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val startText = sdf.format(Date(file.start))

            val endText = if (file.end != null) {
                val dateStr = sdf.format(Date(file.end!!))
                context.getString(R.string.log_file_finished_fmt, dateStr)
            } else {
                context.getString(R.string.log_file_active)
            }

            binding.tvFileName.text = context.getString(R.string.log_file_session_fmt, startText)
            binding.tvFileDuration.text = endText

            binding.root.setOnClickListener { onItemClicked(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemLogFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FileEntity>() {
        override fun areItemsTheSame(oldItem: FileEntity, newItem: FileEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FileEntity, newItem: FileEntity) = oldItem == newItem
    }
}