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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val APIkey:String = "AIzaSyC6OOmcv32-NvpVqWm_6QXkwNflZu5HDN0"

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
            translateText("Hallo, vielen Dank!", textView)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun translateText(text: String, textView: TextView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val translate = TranslateOptions.newBuilder().setApiKey(APIkey).build().service
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

}