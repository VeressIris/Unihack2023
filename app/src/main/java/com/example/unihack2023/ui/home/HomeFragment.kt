package com.example.unihack2023.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.unihack2023.databinding.FragmentHomeBinding
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.unihack2023.LyricsSearchManager
import com.example.unihack2023.WebsiteTextFetcher
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private val GoogleAPIkey:String = "AIzaSyC6OOmcv32-NvpVqWm_6QXkwNflZu5HDN0"
    private val GeniusAPIkey:String = "G5eF63EO4TJaDTCDc_FqsUvQF8A9u6l_Ob9F3G-GIu7J2x6BojRJjplN2hPfNacA"

    var songName:String = "wish you were gay"
    val lyricsSearchManager = LyricsSearchManager()

    private val networkScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            translateText("Hallo, Welt!", textView)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkScope.launch {
            searchLyrics()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        super.onDestroy()
        networkScope.cancel()
        _binding = null
    }

    private fun translateText(text: String, textView: TextView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val translate = TranslateOptions.newBuilder().setApiKey(GoogleAPIkey).build().service
                val detection: Detection = translate.detect(text)
                val detectedLanguage = detection.language
                val translation = translate.translate(
                    text,
                    TranslateOption.sourceLanguage(detectedLanguage),
                    TranslateOption.targetLanguage("en")
                )

                // Update the UI on the Main thread
                withContext(Dispatchers.Main) {
                    textView.text = translation.translatedText
                }
            } catch (e: Exception) {
                // Log the exception to help identify the issue
                e.printStackTrace()
                Log.e("Error", e.toString())

                // Update the UI on the Main thread with error message
                withContext(Dispatchers.Main) {
                    textView.text = "Translation failed"
                }
            }
        }
    }

    private suspend fun searchLyrics() {
        withContext(Dispatchers.Default) {
            lyricsSearchManager.searchLyrics(songName, GeniusAPIkey) { results ->
                if (results != null) {
                    Log.i("LyricResult", results[0].url)
                    CoroutineScope(Dispatchers.IO).launch {
                        val websiteTextFetcher = WebsiteTextFetcher()
                        val websiteText = getLyricsFromSite(websiteTextFetcher.fetchTextFromUrl(results[0].url))
                        Log.i("TEXT", websiteText)

                        // Handle fetched text here or update UI
                    }


                } else {
                    // Handle error or no results
                    Log.e("LyricsSearch", "Failed to fetch lyrics")
                }
            }
        }
    }

    fun getLyricsFromSite(text: String):String {
        val start: Int = text.indexOf('[')
        val end: Int = text.indexOf("Embed")

        var str = text.substring(start, end)

        var i: Int = str.length - 1
        while (text[i] != ' ') {
            i--
        }

        return str.substring(0, i - 1)
    }

}