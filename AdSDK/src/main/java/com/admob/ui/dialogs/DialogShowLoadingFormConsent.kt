package com.admob.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import com.admob.ads.databinding.DialogLoadingFormConsentBinding
import com.admob.ui.BaseDialog

class DialogShowLoadingFormConsent(context: Context) : BaseDialog<DialogLoadingFormConsentBinding>(context) {
    override val binding = DialogLoadingFormConsentBinding.inflate(LayoutInflater.from(context))
    override fun onViewReady() {}
}