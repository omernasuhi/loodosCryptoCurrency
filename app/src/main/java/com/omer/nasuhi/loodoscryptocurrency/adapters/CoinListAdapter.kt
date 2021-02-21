package com.omer.nasuhi.loodoscryptocurrency.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.omer.nasuhi.loodoscryptocurrency.R
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinResponse
import kotlinx.android.synthetic.main.row_list.view.*


class CoinListAdapter : RecyclerView.Adapter<CoinListAdapter.CoinListViewHolder>() {

    private var lastPosition: Int = -1

    class CoinListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<CoinResponse>() {
        override fun areItemsTheSame(oldItem: CoinResponse, newItem: CoinResponse): Boolean {
            return oldItem.coinId == newItem.coinId
        }

        override fun areContentsTheSame(oldItem: CoinResponse, newItem: CoinResponse): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoinListViewHolder {
        return CoinListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CoinListViewHolder, position: Int) {
        val coinItem = differ.currentList[position]
        holder.itemView.apply {


            val characterCount = coinItem.name.length

            if (characterCount >= 15) {

                tvDailyChange.text = coinItem.name.substring(0, 15) + "..."
            } else {
                tvDailyChange.text = coinItem.name
            }
            tvCoinName.text = coinItem.symbol.toUpperCase()

        }

        val animation: Animation = AnimationUtils.loadAnimation(
            holder.itemView.context,
            if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )

        holder.itemView.startAnimation(animation)
        lastPosition = position

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(coinItem)
        }
    }

    private var onItemClickListener: ((CoinResponse) -> Unit)? = null

    fun setOnItemClickListener(listener: (CoinResponse) -> Unit) {
        onItemClickListener = listener
    }
}