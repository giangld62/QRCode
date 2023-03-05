package com.tapbi.spark.qrcode.ui.main.history

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.data.model.ScanResult
import com.tapbi.spark.qrcode.databinding.FragmentHistoryBinding
import com.tapbi.spark.qrcode.ui.adapter.HistoryAdapter
import com.tapbi.spark.qrcode.ui.adapter.ScanResultAdapter
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.ui.dialog.DeleteDialogFragment
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.gone
import com.tapbi.spark.qrcode.utils.show


class HistoryFragment : BaseBindingFragment<FragmentHistoryBinding, HistoryViewModel>(),
    HistoryAdapter.OnItemClickListener, ScanResultAdapter.OnItemClickListener {
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var scanResultAdapter: ScanResultAdapter
    private var scanResultIsOpening = -1
    private var dialogFragment: DeleteDialogFragment? = null

    private var queryString: String? = ""
    private var isShowFilter = false
    private val sortTypeSaved
        get() = sharedPreferenceHelper.getStringWithDefault(
            Constant.SORT_HISTORY,
            Constant.DATE
        )
    private var idClicked = -1

    override fun getViewModel(): Class<HistoryViewModel> {
        return HistoryViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override val layoutId: Int
        get() = R.layout.fragment_history

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
//        Timber.e("giangledinh getStatusBarHeight ${getStatusBarHeight()}")
    }

    override fun onPermissionGranted() {
    }


    override fun onPause() {
        super.onPause()
        if (isShowFilter) {
            goneFilter()
        }
        if (binding.searchView.query.toString().isNotEmpty()) {
            binding.searchView.setQuery("", false)
        }
        closeLayout()
    }

    override fun initView() {
        historyAdapter = HistoryAdapter(this)
        val viewBinderHelper = ViewBinderHelper()
        viewBinderHelper.setOpenOnlyOne(true)
        scanResultAdapter = ScanResultAdapter(viewBinderHelper)
        scanResultAdapter.setListener(this)
        binding.rvHistory.adapter = historyAdapter
        when (sortTypeSaved) {
            Constant.DATE -> getHistoryList(Constant.DATE, "%%")
            Constant.CATEGORY -> getHistoryList(Constant.CATEGORY, "%%")
            Constant.NAME -> getHistoryListOrderByName("%%")
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    override fun evenClick() {
        binding.ivFilter.setOnClickListener {
            if (isShowFilter) {
                goneFilter()
            } else
                showFilter()
        }

        binding.ctlRoot.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isShowFilter) {
                        goneFilter()
                    }
                    closeLayout()
                }
                MotionEvent.ACTION_UP -> view.performClick()
            }
            return@setOnTouchListener false
        }
        binding.tvDate.setOnClickListener {
            if (sortTypeSaved != Constant.DATE) {
                getHistoryList(Constant.DATE, "%${queryString}%")
            } else {
                goneFilter()
            }
        }

        binding.tvType.setOnClickListener {
            if (sortTypeSaved != Constant.CATEGORY) {
                getHistoryList(Constant.CATEGORY, "%${queryString}%")
            } else {
                goneFilter()
            }
        }

        binding.tvName.setOnClickListener {
            if (sortTypeSaved != Constant.NAME) {
                getHistoryListOrderByName("%${queryString}%")
            } else {
                goneFilter()
            }
        }

        binding.ivDelete.setOnClickListener {
            if (checkTime(it)) {
                if (isShowFilter)
                    goneFilter()
                dialogFragment =
                    DeleteDialogFragment.newInstance(getString(R.string.delete_qr_code),
                        getString(R.string.do_you_want_to_delete),
                        object : DeleteDialogFragment.OnDeleteClickListener {
                            override fun onDeleteClick() {
                                mainViewModel.deleteAllScanResult(false)
                            }
                        })
                dialogFragment?.show(childFragmentManager, null)
            }
        }

        binding.searchView.post {
            if (isAdded) {
                binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        binding.searchView.clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        queryString = newText
                        getHistoryListByType("%${newText}%")
                        return true
                    }
                })

                binding.searchView.setOnQueryTextFocusChangeListener { view, b ->
                    if (b && isShowFilter) {
                        goneFilter()
                    }
                    closeLayout()
                }
            }
        }

        binding.rvHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isShowFilter) {
                    goneFilter()
                }
                closeLayout()
            }
        })

        binding.rvHistory.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isShowFilter) {
                        goneFilter()
                    }
                }
                MotionEvent.ACTION_UP -> view.performClick()
            }
            return@setOnTouchListener false
        }
    }

    override fun observerData() {
        mainViewModel.isTimeChange.observe(viewLifecycleOwner) {
            if (sortTypeSaved == Constant.DATE) {
                getHistoryList(Constant.DATE, "%${queryString}%")
            }
        }

        mainViewModel.rowCountLiveData.observe(viewLifecycleOwner) {
            if (it == 0) {
                loadViewWhenEmptyList()
                historyAdapter.submitList(null)
            }
        }

        mainViewModel.scanHistoryList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    loadViewWhenEmptyList()
                } else {
                    binding.tvNoData.gone()
                    binding.ivDelete.show()
                }
                historyAdapter.submitList(it)
                mainViewModel.scanHistoryList.postValue(null)
            }
        }

        mainViewModel.isDeleteAllScanResultDone.observe(viewLifecycleOwner) {
            historyAdapter.submitList(null)
            scanResultAdapter.submitList(null)
            loadViewWhenEmptyList()
            updateFavoriteList()
        }

        mainViewModel.isDeleteScanResultByIdDone.observe(viewLifecycleOwner) {
            updateFavoriteList()
        }

        mainViewModel.isUpdateFavoriteDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                updateFavoriteList()
            }
        }

        mainViewModel.scanResultOrderByName.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    binding.tvNoData.show()
                } else {
                    binding.tvNoData.gone()
                }
                scanResultAdapter.submitList(it)
                mainViewModel.scanResultOrderByName.postValue(null)
            }
        }
        mainViewModel.eventScreen.observe(viewLifecycleOwner) {
            if (it) {
                val bundle = Bundle()
                bundle.putBoolean(Constant.FROM_FAVORITE_FRAGMENT, false)
                bundle.putInt(Constant.SCAN_RESULT_ID, idClicked)
                navigationToFragmentWithAnimation(R.id.scanResultFragment, bundle)
                mainViewModel.eventScreen.postValue(false)
            }
        }

    }

    private fun updateFavoriteList() {
        when (sharedPreferenceHelper.getStringWithDefault(Constant.SORT_FAVORITE, Constant.DATE)) {
            Constant.DATE -> mainViewModel.getAllFavoriteScanResults(Constant.DATE, "%%")
            Constant.CATEGORY -> mainViewModel.getAllFavoriteScanResults(Constant.CATEGORY, "%%")
            Constant.NAME -> mainViewModel.getFavoriteListOrderByName("%%")
        }
    }

    private fun getHistoryListByType(queryString: String?) {
        when (sortTypeSaved) {
            Constant.DATE -> mainViewModel.getScanHistoryList(
                Constant.DATE,
                queryString
            )
            Constant.CATEGORY -> mainViewModel.getScanHistoryList(
                Constant.CATEGORY,
                queryString
            )
            Constant.NAME -> mainViewModel.getScanResultOrderByName(queryString)
        }
    }

    override fun initData() {
    }

    override fun onItemClick(id: Int) {
        if (isShowFilter)
            goneFilter()
        idClicked = id
        showAdsFull()
    }

    override fun nextAfterFullScreen() {
        super.nextAfterFullScreen()
        if (idClicked != -1 && isAdded) {
            mainViewModel.eventScreen.postValue(true)
        }
    }

    override fun onFavoriteClick(id: Int, isFavorite: Boolean, rawValue: String) {
        if (isShowFilter)
            goneFilter()
        mainViewModel.updateFavorite(id, !isFavorite)
    }

    override fun onDeleteClick(scanResult: ScanResult, position: Int) {
        if (isShowFilter)
            goneFilter()
        mainViewModel.deleteScanResultById(scanResult.id)
    }

    override fun onSlide(id: Int) {
        if (isShowFilter)
            goneFilter()
    }

    override fun onOpened(id: Int, position: Int) {
        scanResultIsOpening = id
    }

    override fun onClosed(id: Int) {

    }

    private fun loadViewWhenEmptyList() {
        binding.tvNoData.show()
        binding.ivDelete.gone()
    }

    private fun getHistoryList(orderBy: String, queryString: String) {
        setUpAdapter()
        mainViewModel.getScanHistoryList(orderBy, queryString)
        if (orderBy == Constant.DATE)
            saveState(
                Constant.DATE, ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.tick_icon,
                    null
                ), null, null
            )
        else
            saveState(
                Constant.CATEGORY, null, ResourcesCompat.getDrawable(
                    requireContext().resources,
                    R.drawable.tick_icon,
                    null
                ), null
            )
    }

    private fun setUpAdapter() {
        closeLayout()
        binding.rvHistory.adapter = historyAdapter
        historyAdapter.submitList(null)
        scanResultAdapter.submitList(null)
    }

    private fun closeLayout() {
        scanResultAdapter.closeLayout(scanResultIsOpening.toString())
        historyAdapter.closeLayout(scanResultIsOpening.toString())
    }

    private fun getHistoryListOrderByName(queryString: String?) {
        setUpAdapter2()
        mainViewModel.getScanResultOrderByName(queryString)
        saveState(
            Constant.NAME, null, null, ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.tick_icon,
                null
            )
        )
    }

    private fun setUpAdapter2() {
        historyAdapter.closeLayout(scanResultIsOpening.toString())
        binding.rvHistory.adapter = scanResultAdapter
        historyAdapter.submitList(null)
        scanResultAdapter.submitList(null)
    }

    private fun saveState(
        sortTypeSaved: String,
        drawableDate: Drawable?,
        drawableType: Drawable?,
        drawableName: Drawable?
    ) {
        sharedPreferenceHelper.storeString(Constant.SORT_HISTORY, sortTypeSaved)
        binding.tvDate.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableDate, null, null, null
        )
        binding.tvType.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableType, null, null, null
        )
        binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableName, null, null, null
        )
        goneFilter()
    }

    private fun showFilter() {
        isShowFilter = true
        binding.llSortOrder.show()
    }

    private fun goneFilter() {
        isShowFilter = false
        binding.llSortOrder.gone()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialogFragment?.dismissAllowingStateLoss()
    }
}