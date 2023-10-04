package com.music.moodplay.presentation.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import com.music.moodplay.R

object CustomDialog {
    fun singleDialogWithoutListener(
        context: Context,
        message: String,
        positiveText: String = context.getString(R.string.ok),
        title: String = context.getString(R.string.app_name)
    ) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setCancelable(false)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton(positiveText) { dialog, _ ->
            dialog!!.dismiss()
        }.show()
    }

    fun singleDialogWithListener(
        context: Context, message: String,
        positiveText: String = context.getString(R.string.ok),
        listener: DialogListener,
        title: String = context.getString(R.string.app_name)
    ) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setCancelable(false)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton(positiveText) { dialog, _ ->
            dialog!!.dismiss()
            listener.onPositiveClick()
        }.show()
    }

    fun choiceDialogWithListener(
        context: Context, message: String,
        positiveText: String, negativeText: String,
        listener: DialogListener,
        title: String = context.getString(R.string.app_name)
    ) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setCancelable(false)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton(positiveText) { dialog, _ ->
            dialog!!.dismiss()
            listener.onPositiveClick()
        }.setNegativeButton(negativeText) { dialog, _ ->
            dialog.dismiss()
            listener.onNegativeClick()
        }.show()
    }
}