package io.githun.mucute.qwq.kolomitm.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import io.githun.mucute.qwq.kolomitm.R
import io.githun.mucute.qwq.kolomitm.databinding.LayoutAccountItemBinding
import io.githun.mucute.qwq.kolomitm.manager.AccountManager
import io.githun.mucute.qwq.kolomitm.model.Account

class AccountAdapter(
    val context: Context,
    var accounts: List<Account> = AccountManager.accounts.value,
    var selectedAccount: Account? = AccountManager.selectedAccount.value
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    private val inflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutAccountItemBinding.inflate(
                inflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val viewBinding = holder.viewBinding
        val account = accounts[position]

        viewBinding.root.setOnClickListener {
            if (account == selectedAccount) {
                AccountManager.selectAccount(null)
            } else {
                AccountManager.selectAccount(account)
            }
        }
        viewBinding.accountName.text = account.session.mcChain.displayName
        viewBinding.deviceType.text = account.deviceType
        viewBinding.accountSelected.visibility = if (account === selectedAccount) {
            View.VISIBLE
        } else {
            View.GONE
        }

        viewBinding.materialButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(context, view, Gravity.BOTTOM)
            popupMenu.setForceShowIcon(true)
            popupMenu.inflate(R.menu.account_item_menu)

            val toggleSelectionItem = popupMenu.menu.findItem(R.id.toggleSelection)
            toggleSelectionItem.setIcon(if (account == selectedAccount) R.drawable.check_box_24px else R.drawable.check_box_outline_blank_24px)
            toggleSelectionItem.setTitle(if (account == selectedAccount) R.string.unselect else R.string.select)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.toggleSelection -> {
                        if (account == selectedAccount) {
                            AccountManager.selectAccount(null)
                        } else {
                            AccountManager.selectAccount(account)
                        }
                    }

                    R.id.delete -> {
                        AccountManager.removeAccount(account)
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    inner class ViewHolder(val viewBinding: LayoutAccountItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

}