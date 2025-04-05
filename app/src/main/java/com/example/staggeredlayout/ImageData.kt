package com.example.staggeredlayout

data class Walli(
    val downloadLinks: DownloadLinks
)

data class DownloadLinks(
    val original: String,
    val thumbnail : String
)

data class WallpaperResponse(
    val data: List<Wallpaper>
)

data class Wallpaper(
    val dimension_x: Int,
    val dimension_y: Int,
    val path: String
)

data class WallpaperItem(
    val imageUrl: String,
    val width: Int?,
    val height: Int?
)


