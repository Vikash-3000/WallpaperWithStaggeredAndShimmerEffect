package com.example.staggeredlayout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import kotlin.random.Random

class Adapter(
    private val context: Context,
    private val wallpapers: MutableList<WallpaperItem>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<Adapter.WallpaperViewHolder>() {

    private val longPressHandler = Handler()
    private var storyDialog: StoryDialog? = null
    private var isPopupOpen = false
    private val placeholderColors = listOf(
        // 🌟 Vibrant Colors
        Color.parseColor("#FF5733"), // Bright Red-Orange
        Color.parseColor("#D32F2F"), // Deep Red
        Color.parseColor("#4CAF50"), // Fresh Green
        Color.parseColor("#2196F3"), // Vivid Blue
        Color.parseColor("#9C27B0"), // Rich Purple
        Color.parseColor("#FFC107"), // Bright Yellow
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#00BCD4"), // Cyan

        // 🎨 Pastel Colors
        Color.parseColor("#FFB6C1"), // Light Pink
        Color.parseColor("#FFDAB9"), // Peach
        Color.parseColor("#FFE4B5"), // Moccasin
        Color.parseColor("#98FB98"), // Pale Green
        Color.parseColor("#87CEEB"), // Light Sky Blue
        Color.parseColor("#DDA0DD"), // Plum
        Color.parseColor("#F0E68C"), // Khaki
        Color.parseColor("#B0E0E6"), // Powder Blue

        // 🌙 Dark & Elegant Colors
        Color.parseColor("#212121"), // Deep Charcoal
        Color.parseColor("#37474F"), // Blue Gray
        Color.parseColor("#455A64"), // Steel Gray
        Color.parseColor("#1B5E20"), // Dark Green
        Color.parseColor("#311B92"), // Deep Indigo
        Color.parseColor("#B71C1C"), // Dark Red
        Color.parseColor("#4A148C"), // Dark Purple
        Color.parseColor("#263238"), // Graphite

        // 🕹️ Neon & Futuristic Colors
        Color.parseColor("#FF1744"), // Neon Red
        Color.parseColor("#D500F9"), // Neon Purple
        Color.parseColor("#2979FF"), // Electric Blue
        Color.parseColor("#00E5FF"), // Cyan Glow
        Color.parseColor("#76FF03"), // Laser Green
        Color.parseColor("#FFD600"), // Bright Gold
        Color.parseColor("#FF6D00"), // Lava Orange
        Color.parseColor("#6200EA")  // Deep Violet
    )

    private fun getShimmerDrawable(): ShimmerDrawable {
        val shimmer = Shimmer.ColorHighlightBuilder()
            .setBaseColor(Color.parseColor("#DDDDDD"))
            .setHighlightColor(Color.parseColor("#EEEEEE"))
            .setDuration(2000)                                // ⏳ Slower shimmer (higher = slower)
            .setBaseAlpha(1.0f)                               // ✅ Fully opaque base
            .setHighlightAlpha(1.0f)                          // ✅ Fully opaque highlight
            .setWidthRatio(1.5f)                              // 🌊 Wider shimmer band
            .setDropoff(3.0f)                                 // 🌈 Controls gradient width (lower = wider highlight)
            .setAutoStart(true)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .build()


        return ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
    }


    class WallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return WallpaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        val wallpaper = wallpapers[position]
        val randomHeight = Random.nextInt(400, 1200) // Dynamic height

        holder.imageView.layoutParams.height = randomHeight

        val shimmerDrawable = getShimmerDrawable()

        // ✅ Glide loads image with shimmer
        Glide.with(holder.itemView)
            .load(wallpaper.imageUrl)
            .placeholder(shimmerDrawable) // Shimmer effect while loading
//            .placeholder(ColorDrawable(getRandomColor()))
            .error(ColorDrawable(getRandomColor())) // Error fallback color
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache images for performance
            .into(holder.imageView)


        // ✅ Preload Image for Popup
        Glide.with(context)
            .asBitmap()
            .load(wallpaper.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.imageView.setTag(R.id.imageView, resource) // Save Bitmap for popup
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        // ✅ Long Press to Show Popup
        holder.imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressHandler.postDelayed({
                        if (!isPopupOpen) {
                            isPopupOpen = true
                            recyclerView.requestDisallowInterceptTouchEvent(true)

                            val cachedBitmap = holder.imageView.getTag(R.id.imageView) as? Bitmap
                            cachedBitmap?.let {
                                storyDialog = StoryDialog(context, wallpaper.imageUrl, false, it)
                                storyDialog?.show()
                            }
                        }
                    }, 500)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longPressHandler.removeCallbacksAndMessages(null)
                    isPopupOpen = false
                    recyclerView.requestDisallowInterceptTouchEvent(false)

                    storyDialog?.dismiss()
                    storyDialog = null
                }
            }
            true
        }
    }

    private fun getRandomColor() : Int {
        return placeholderColors.random()
    }

    override fun getItemCount(): Int = wallpapers.size
}