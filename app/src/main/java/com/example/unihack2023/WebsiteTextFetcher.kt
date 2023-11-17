package com.example.unihack2023

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import org.jsoup.Jsoup
import org.apache.commons.text.StringEscapeUtils
import java.nio.charset.Charset
import android.util.Log
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist

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
                        val html = responseBody.string()
                        val doc = Jsoup.parse(html)
                        doc.outputSettings(Document.OutputSettings().prettyPrint(false))
                        doc.select("br").before("\\n")
                        doc.select("p").before("\\n")

                        return@withContext doc.text()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return@withContext "" // Return empty string if failed to fetch text
        }
    }

    fun parseLyrics(text: String): String {
        var aux = text
        var start: Int = 0
        var end: Int = 0
        do {
            start = aux.indexOf("[", start)
            if (start != -1) {
                end = aux.indexOf("]", start)
                if (end != -1) {
                    val substr = aux.substring(start, end + 1)
                    aux = aux.replace(substr, "")
                }
            }
        } while (start != -1 && end != -1)
        return aux
    }

}