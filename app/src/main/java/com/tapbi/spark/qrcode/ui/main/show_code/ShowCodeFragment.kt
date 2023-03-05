package com.tapbi.spark.qrcode.ui.main.show_code

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.FragmentShowCodeBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingFragment
import com.tapbi.spark.qrcode.utils.checkTime
import com.tapbi.spark.qrcode.utils.showToast
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ShowCodeFragment : BaseBindingFragment<FragmentShowCodeBinding, ShowCodeViewModel>() {
    private var imageBitmap: Bitmap? = null

    private val requestPermissionWriteStorageLauncher =
        registerForActivityResult(RequestPermission()) { result ->
            if (!result && shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showDialogTryAgain()
            } else if (!result && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showDialogOpenSetting()
            } else if (result) {
                imageBitmap?.let { it1 -> viewModel.saveImage(requireContext(), it1) }
            }
        }

    private val isWriteExternalStoragePermissionGranted
        get() = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun showDialogTryAgain() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_external_storage))
            setPositiveButton(R.string.ok) { dialog, _ ->
                requestPermissionWriteStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.dismiss()
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showDialogOpenSetting() {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.permission_required)
            setMessage(getString(R.string.permission_external_storage))
            setPositiveButton(R.string.go_to_setting) { _, _ ->
                openSettings(activityWriteExternalStorageResultLauncher)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private val activityWriteExternalStorageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isWriteExternalStoragePermissionGranted) {
                imageBitmap?.let { it1 -> viewModel.saveImage(requireContext(), it1) }
            } else {
                showDialogOpenSetting()
            }
        }

    override fun getViewModel(): Class<ShowCodeViewModel> {
        return ShowCodeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_show_code

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
//        binding.ctlRoot.setPadding(0, getStatusBarHeight(), 0, 0)
        Timber.e("giangledinh savedInstanceState ${savedInstanceState == null}")
        savedInstanceState?.let { bundle ->
//            val byteArray = bundle.getByteArray(Constant.IMAGE_BITMAP)
//            Timber.e("giangledinh savedInstanceState ${byteArray == null}")
//            byteArray?.let {
//                val compressedBitmap = BitmapFactory.decodeByteArray(it,0,it.size)
//                imageBitmap = compressedBitmap
//                binding.ivCode.setImageBitmap(compressedBitmap)
//                Timber.e("giangledinh savedInstanceState ${compressedBitmap}")
//            }
            val image = bundle.getString(Constant.IMAGE_BITMAP, "")
//            val bitmap = BitmapFactory.decodeFile(image)
            Timber.e("giangledinh onResourceReady image $image ")

            Glide.with(requireContext()).asBitmap().load(image)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Timber.e("giangledinh onResourceReady ")
                        imageBitmap = resource
                        binding.ivCode.setImageBitmap(resource)
                        if (File(image).exists()) {
                            File(image).delete()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
        }
    }

    override fun onPermissionGranted() {
    }

    override fun initView() {
    }

    override fun evenClick() {
        binding.ivBack.setOnClickListener {
            if (checkTime(it))
                findNavController().popBackStack()
        }

        binding.tvShare.setOnClickListener {
            if (checkTime(it, 1000))
                imageBitmap?.let {
                    mainViewModel.getImageUri(requireContext(), it)
                }
        }

        binding.tvDownload.setOnClickListener {
            if (checkTime(it)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // CHECK VERSION
                    if (isWriteExternalStoragePermissionGranted) {
                        imageBitmap?.let { it1 -> viewModel.saveImage(requireContext(), it1) }
                    } else {
                        requestPermissionWriteStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                } else {
                    imageBitmap?.let { it1 -> viewModel.saveImage(requireContext(), it1) }
                }
            }
        }
    }

    override fun observerData() {
        mainViewModel.imageUriSaved.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                shareImage(it)
            }
        }

        mainViewModel.codeImageBitmap.observe(viewLifecycleOwner) {
            imageBitmap = it
            binding.ivCode.setImageBitmap(it)
        }

        viewModel.isSaveImageDone.observe(viewLifecycleOwner) {
            showToast(requireContext(), getString(R.string.save_image_successfully))
        }
    }

    override fun initData() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        lifecycleScope.launch(Dispatchers.IO){
//            Timber.e("giangledinh savedInstanceState ${imageBitmap == null}")
//            val stream = ByteArrayOutputStream()
//            imageBitmap?.compress(Bitmap.CompressFormat.JPEG,100,stream)
//            outState.putByteArray(Constant.IMAGE_BITMAP,stream.toByteArray())
//            Timber.e("giangledinh savedInstanceState ${stream.toByteArray().size}")
        imageBitmap?.let {
            val parent: String = requireContext().cacheDir.absolutePath + File.separator + "Thumb"
            if (!File(parent).exists()) {
                File(parent).mkdir()
            }
            val file = File(parent, "image" + System.currentTimeMillis() + ".jpg")
            outState.putString(Constant.IMAGE_BITMAP, saveBitmapToLocal(it, file))
        }
//        }
    }

    private fun saveBitmapToLocal(bm: Bitmap, file: File): String? {
        val path: String? = try {
            val fos = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return path
    }
}