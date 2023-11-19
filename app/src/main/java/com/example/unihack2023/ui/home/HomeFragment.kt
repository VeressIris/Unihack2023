package com.example.unihack2023.ui.home

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.unihack2023.LyricsSearchManager
import com.example.unihack2023.R
import com.example.unihack2023.WebsiteTextFetcher
import com.example.unihack2023.databinding.FragmentHomeBinding
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.*
import com.example.unihack2023.MainActivity
import android.text.style.ClickableSpan
import android.text.Spannable
import com.google.cloud.translate.Translate
import android.text.method.LinkMovementMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val GoogleAPIkey: String = "AIzaSyC6OOmcv32-NvpVqWm_6QXkwNflZu5HDN0"
    private val GeniusAPIkey: String = "G5eF63EO4TJaDTCDc_FqsUvQF8A9u6l_Ob9F3G-GIu7J2x6BojRJjplN2hPfNacA"

    val lyricsSearchManager = LyricsSearchManager()
    private val networkScope = CoroutineScope(Dispatchers.IO)

    val mainActivity = MainActivity()

    var mainTextUpdateCallBack: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    var startIndexOfClickedWord: Int = -1
    var endIndexOfClickedWord: Int = -1
    fun makeWordsClickable(textView: TextView) {
        val spannable = Spannable.Factory.getInstance().newSpannable(textView.text)
        val text = textView.text.toString()
        val wordPattern = "\\b\\w+\\b".toRegex() // Regex to match words

        val matches = wordPattern.findAll(text)
        matches.forEach { matchResult ->
            val word = matchResult.value
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Log.d("ClickedWord", word)
                    startIndexOfClickedWord = start
                    endIndexOfClickedWord = end
                    clickOnWord(word)
                }
            }
            spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.isVerticalScrollBarEnabled = true
    }


    public var mainText: TextView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainText = view.findViewById(R.id.text_home)

        mainText!!.movementMethod = ScrollingMovementMethod()

        mainTextUpdateCallBack?.invoke("init text")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        super.onDestroy()
        networkScope.cancel()
        _binding = null
    }

    public suspend fun translateText(text: String?, targetLang:String): String {
        return withContext(Dispatchers.IO) {
            try {
                val translate =
                    TranslateOptions.newBuilder().setApiKey(GoogleAPIkey).build().service
                val detection: Detection = translate.detect(text)
                val detectedLanguage = detection.language
                val translation = translate.translate(
                    text,
                    TranslateOption.sourceLanguage(detectedLanguage),
                    TranslateOption.targetLanguage(targetLang)
                )
                translation.translatedText
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Error", e.toString())
                "Translation failed"
            }
        }
    }

    public suspend fun searchLyrics(songName:String): String {
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

    fun clickOnWord(word: String) {
        var translation:String? = null
        networkScope.launch {
            translation = translateWord(word, "en")

            Log.d("TranslatedWord", translation.toString())

            // Or update UI with the translated word
            withContext(Dispatchers.Main) {
                mainText?.text = replaceClickedWord(word, translation!!, mainText?.text.toString())
            }
        }
    }

    fun replaceClickedWord(clickedWord: String, translation: String, text:String):String {
        val before = text.substring(0, startIndexOfClickedWord)
        val after = text.substring(endIndexOfClickedWord)

        return before + translation + after
    }

    public fun replaceSymbolsInLyrics(songName:String, targetLang: String) {
        Log.e("mainText", "i have been called " + mainText.toString())
        networkScope.launch {
            val lyrics = translateText(searchLyrics(songName), targetLang)

            withContext(Dispatchers.IO) {
                mainText?.text = lyrics.replace("&#39;", "'").replace("&quot;", "\"").replace("\\n", "\n").replace("\\ n", "\n")

                mainText?.post { makeWordsClickable(mainText!!) }
            }
        }
    }

    public suspend fun translateWord(word: String, targetLang: String): String {
        val deferred = CompletableDeferred<String>()

        networkScope.launch {
            val translatedWord = translateText(word, targetLang)
            deferred.complete(translatedWord)
        }

        return deferred.await()
    }

    fun setMainTextUpdateCallback(callback: (String) -> Unit) {
        mainTextUpdateCallBack = callback
    }


}