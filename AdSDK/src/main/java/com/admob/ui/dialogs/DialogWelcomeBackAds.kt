package com.admob.ui.dialogs

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.admob.ads.databinding.DialogWelcomeBackResumeBinding
import com.admob.delay
import com.admob.screenWidth
import com.admob.ui.BaseDialog

class DialogWelcomeBackAds(
    context: Context,
    private val onGotoApp: () -> Unit
) :  BaseDialog<DialogWelcomeBackResumeBinding>(context) {

    override val binding = DialogWelcomeBackResumeBinding.inflate(LayoutInflater.from(context))

    override fun onViewReady() {
        binding.btnGotoApp.setOnClickListener {
            onGotoApp.invoke()
            delay(200) {
                dismiss()
            }
        }
    }

    override fun show() {
        super.show()
        val width = screenWidth * getWidthPercent()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.CENTER)
    }
}
