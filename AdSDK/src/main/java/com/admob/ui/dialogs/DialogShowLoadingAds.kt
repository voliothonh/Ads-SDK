package com.admob.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import com.admob.ads.databinding.DialogLoadingInterBinding
import com.admob.ui.BaseDialog

class DialogShowLoadingAds(context: Context) : BaseDialog<DialogLoadingInterBinding>(context) {
    override val binding = DialogLoadingInterBinding.inflate(LayoutInflater.from(context))
    override fun onViewReady() {}
}