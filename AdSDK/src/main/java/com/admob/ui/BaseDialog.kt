package com.admob.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.admob.screenWidth

abstract class BaseDialog<V : ViewDataBinding>(
    context: Context,
    private val gravity: Int = Gravity.CENTER
) : Dialog(context) {

    open val isCancelable = false

    abstract val binding: V

    abstract fun onViewReady()

    open fun getWidthPercent(): Float = 0.85F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCancelable)
        onViewReady()
    }

    final override fun setContentView(layoutResID: Int) {
        super.setContentView(binding.root)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCancelable)
        onViewReady()
    }

    override fun show() {
        super.show()
        val width = screenWidth * getWidthPercent()
        window?.setLayout(width.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(gravity)
    }
}