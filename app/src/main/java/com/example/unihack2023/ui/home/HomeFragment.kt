package com.example.unihack2023.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.unihack2023.databinding.FragmentHomeBinding
import com.google.cloud.translate.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val APIkey:String = "AIzaSyDecAm1Ox2jSa3mqo12HxWl-BlK0_JFzkE"

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
            textView.text = translateText("Hallo, vielen Dank!")
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun translateText(text: String): String {
        try {
            val translate = TranslateOptions.newBuilder().setApiKey(APIkey).build().service
            val detection: Detection = translate.detect(text)
            val detectedLanguage = detection.language
            val translation = translate.translate(
                text,
                TranslateOption.sourceLanguage(detectedLanguage),
                TranslateOption.targetLanguage("en")
            )
            return translation.translatedText
        } catch (e: Exception) {
            // Log the exception to help identify the issue
            e.printStackTrace()
            return "Translation failed"
        }
    }
}