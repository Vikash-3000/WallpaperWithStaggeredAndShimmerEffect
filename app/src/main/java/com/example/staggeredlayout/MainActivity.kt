package com.example.staggeredlayout

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: Adapter
    private val imageUrls = mutableListOf<WallpaperItem>()

    private var isLoading = false

    private var currentPageWalli = 1
    private var currentPageFeature = 1
    private var currentPagePopular = 1
    private var currentPageWallhaven = 1

    private val apiKey = "HSsQUq456pp0SZejuuR17dkAAmdJ9Vi3"

    private val lastPages = mapOf(
        "recent" to 194,
        "featured" to 10,
        "popular" to 1240,
        "wallhaven" to 8333
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
        setupSwipeToRefresh()
        loadImages()
    }

    private fun setupRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
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
                imageUrls.clear()
                imageUrls.addAll(images)
                adapter.notifyDataSetChanged()
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
                incrementPages()
                val newImages = fetchWallpapers()
                val oldSize = imageUrls.size
                imageUrls.addAll(newImages)
                adapter.notifyItemRangeInserted(oldSize, newImages.size)
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
            // Fetching "recent", "featured", and "popular" pages using current page trackers
            val recentResponse = RetrofitClient.walliApi.getWallpapers("recent", currentPageWalli)
            val featureResponse = RetrofitClient.walliApi.getWallpapers("feature", currentPageFeature)
            val popularResponse = RetrofitClient.walliApi.getWallpapers("popular", currentPagePopular)

            recentResponse.forEach { image ->
                imageList.add(WallpaperItem(image.downloadLinks.original, null, null))
            }
            featureResponse.forEach { image ->
                imageList.add(WallpaperItem(image.downloadLinks.original, null, null))
            }
            popularResponse.forEach { image ->
                imageList.add(WallpaperItem(image.downloadLinks.original, null, null))
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "API Fetch Failed: ${e.message}")
        }

        imageList.shuffle() // Shuffle images after fetching
        return@withContext imageList
    }


    private fun refreshImages() {
        swipeRefreshLayout.isRefreshing = true
        currentPageWalli = 1
        currentPageFeature = 1
        currentPagePopular = 1
        currentPageWallhaven = 1
        loadImages()
    }

    private fun incrementPages() {
        currentPageWalli = (currentPageWalli % lastPages["recent"]!!) + 1
        currentPageFeature = (currentPageFeature % lastPages["featured"]!!) + 1
        currentPagePopular = (currentPagePopular % lastPages["popular"]!!) + 1
        currentPageWallhaven = (currentPageWallhaven % lastPages["wallhaven"]!!) + 1
    }
}
