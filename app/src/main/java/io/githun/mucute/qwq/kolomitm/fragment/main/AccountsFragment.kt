package io.githun.mucute.qwq.kolomitm.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.githun.mucute.qwq.kolomitm.adapter.AccountAdapter
import io.githun.mucute.qwq.kolomitm.databinding.FragmentAccountsBinding

class AccountsFragment : Fragment() {

    private lateinit var viewBinding: FragmentAccountsBinding

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
        viewBinding.recyclerView.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(),
                MaterialDividerItemDecoration.VERTICAL
            ).also { it.isLastItemDecorated = false }
        )
        viewBinding.recyclerView.adapter = AccountAdapter(requireContext())

    }

}