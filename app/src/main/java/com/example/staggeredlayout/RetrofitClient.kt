package com.example.staggeredlayout

import WallhavenApi
import WalliApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val walliApi: WalliApi by lazy {
        createRetrofit("https://walli.vercel.app/").create(WalliApi::class.java)
    }

    val wallhavenApi: WallhavenApi by lazy {
        createRetrofit("https://wallhaven.cc/api/v1/").create(WallhavenApi::class.java)
    }
}
