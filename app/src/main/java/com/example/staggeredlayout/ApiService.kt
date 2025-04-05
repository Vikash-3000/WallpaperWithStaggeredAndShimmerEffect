import com.example.staggeredlayout.Walli
import com.example.staggeredlayout.WallpaperResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Walli API Interface
interface WalliApi {
    @GET("wallpaper")
    suspend fun getWallpapers(
        @Query("type") type: String,
        @Query("page") page: Int
    ): List<Walli>
}

// Wallhaven API Interface
interface WallhavenApi {
    @GET("search")
    suspend fun searchWallpapers(
        @Query("apikey") apiKey: String,
        @Query("page") page: Int
    ): WallpaperResponse
}
