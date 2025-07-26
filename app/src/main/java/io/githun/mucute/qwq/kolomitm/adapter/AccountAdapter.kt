package io.githun.mucute.qwq.kolomitm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.githun.mucute.qwq.kolomitm.databinding.LayoutAccountItemBinding

class AccountAdapter(
    val context: Context
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
    }

    override fun getItemCount(): Int {
        return 10
    }

    inner class ViewHolder(val viewBinding: LayoutAccountItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

}