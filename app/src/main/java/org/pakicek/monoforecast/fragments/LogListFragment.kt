package org.pakicek.monoforecast.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.FragmentLogListBinding
import org.pakicek.monoforecast.logic.viewmodel.LogsViewModel
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.adapters.FileAdapter
import org.pakicek.monoforecast.domain.model.dto.FileDto

class LogListFragment : Fragment(R.layout.fragment_log_list) {
    private var _binding: FragmentLogListBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private val viewModel: LogsViewModel by activityViewModels()

    private lateinit var adapter: FileAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)

        adapter = FileAdapter(emptyList()) { file ->
            openDetails(file)
        }

        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFiles.adapter = adapter

        lifecycleScope.launch {
            viewModel.getFiles().collect { list ->
                adapter.updateList(list)
            }
        }
    }

    private fun openDetails(file: FileDto) {
        val fragment = LogDetailsFragment.newInstance(file.id)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.logsFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
