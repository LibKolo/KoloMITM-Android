package io.githun.mucute.qwq.kolomitm.fragment.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.githun.mucute.qwq.kolomitm.R
import io.githun.mucute.qwq.kolomitm.databinding.FragmentHomeBinding
import io.githun.mucute.qwq.kolomitm.service.KoloMITMService
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var viewBinding: FragmentHomeBinding

    private val postNotificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        toggleKoloMITM()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.appVersions.text = fetchAppVersions()

        val floatingActionButton = viewBinding.floatingActionButton
        floatingActionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            } else {
                toggleKoloMITM()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                KoloMITMService.stateFlow.collect { state ->
                    if (state !== KoloMITMService.State.Loading) {
                        viewBinding.linearProgressIndicator.visibility = View.GONE
                        viewBinding.floatingActionButton.isClickable = true
                        viewBinding.floatingActionButton.isFocusable = true
                    } else {
                        viewBinding.linearProgressIndicator.visibility = View.VISIBLE
                        viewBinding.floatingActionButton.isClickable = false
                        viewBinding.floatingActionButton.isFocusable = false
                    }

                    floatingActionButton.setImageResource(
                        when (state) {
                            KoloMITMService.State.Active -> R.drawable.pause_24px
                            KoloMITMService.State.Inactive -> R.drawable.play_arrow_24px
                            KoloMITMService.State.Loading -> R.drawable.pace_24px
                        }
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun fetchAppVersions(): String {
        val context = requireContext()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode
        }
        return "$versionName (${versionCode})"
    }

    private fun toggleKoloMITM() {
        val context = requireContext()
        when (KoloMITMService.stateFlow.value) {
            KoloMITMService.State.Active -> context.startForegroundService(Intent(KoloMITMService.ACTION_STOP).apply {
                `package` = context.packageName
            })

            KoloMITMService.State.Inactive -> context.startForegroundService(Intent(KoloMITMService.ACTION_START).apply {
                `package` = context.packageName
            })

            KoloMITMService.State.Loading -> {}
        }
    }

}