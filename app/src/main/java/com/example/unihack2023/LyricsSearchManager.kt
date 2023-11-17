package com.example.unihack2023

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

const val GENIUS_BASE_URL = "https://api.genius.com/"

public class LyricsSearchManager {
    private val retrofit = Retrofit.Builder()
        .baseUrl(GENIUS_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GeniusLyricsService::class.java)

    fun searchLyrics(query: String, apiKey: String, callback: (List<GeniusResult>?) -> Unit) {
        service.searchLyrics("Bearer $apiKey", query).enqueue(object : Callback<GeniusSearchResponse> {
            override fun onResponse(call: Call<GeniusSearchResponse>, response: Response<GeniusSearchResponse>) {
                if (response.isSuccessful) {
                    val hits = response.body()?.response?.hits?.map { it.result }
                    callback(hits)
                    Log.i("LyricsSUCCESS", "SUCCESS")
                } else {
                    callback(null)
                    Log.e("LyricsERROR", "ERROR")
                }
            }

            override fun onFailure(call: Call<GeniusSearchResponse>, t: Throwable) {
                callback(null)
                Log.e("LyricsERROR", "ERROR")
            }
        })
    }
}
