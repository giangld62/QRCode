package com.tapbi.spark.qrcode.ui.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ironman.trueads.admob.open.AppOpenAdAdmob
import com.ironman.trueads.admob.open.ShowOpenAdsAdmobListener
import com.tapbi.spark.qrcode.App
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.databinding.FragmentSplashBinding
import com.tapbi.spark.qrcode.ui.main.MainViewModel
import timber.log.Timber

class SplashFragment : Fragment() {
    lateinit var mainViewModel: MainViewModel
    private var binding: FragmentSplashBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {


            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        loadAds()
        mainViewModel.finishLoadAds.observe(viewLifecycleOwner) { aBoolean ->
            if (aBoolean) {
                goHome()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).isShowAdsBack = true
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).isResume = true
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).currentActivity = null
    }

    private fun loadAds() {
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).isShowAdsBack = true
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).isResume = true
        AppOpenAdAdmob.Companion.getInstance(App.instance!!).loadAndShowOpenAdsAdmob(
            requireActivity(),
            getString(R.string.admob_id_ads_open),
            true,
            object : ShowOpenAdsAdmobListener {
                override fun onLoadedAdsOpenApp() {
                    Timber.e("SPLASH onLoadedAdsOpenApp")
                }

                override fun onLoadFailAdsOpenApp() {

                    Timber.e("SPLASH onLoadFailAdsOpenApp")
                    mainViewModel.finishLoadAds.postValue(true)
                }

                override fun onShowAdsOpenAppDismissed() {
                    Timber.e("SPLASH onShowAdsOpenAppDismissed")
                    mainViewModel.finishLoadAds.postValue(true)
                }

                override fun onAdsOpenLoadedButNotShow() {
                    Timber.e("SPLASH onAdsOpenLoadedButNotShow")
                    mainViewModel.finishLoadAds.postValue(true)
                }
            })
    }

    private fun goHome() {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
    }
}