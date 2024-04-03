package com.admob.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.admob.ads.R
import com.admob.ui.BaseDialog

class DialogShowLoadingAds(context: Context) : BaseDialog<ViewDataBinding>(context) {
    override val binding: ViewDataBinding = DataBindingUtil.inflate<ViewDataBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_loading_inter,
        null,
        false
    )

    override fun onViewReady() {}
}