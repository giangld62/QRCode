package com.tapbi.spark.qrcode.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.qrcode.ui.main.create_qrcode.CreateFragment
import com.tapbi.spark.qrcode.ui.main.favorite.FavoriteFragment
import com.tapbi.spark.qrcode.ui.main.history.HistoryFragment
import com.tapbi.spark.qrcode.ui.main.scanner.ScannerFragment
import com.tapbi.spark.qrcode.ui.main.setting.SettingFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private var historyFragment: HistoryFragment? = null
    private var createFragment: CreateFragment? = null
    private var favoriteFragment: FavoriteFragment? = null
    private var settingFragment: SettingFragment? = null
    private var scannerFragment: ScannerFragment? = null


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (historyFragment == null) historyFragment = HistoryFragment()
                historyFragment!!
            }
            1 -> {
                if (createFragment == null) createFragment = CreateFragment()
                createFragment!!
            }
            2 -> {
                if (scannerFragment == null) scannerFragment = ScannerFragment()
                scannerFragment!!
            }
            3 -> {
                if (favoriteFragment == null) favoriteFragment = FavoriteFragment()
                favoriteFragment!!
            }
            else -> {
                if (settingFragment == null) settingFragment = SettingFragment()
                settingFragment!!
            }
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}