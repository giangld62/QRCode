package com.tapbi.spark.qrcode.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.databinding.ItemStarBinding
import timber.log.Timber


class RateAppAdapter(private val stars: List<Int>) :
    RecyclerView.Adapter<RateAppAdapter.ViewHolder>() {

    private var checkedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStarBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun unCheckAllStar() {
        checkedPosition = -1
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stars[position])
    }

    override fun getItemCount(): Int {
        return stars.size
    }

    inner class ViewHolder(private val binding: ItemStarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(star: Int) {
            binding.ivStar.tag = "ivStar"
            if (adapterPosition <= checkedPosition) {
                binding.ivStar.setImageResource(R.drawable.yellow_star)
            } else {
                binding.ivStar.setImageResource(star)
            }
            binding.root.setOnClickListener {
                checkedPosition = adapterPosition
                onItemClickListener?.let {
                    it(adapterPosition + 1)
                }
                notifyItemRangeChanged(0, 5)
            }
        }
    }

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: ((Int) -> Unit)) {
        onItemClickListener = listener
    }
}