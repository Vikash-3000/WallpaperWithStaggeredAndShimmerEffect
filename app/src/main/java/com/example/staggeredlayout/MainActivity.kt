package com.example.staggeredlayout

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: Adapter
    private val imageUrls = mutableListOf<WallpaperItem>()

    private var isLoading = false
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
        setupSwipeToRefresh()
        loadImages() // Load first batch of images
    }

    private fun setupRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE // ✅ Prevents shifting gaps
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(25)
        recyclerView.recycledViewPool.setMaxRecycledViews(0, 25)

        adapter = Adapter(this, imageUrls, recyclerView)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading) {
                    val lastVisibleItems = layoutManager.findLastVisibleItemPositions(null)
                    val lastItem = lastVisibleItems.maxOrNull() ?: 0
                    if (lastItem >= imageUrls.size - 5) {
                        loadMoreImages()
                    }
                }
            }
        })
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshImages()
        }
    }

    private fun loadImages() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch {
            try {
                val images = fetchWallpapers()
                updateList(images)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching images: ${e.message}")
            } finally {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadMoreImages() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch {
            try {
                currentPage++
                val newImages = fetchWallpapers()
                updateList(imageUrls + newImages)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading more images: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchWallpapers(): List<WallpaperItem> = withContext(Dispatchers.IO) {
        val imageList = mutableListOf<WallpaperItem>()

        try {
            val response = RetrofitClient.walliApi.getWallpapers("recent", currentPage)
            response.forEach { image ->
                imageList.add(WallpaperItem(image.downloadLinks.original, null, null))
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "API Fetch Failed: ${e.message}")
        }

        imageList.shuffle() // Shuffle images after fetching
        return@withContext imageList
    }

    private fun updateList(newList: List<WallpaperItem>) {
        val diffCallback = WallpaperDiffCallback(imageUrls, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        imageUrls.clear()
        imageUrls.addAll(newList)
        diffResult.dispatchUpdatesTo(adapter)
    }

    private fun refreshImages() {
        swipeRefreshLayout.isRefreshing = true
        currentPage = 1
        loadImages()
    }
}

