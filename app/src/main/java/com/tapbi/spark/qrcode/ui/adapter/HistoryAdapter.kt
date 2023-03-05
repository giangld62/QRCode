package com.tapbi.spark.qrcode.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanHistory
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.ItemHistoryBinding
import com.tapbi.spark.qrcode.utils.formatDate

class HistoryAdapter(private val inter: OnItemClickListener) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()

    init {
        viewBinderHelper.setOpenOnlyOne(true)
    }

    private val scanHistoryList = arrayListOf<ScanHistory>()

    fun submitList(list: List<ScanHistory>?){
        scanHistoryList.clear()
        if (list != null) {
            scanHistoryList.addAll(list)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
//    fun reset(recyclerView: RecyclerView,id:String, position: Int){
//        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as ViewHolder
//        viewHolder.scanResultAdapter.closeLayout(id)
//    }

    override fun getItemCount(): Int {
        return if(scanHistoryList.isEmpty()) 0 else scanHistoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(scanHistoryList.isNotEmpty()){
            holder.bind(scanHistoryList[position])
        }
    }

    fun closeLayout(id: String) {
        viewBinderHelper.closeLayout(id)
    }


    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val scanResultAdapter = ScanResultAdapter(viewBinderHelper)
        fun bind(scanHistory: ScanHistory) {
            binding.tvTime.text = if (scanHistory.time != 0L) formatDate(
                scanHistory.time,
                Constant.DATE_TYPE_2
            ) else scanHistory.category
            scanResultAdapter.submitList(scanHistory.scanResults)
            binding.rvScanResult.adapter = scanResultAdapter
            binding.rvScanResult.layoutManager = LinearLayoutManager(binding.root.context)
            scanResultAdapter.setListener(object : ScanResultAdapter.OnItemClickListener {
                override fun onItemClick(id: Int) {
                    inter.onItemClick(id)
                }

                override fun onFavoriteClick(id: Int, isFavorite: Boolean, rawValue: String) {
                    inter.onFavoriteClick(id, isFavorite, rawValue)
                }

                override fun onDeleteClick(scanResult: ScanResult, position: Int) {
                    inter.onDeleteClick(scanResult,position)
                    scanResultAdapter.scanList.remove(scanResult)
                    scanResultAdapter.notifyItemRemoved(position)
                    if (scanResultAdapter.scanList.isEmpty()) {
                        scanHistoryList.remove(scanHistory)
                        notifyItemRemoved(adapterPosition)
                    }
                }

                override fun onSlide(id: Int) {
                    inter.onSlide(id)
                }

                override fun onOpened(id: Int, position: Int) {
                    inter.onOpened(id, adapterPosition)
                }

                override fun onClosed(id: Int) {
                    inter.onClosed(id)
                }
            })
        }
    }

    interface OnItemClickListener {
        fun onItemClick(id: Int)
        fun onFavoriteClick(id: Int, isFavorite: Boolean, rawValue: String)
        fun onDeleteClick(scanResult: ScanResult,position: Int)
        fun onSlide(id: Int)
        fun onOpened(id: Int, position: Int)
        fun onClosed(id: Int)
    }
}