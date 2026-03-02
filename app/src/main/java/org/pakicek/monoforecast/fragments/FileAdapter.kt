package org.pakicek.monoforecast.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.pakicek.monoforecast.databinding.FileItemBinding
import org.pakicek.monoforecast.domain.model.dto.FileDto

class FileAdapter(
    private var files: List<FileDto>,
    private val onClick: (FileDto) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: FileDto) {
            binding.startText.text = file.startTime
            binding.endText.text = file.endTime
            binding.root.setOnClickListener { onClick(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun updateList(newList: List<FileDto>) {
        files = newList
        notifyDataSetChanged()
    }
}