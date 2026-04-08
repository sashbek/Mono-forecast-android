package org.pakicek.monoforecast.presentation.bdui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ActivityBduiBinding
import org.pakicek.monoforecast.utils.showSnackbar

class BduiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBduiBinding

    private val viewModel: BduiViewModel by viewModels {
        val repo = (application as MonoForecastApp).container.bduiRepository
        BduiViewModelFactory(repo)
    }

    private val renderer by lazy { BduiRenderer(this, layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBduiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()

        binding.btnBack.setOnClickListener { finish() }

        viewModel.load(currentPath())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.isVisible = state is BduiUiState.Loading
                    binding.tvError.isVisible = state is BduiUiState.Error
                    binding.contentContainer.isVisible = state is BduiUiState.Content

                    when (state) {
                        is BduiUiState.Loading -> {
                            binding.tvSectionTitle.text = getString(R.string.bdui_header_loading)
                        }

                        is BduiUiState.Error -> {
                            binding.tvSectionTitle.text = getString(R.string.bdui_header_error)
                            binding.tvError.text = state.message
                        }

                        is BduiUiState.Content -> {
                            binding.tvSectionTitle.text = state.page.title

                            val isMainPage = state.path == "/main"
                            renderer.render(
                                container = binding.contentContainer,
                                blocks = state.page.blocks,
                                isMainPage = isMainPage,
                                onNavigate = ::navigate,
                                onAction = ::handleAction,
                                onDeletePost = { postId, postPath -> confirmDelete(postId, postPath) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun currentPath(): String = intent.getStringExtra(EXTRA_PATH) ?: "/main"

    private fun navigate(path: String) {
        startActivity(
            Intent(this, BduiActivity::class.java)
                .putExtra(EXTRA_PATH, path)
        )
    }

    private fun handleAction(action: String) {
        val path = currentPath()
        val isMain = path == "/main"

        when (action) {
            "add_post" -> {
                if (!isMain) {
                    binding.root.showSnackbar(getString(R.string.bdui_add_only_main))
                    return
                }
                showAddPostDialog()
            }

            "reload" -> viewModel.load(path)

            else -> binding.root.showSnackbar("Unknown action: $action")
        }
    }

    private fun showAddPostDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_post, null)

        val titleEt = dialogView.findViewById<EditText>(R.id.etTitle)
        val bodyEt = dialogView.findViewById<EditText>(R.id.etBody)
        val iconSpinner = dialogView.findViewById<Spinner>(R.id.spinnerIcon)

        val icons = listOf(
            "ic_forecast_button",
            "ic_location_button",
            "ic_bluetooth_button",
            "ic_logs_button",
            "ic_warning"
        )
        iconSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, icons)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.bdui_add_post))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .setPositiveButton(getString(R.string.save), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleEt.text?.toString()?.trim().orEmpty()
                val body = bodyEt.text?.toString()?.trim().orEmpty()
                val iconKey = icons.getOrElse(iconSpinner.selectedItemPosition) { "ic_logs_button" }

                if (title.isBlank() || body.isBlank()) {
                    binding.root.showSnackbar(getString(R.string.bdui_fill_fields))
                    return@setOnClickListener
                }

                viewModel.addPost(title, body, iconKey)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDelete(postId: String?, postPath: String) {
        if (currentPath() != "/main") {
            binding.root.showSnackbar(getString(R.string.bdui_delete_only_main))
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.bdui_delete_confirm_title))
            .setMessage(getString(R.string.bdui_delete_confirm_message))
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .setPositiveButton(getString(R.string.bdui_delete)) { d, _ ->
                viewModel.deletePostFromMain(postId, postPath)
                d.dismiss()
            }
            .show()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_PATH = "EXTRA_PATH"
    }
}