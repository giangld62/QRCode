package com.tapbi.spark.qrcode.ui.main.create_history

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.FragmentCreateHistoryBinding
import com.tapbi.spark.qrcode.ui.adapter.HistoryAdapter
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.dialog.DeleteDialogFragment
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.gone
import com.tapbi.spark.qrcode.utils.show
import timber.log.Timber

class CreateHistoryFragment :
    BaseBindingFragment<FragmentCreateHistoryBinding, CreateHistoryViewModel>() {
    private var createHistoryAdapter: HistoryAdapter? = null
    private var scanResultIdIsOpening = -1

    override fun getViewModel(): Class<CreateHistoryViewModel> {
        return CreateHistoryViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_create_history

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun onPermissionGranted() {

    }

    private fun closeLayout(){
        createHistoryAdapter?.closeLayout(scanResultIdIsOpening.toString())
    }


    override fun initView() {
        createHistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(id: Int) {
                val bundle = Bundle()
                bundle.putBoolean(Constant.IS_EDITABLE, true)
                bundle.putBoolean(Constant.FROM_CREATE_HISTORY_FRAGMENT, true)
                bundle.putInt(Constant.SCAN_RESULT_ID, id)
                navigationToFragmentWithAnimation(R.id.scanResultFragment, bundle)
            }

            override fun onFavoriteClick(id: Int, isFavorite: Boolean, rawValue: String) {
                mainViewModel.updateFavorite(id, !isFavorite)
            }

            override fun onDeleteClick(scanResult: ScanResult, position: Int) {
                mainViewModel.deleteScanResultById(scanResult.id)
            }

            override fun onSlide(id: Int) {
            }

            override fun onOpened(id: Int, position: Int) {
                scanResultIdIsOpening = id
            }

            override fun onClosed(id: Int) {
            }
        })
        binding.rvCreateHistory.adapter = createHistoryAdapter
    }

    override fun evenClick() {
        binding.ivBack.setOnClickListener {
            if (checkTime(it))
                findNavController().popBackStack()
        }

        binding.ivDelete.setOnClickListener {
            if(checkTime(it)){
                DeleteDialogFragment.newInstance(getString(R.string.delete_history),
                    getString(R.string.do_you_want_to_delete_all_generated_qr_code),
                    object : DeleteDialogFragment.OnDeleteClickListener {
                        override fun onDeleteClick() {
                            mainViewModel.deleteAllScanResult(true)
                        }
                    }).show(requireActivity().supportFragmentManager, null)
            }
        }

        binding.rvCreateHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                closeLayout()
            }
        })
    }

    override fun observerData() {
        mainViewModel.isTimeChange.observe(viewLifecycleOwner) {
            mainViewModel.getAllQrCode(true)
        }

        mainViewModel.getAllQrCodeCreateByUser.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvNoData.show()
                binding.ivDelete.gone()
            } else {
                binding.tvNoData.gone()
                binding.ivDelete.show()
            }
            createHistoryAdapter?.submitList(it)
        }

        mainViewModel.rowCountCreateByUserLiveData.observe(viewLifecycleOwner) {
            Timber.e("rowCountCreateByUserLiveData ${it}")
            if (it == 0) {
                binding.tvNoData.show()
                binding.ivDelete.gone()
                createHistoryAdapter?.submitList(null)
            }
        }

        mainViewModel.isDeleteAllScanResultDone.observe(viewLifecycleOwner) {
            createHistoryAdapter?.submitList(null)
            binding.tvNoData.show()
            binding.ivDelete.gone()
        }
    }

    override fun initData() {
        mainViewModel.getAllQrCode(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createHistoryAdapter = null
    }

}