package com.admob.ui.dialogs

import android.app.Activity
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.admob.ads.databinding.DialogBackgroundOpenResumeBinding
import com.admob.screenWidth
import com.admob.ui.BaseDialog

class DialogBackgroundOpenApp(
    val activity: Activity
) : BaseDialog<DialogBackgroundOpenResumeBinding>(activity) {

    override fun getWidthPercent() = 1f

    override val binding = DialogBackgroundOpenResumeBinding.inflate(LayoutInflater.from(activity))


    private val activityCallback = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            dismiss()
        }
    }

    override fun onViewReady() {
        (activity as? AppCompatActivity)?.lifecycle?.addObserver(activityCallback)
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        (activity as? AppCompatActivity)?.lifecycle?.removeObserver(activityCallback)
        super.setOnDismissListener(listener)
    }

    override fun show() {
        super.show()
        val width = screenWidth * getWidthPercent()
        window?.setLayout(width.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.CENTER)
    }
}