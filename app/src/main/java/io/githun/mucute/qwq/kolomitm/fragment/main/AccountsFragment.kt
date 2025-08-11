package io.githun.mucute.qwq.kolomitm.fragment.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.githun.mucute.qwq.kolomitm.R
import io.githun.mucute.qwq.kolomitm.activity.AuthActivity
import io.githun.mucute.qwq.kolomitm.adapter.AccountAdapter
import io.githun.mucute.qwq.kolomitm.databinding.FragmentAccountsBinding
import io.githun.mucute.qwq.kolomitm.manager.AccountManager
import io.githun.mucute.qwq.kolomitm.util.DeviceTypeAndroid
import io.githun.mucute.qwq.kolomitm.util.DeviceTypeIos
import io.githun.mucute.qwq.kolomitm.util.DeviceTypeNintendo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AccountsFragment : Fragment() {

    private lateinit var viewBinding: FragmentAccountsBinding

    private val adapter by lazy { AccountAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentAccountsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.adapter = adapter
        viewBinding.floatingActionButton.setOnClickListener {
            showDeviceTypeChoicesDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AccountManager.accounts
                    .onEach { afterAccounts ->
                        val beforeAccounts = adapter.accounts
                        val differ = if (afterAccounts.size > beforeAccounts.size) afterAccounts.minus(beforeAccounts) else beforeAccounts.minus(afterAccounts)
                        if (differ.isEmpty()) {
                            return@onEach
                        }

                        adapter.accounts = afterAccounts
                        if (!beforeAccounts.containsAll(differ)) {
                            adapter.notifyItemRangeInserted(beforeAccounts.size, differ.size)

                        } else {
                            val index = beforeAccounts.indexOf(differ.first())
                            adapter.notifyItemRangeRemoved(index, differ.size)
                        }

                    }
                    .launchIn(this)

                AccountManager.selectedAccount
                    .onEach { afterSelectedAccount ->
                        val beforeSelectedAccount = adapter.selectedAccount

                        adapter.selectedAccount = afterSelectedAccount
                        if (beforeSelectedAccount == null) {
                            val currentIndex = adapter.accounts.indexOf(afterSelectedAccount).takeIf { it >= 0 } ?: return@onEach
                            adapter.notifyItemChanged(currentIndex)
                            return@onEach
                        }

                        if (afterSelectedAccount == null) {
                            val currentIndex = adapter.accounts.indexOf(beforeSelectedAccount).takeIf { it >= 0 } ?: return@onEach
                            adapter.notifyItemChanged(currentIndex)
                            return@onEach
                        }

                        val beforeIndex = adapter.accounts.indexOf(beforeSelectedAccount).takeIf { it >= 0 } ?: return@onEach
                        val currentIndex = adapter.accounts.indexOf(afterSelectedAccount).takeIf { it >= 0 } ?: return@onEach

                        adapter.notifyItemChanged(beforeIndex)
                        adapter.notifyItemChanged(currentIndex)
                    }
                    .launchIn(this)
            }
        }
    }

    private fun showDeviceTypeChoicesDialog() {
        var deviceType = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.login_as)
            .setSingleChoiceItems(
                R.array.device_types,
                0
            ) { _, which ->
                deviceType = which
            }
            .setPositiveButton(R.string.confirm) { _, _ ->
                startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
                    putExtra("deviceType", when (deviceType) {
                        0 -> DeviceTypeAndroid
                        1 -> DeviceTypeIos
                        else -> DeviceTypeNintendo
                    })
                })
            }
            .show()
    }

}