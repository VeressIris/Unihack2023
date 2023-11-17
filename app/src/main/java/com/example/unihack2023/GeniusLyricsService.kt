package com.example.unihack2023
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GeniusLyricsService {
    @GET("search")
    fun searchLyrics(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): Call<GeniusSearchResponse>
}

data class GeniusSearchResponse(val response: GeniusResponse)
data class GeniusResponse(val hits: List<GeniusHit>)
data class GeniusHit(val result: GeniusResult)
data class GeniusResult(val title: String, val url: String)