package org.pakicek.monoforecast.presentation.sdui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import org.pakicek.monoforecast.MonoForecastApp
import org.pakicek.monoforecast.R
import org.pakicek.monoforecast.databinding.ActivitySduiBinding
import org.pakicek.monoforecast.domain.model.sdui.obj
import org.pakicek.monoforecast.domain.model.sdui.models.SduiAction
import org.pakicek.monoforecast.domain.model.sdui.models.SduiEffect
import org.pakicek.monoforecast.domain.model.sdui.models.TemplateVars
import org.pakicek.monoforecast.presentation.sdui.dialog.SduiDialogBuilder
import org.pakicek.monoforecast.utils.showSnackbar

class SduiActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySduiBinding
    private val renderer by lazy { SduiRenderer(this, layoutInflater) }

    private var activeDialog: AlertDialog? = null
    private var lastRawJson: String? = null

    private val viewModel: SduiViewModel by viewModels {
        val repo = (application as MonoForecastApp).container.echoRepository
        SduiViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySduiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnReload.setOnClickListener { viewModel.load(currentPath()) }
        binding.btnJson.setOnClickListener {
            val json = lastRawJson
            if (json.isNullOrBlank()) {
                binding.root.showSnackbar("No JSON loaded")
            } else {
                showRawJsonDialog(json)
            }
        }

        viewModel.load(currentPath())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectState() }
                launch { collectEffects() }
            }
        }
    }

    private suspend fun collectState() {
        viewModel.state.collect { state ->
            binding.progress.isVisible = state is SduiUiState.Loading
            binding.tvError.isVisible = state is SduiUiState.Error
            binding.contentContainer.isVisible = state is SduiUiState.Content

            when (state) {
                is SduiUiState.Loading -> {
                    lastRawJson = null
                    binding.tvSectionTitle.text = getString(R.string.bdui_header_loading)
                }

                is SduiUiState.Error -> {
                    lastRawJson = null
                    binding.tvSectionTitle.text = getString(R.string.bdui_header_error)
                    binding.tvError.text = state.message
                }

                is SduiUiState.Content -> {
                    lastRawJson = state.rawJson
                    binding.tvSectionTitle.text = state.page.title ?: "News"

                    val isMain = state.path == "/main"
                    renderer.render(
                        container = binding.contentContainer,
                        blocks = state.page.blocks,
                        isMainPage = isMain,
                        onAction = ::dispatchAction,
                        onDeleteAction = ::confirmDelete
                    )
                }
            }
        }
    }

    private suspend fun collectEffects() {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SduiEffect.Navigate -> navigate(effect.path)
                is SduiEffect.CloseDialog -> closeDialog()
                is SduiEffect.Message -> binding.root.showSnackbar(effect.text)
            }
        }
    }

    private fun currentPath(): String = intent.getStringExtra(EXTRA_PATH) ?: "/main"

    private fun navigate(path: String) {
        val target = path.trim()
        if (target == currentPath()) {
            viewModel.load(target)
            return
        }
        startActivity(Intent(this, SduiActivity::class.java).putExtra(EXTRA_PATH, target))
    }

    private fun dispatchAction(action: SduiAction) {
        if (action.type == "open_dialog") {
            val dialogObj = action.data?.obj("dialog") ?: run {
                binding.root.showSnackbar("open_dialog requires data.dialog")
                return
            }
            showDialog(dialogObj)
            return
        }

        val vars = TemplateVars(ts = System.currentTimeMillis().toString(), form = emptyMap())
        viewModel.runAction(action, vars, currentPath())
    }

    private fun confirmDelete(action: SduiAction) {
        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MonoForecast_MaterialAlertDialog)
            .setTitle(getString(R.string.bdui_delete_confirm_title))
            .setMessage(getString(R.string.bdui_delete_confirm_message))
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .setPositiveButton(getString(R.string.bdui_delete)) { d, _ ->
                val vars = TemplateVars(ts = System.currentTimeMillis().toString(), form = emptyMap())
                viewModel.runAction(action, vars, currentPath())
                d.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        dialog.show()
    }

    private fun showDialog(dialogObj: JsonObject) {
        val baseVars = TemplateVars(ts = System.currentTimeMillis().toString())
        val built = SduiDialogBuilder(this).build(dialogObj, baseVars)

        activeDialog = built.dialog

        built.dialog.setOnShowListener {
            built.dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                val a = built.negativeAction
                if (a == null || a.type == "close_dialog") {
                    closeDialog()
                } else {
                    val vars = built.buildVarsOrNull() ?: return@setOnClickListener
                    viewModel.runAction(a, vars, currentPath())
                }
            }

            built.dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val vars = built.buildVarsOrNull() ?: return@setOnClickListener
                val a = built.positiveAction ?: run {
                    closeDialog()
                    return@setOnClickListener
                }
                viewModel.runAction(a, vars, currentPath())
            }
        }

        built.dialog.show()
    }

    private fun closeDialog() {
        activeDialog?.dismiss()
        activeDialog = null
    }

    private fun showRawJsonDialog(rawJson: String) {
        val tv = TextView(this).apply {
            text = rawJson
            setTextIsSelectable(true)
            typeface = Typeface.MONOSPACE
            textSize = 12f
            setPadding(24, 16, 24, 16)
            isHorizontalScrollBarEnabled = true
            isVerticalScrollBarEnabled = false
            setHorizontallyScrolling(true)
        }

        val hScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            addView(tv)
        }

        val vScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
            addView(hScroll)
        }

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MonoForecast_MaterialAlertDialog)
            .setTitle(getString(R.string.bdui_raw_json_title))
            .setView(vScroll)
            .setNeutralButton(getString(R.string.copy)) { _, _ -> copyToClipboard(rawJson) }
            .setPositiveButton(getString(R.string.close)) { d, _ -> d.dismiss() }
            .show()
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("SDUI JSON", text))
        binding.root.showSnackbar("Copied")
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