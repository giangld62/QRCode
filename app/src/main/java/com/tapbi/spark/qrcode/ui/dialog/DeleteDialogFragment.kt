package com.tapbi.spark.qrcode.ui.dialog

import android.os.Bundle
import android.view.View
import com.tapbi.spark.qrcode.R
import com.tapbi.spark.qrcode.common.Constant
import com.tapbi.spark.qrcode.databinding.DialogDeleteBinding
import com.tapbi.spark.qrcode.ui.base.BaseBindingDialogFragment
import com.tapbi.spark.qrcode.utils.checkTime


class DeleteDialogFragment :
    BaseBindingDialogFragment<DialogDeleteBinding>() {

    private var inter: OnDeleteClickListener? = null

    override val layoutId: Int
        get() = R.layout.dialog_delete

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        binding?.tvDialogTitle?.text = arguments?.getString(Constant.DELETE_FRAGMENT_TITLE)
        binding?.tvDialogMessage?.text = arguments?.getString(Constant.DELETE_FRAGMENT_MESSAGE)

        binding?.btnCancel?.setOnClickListener {
            if (checkTime(it)) {
                dismiss()
            }
        }

        binding?.btnDelete?.setOnClickListener {
            if (checkTime(it)) {
                inter?.onDeleteClick()
                dismiss()
            }
        }
    }

    interface OnDeleteClickListener {
        fun onDeleteClick()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            param1: String,
            param2: String,
            inter: OnDeleteClickListener
        ): DeleteDialogFragment {
            val frag = DeleteDialogFragment()
            val args = Bundle()
            args.putString(Constant.DELETE_FRAGMENT_TITLE, param1)
            args.putString(Constant.DELETE_FRAGMENT_MESSAGE, param2)
            frag.arguments = args
            frag.inter = inter
            return frag
        }
    }
}