package com.example.unihack2023.ui.home

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.unihack2023.LyricsSearchManager
import com.example.unihack2023.R
import com.example.unihack2023.WebsiteTextFetcher
import com.example.unihack2023.databinding.AppBarMainBinding
import com.example.unihack2023.databinding.FragmentHomeBinding
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.*
import androidx.appcompat.widget.Toolbar
import com.example.unihack2023.MainActivity
import android.text.style.ClickableSpan
import android.text.Spannable
import com.google.cloud.translate.Translate
import com.google.cloud.translate.Translation
import android.text.method.LinkMovementMethod


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val GoogleAPIkey: String = "AIzaSyC6OOmcv32-NvpVqWm_6QXkwNflZu5HDN0"
    private val GeniusAPIkey: String = "G5eF63EO4TJaDTCDc_FqsUvQF8A9u6l_Ob9F3G-GIu7J2x6BojRJjplN2hPfNacA"

    val lyricsSearchManager = LyricsSearchManager()
    private val networkScope = CoroutineScope(Dispatchers.IO)

    val songName:String = "Cro Easy"

    val mainActivity = MainActivity()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    fun splitTextIntoWords(text: String): List<String> {
        return text.split("[\\s]+".toRegex())
    }

    fun makeWordsClickable(textView: TextView) {
        val spannable = Spannable.Factory.getInstance().newSpannable(textView.text)
        val words = splitTextIntoWords(textView.text.toString())

        var start = 0
        words.forEach { word ->
            val end = start + word.length
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Handle word click here
                    Log.d("ClickedWord", word)
                    clickOnWord(word)
                }
            }
            spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            start = end + 1  // Move start to the next word
        }
        textView.text = spannable

        // Enable clickable spans within the TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
    }


    var mainText: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainText: TextView = view.findViewById(R.id.text_home)

        networkScope.launch {
            val lyrics = translateText(searchLyrics())
            withContext(Dispatchers.Main) {
                mainText.text =
                    lyrics.replace("&#39;", "'").replace("&quot;", "\"").replace("\\n", "\n")
                        .replace("\\ n", "\n")

                mainText.post { makeWordsClickable(mainText) }
            }
        }

        mainText.movementMethod = ScrollingMovementMethod()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        super.onDestroy()
        networkScope.cancel()
        _binding = null
    }

    private suspend fun translateText(text: String?): String {
        return withContext(Dispatchers.IO) {
            try {
                val translate =
                    TranslateOptions.newBuilder().setApiKey(GoogleAPIkey).build().service
                val detection: Detection = translate.detect(text)
                val detectedLanguage = detection.language
                val translation = translate.translate(
                    text,
                    TranslateOption.sourceLanguage(detectedLanguage),
                    TranslateOption.targetLanguage("en")
                )
                translation.translatedText
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Error", e.toString())
                "Translation failed"
            }
        }
    }

    private suspend fun searchLyrics(): String {
        val deferred = CompletableDeferred<String>()

        lyricsSearchManager.searchLyrics(songName, GeniusAPIkey) { results ->
            if (!results.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val websiteTextFetcher = WebsiteTextFetcher()
                    val websiteText = websiteTextFetcher.parseLyrics(
                        getLyricsFromSite(
                            websiteTextFetcher.fetchTextFromUrl(results[0].url)
                        )
                    )
                    deferred.complete(websiteText)
                }
            } else {
                Log.e("LyricsSearch", "Failed to fetch lyrics")
                deferred.complete("Failed to fetch Lyrics")
            }
        }

        return deferred.await()
    }

    private fun getLyricsFromSite(text: String): String {
        val start: Int = text.indexOf('[')
        val end: Int = text.indexOf("Embed")

        val str = text.substring(start, end)

        var i: Int = str.length - 1
        while (text[i] != ' ') {
            i--
        }

        return str.substring(0, i - 1)
    }


    // Then, you can use it in your clickOnWord function
    fun clickOnWord(word: String) {
        val translate = TranslateOptions.newBuilder().setApiKey("AIzaSyC6OOmcv32-NvpVqWm_6QXkwNflZu5HDN0").build().service

        networkScope.launch {
            try {
                val translation = translate.translate(
                    word,
                    Translate.TranslateOption.targetLanguage("en")
                )
                val translatedWord = translation.translatedText

                withContext(Dispatchers.Main) {
                    // Update the TextView with the translated word if it's not null
                    mainText?.append("$word -> $translatedWord\n")
                }
            } catch (e: Exception) {
                Log.e("TranslationError", e.message ?: "Translation failed")
            }
        }
    }


}