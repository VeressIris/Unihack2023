package com.example.unihack2023

import android.content.Context
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.unihack2023.databinding.ActivityMainBinding
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import android.view.View
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import android.widget.Spinner
import android.widget.AdapterView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import com.example.unihack2023.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    var selectedItem:String? = null
    public var langCode:String? = null

    private val networkScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val searchBttn = toolbar.findViewById<Button>(R.id.search_bttn)
        val targetLanguage = toolbar.findViewById<Spinner>(R.id.languageSelector)

        targetLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedItem = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        }

        val homeFrag:HomeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.drawer_layout, homeFrag).commit()

        //Search for song:
        searchBttn?.setOnClickListener {
            val songInput = toolbar.findViewById<TextInputEditText>(R.id.searchSong_txtBox)
            langCode = getLanguageCode()

            closeKeyboard(songInput)

            homeFrag.replaceSymbolsInLyrics(songInput.text.toString(), langCode.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun getLanguageCode():String {
        val first = selectedItem.toString().indexOf("(")
        val last = selectedItem.toString().indexOf(")")
        return selectedItem.toString().substring(first + 1, last)
    }

    fun closeKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}