package com.raywenderlich.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment
import com.raywenderlich.placebook.R

class PhotoOptionDialogFragment: DialogFragment() {
    interface PhotoDialogOptionListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    private lateinit var listener: PhotoDialogOptionListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = activity as PhotoDialogOptionListener

        var captureSelectIdx = -1
        var pickSelectIdx = -1

        val options = ArrayList<String>()
        val context = activity as Context

        if (canCapturePhoto(context)) {
            options.add(getString(R.string.photo_option_camera))
            captureSelectIdx = 0
        }

        if (canPickPhoto(context)) {
            options.add(getString(R.string.photo_option_gallery))
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }

        return AlertDialog.Builder(context)
            .setTitle(R.string.photo_option_title)
            .setItems(options.toTypedArray<CharSequence>()) { _, which ->
                if (which == captureSelectIdx) {
                    listener.onCaptureClick()
                } else if (which == pickSelectIdx) {
                    listener.onPickClick()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    }

    companion object {
        fun canPickPhoto(context: Context): Boolean {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            return (pickIntent.resolveActivity(context.packageManager) != null)
        }

        fun canCapturePhoto(context: Context): Boolean {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            return (captureIntent.resolveActivity(context.packageManager) != null)
        }

        fun newInstance(context: Context): PhotoOptionDialogFragment? {
            if (canPickPhoto(context) || canCapturePhoto(context)) {
                return PhotoOptionDialogFragment()
            }

            return null
        }
    }
}