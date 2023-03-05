package com.tapbi.spark.qrcode.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.ItemScanResultBinding
import com.tapbi.spark.qrcode.utils.checkTime


class ScanResultAdapter2(private val viewBinderHelper: ViewBinderHelper) :
    RecyclerView.Adapter<ScanResultAdapter2.ViewHolder>() {


    private var inter: OnItemClickListener? = null

    val scanList = arrayListOf<ScanResult>()

    fun submitList(list: List<ScanResult>?){
        scanList.clear()
        if (list != null) {
            scanList.addAll(list)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemScanResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun closeLayout(id: String) {
        viewBinderHelper.closeLayout(id)
    }

    override fun getItemCount(): Int {
        return if(scanList.isNotEmpty()) scanList.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(scanList.isNotEmpty()){
            viewBinderHelper.bind(holder.binding.layoutRoot, scanList[position].id.toString())
            holder.bind(scanList[position])
        }
    }

    inner class ViewHolder(val binding: ItemScanResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(scanResult: ScanResult) {
            binding.data = scanResult
            binding.ivFavorite.setImageResource(if (scanResult.isFavorite) R.drawable.rated_star else R.drawable.star_icon)
            binding.viewContent.setOnClickListener {
                if (checkTime(it))
                    inter?.onItemClick(scanResult.id)
            }
            binding.ivFavorite.setOnClickListener {
                if (checkTime(it)) {
                    inter?.onFavoriteClick(scanResult,adapterPosition)
                }
            }

            binding.ivDelete.setOnClickListener {
                if (checkTime(it)) {
                    inter?.onDeleteClick(scanResult,adapterPosition)
                }
            }

            binding.layoutRoot.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
                override fun onClosed(view: SwipeRevealLayout?) {
                    inter?.onClosed(scanResult.id)
                }

                override fun onOpened(view: SwipeRevealLayout?) {
                    inter?.onOpened(scanResult.id, adapterPosition)
                }

                override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {
                    inter?.onSlide(scanResult.id)
                }
            })
        }
    }

    fun setListener(listener: OnItemClickListener) {
        inter = listener
    }

    interface OnItemClickListener {
        fun onItemClick(id: Int)
        fun onFavoriteClick(scanResult: ScanResult,position: Int)
        fun onDeleteClick(scanResult: ScanResult,position: Int)
        fun onSlide(id: Int)
        fun onOpened(id: Int, position: Int)
        fun onClosed(id: Int)
    }
}