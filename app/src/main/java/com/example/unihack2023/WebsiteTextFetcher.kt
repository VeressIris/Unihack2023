package com.example.unihack2023
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import org.jsoup.Jsoup

class WebsiteTextFetcher {
    suspend fun fetchTextFromUrl(url: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body
                    if (responseBody != null) {
                        // Read the response body as text
                        val html = responseBody.string()
                        // Parse HTML using Jsoup to extract visible text
                        val doc = Jsoup.parse(html)
                        val text = doc.text()
                        return@withContext text
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return@withContext "" // Return empty string if failed to fetch text
        }
    }
}