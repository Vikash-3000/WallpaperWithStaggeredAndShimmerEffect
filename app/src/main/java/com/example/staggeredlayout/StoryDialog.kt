package com.example.staggeredlayout

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import com.example.staggeredlayout.databinding.DialogStoryBinding

class StoryDialog(
    context: Context,
    private val mediaUrl: String,
    private val isVideo: Boolean,
    private val cachedImage: Bitmap
) {
    private var dialog: Dialog = Dialog(context)
    private val binding: DialogStoryBinding = DialogStoryBinding.inflate(LayoutInflater.from(context))

    fun show() {
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false) // Prevent accidental dismissal

        if (isVideo) {
            binding.imageView.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            playVideo(binding.videoView, mediaUrl)
        } else {

            // ✅ Get screen size & Set ImageView size dynamically
//            val displayMetrics = dialog.context.resources.displayMetrics
//            val size = (displayMetrics.widthPixels * 0.8).toInt() // 80% of screen width
//
//            binding.imageView.layoutParams.width = size
//            binding.imageView.layoutParams.height = size

            binding.videoView.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE


            val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
            val screenHeight = binding.root.context.resources.displayMetrics.heightPixels

// Get bitmap dimensions
            val originalWidth = cachedImage.width
            val originalHeight = cachedImage.height

// Calculate aspect ratio
            val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

// Set max popup size (adjustable)
            val maxPopupWidth = (screenWidth * 0.9).toInt()  // 90% of screen width
            val maxPopupHeight = (screenHeight * 0.7).toInt() // 70% of screen height

// Calculate new dimensions while maintaining aspect ratio
            val newWidth: Int
            val newHeight: Int
            if (aspectRatio > 1) { // Landscape
                newWidth = maxPopupWidth
                newHeight = (maxPopupWidth / aspectRatio).toInt()
            } else { // Portrait or Square
                newHeight = maxPopupHeight
                newWidth = (maxPopupHeight * aspectRatio).toInt()
            }

// ✅ Set ImageView LayoutParams Correctly
            val layoutParams = binding.imageView.layoutParams
            layoutParams.width = newWidth
            layoutParams.height = newHeight

// ✅ Center the Image in CardView
            binding.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.imageView.layoutParams = layoutParams
            binding.imageView.visibility = View.VISIBLE

// ✅ Use Cached Image (No Reload)
            binding.imageView.setImageBitmap(cachedImage)
        }

        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    private fun playVideo(videoView: VideoView, videoUrl: String) {
        videoView.setVideoURI(Uri.parse(videoUrl))
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()
    }
}
