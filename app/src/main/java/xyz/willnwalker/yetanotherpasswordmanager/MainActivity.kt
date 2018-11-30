package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){

    lateinit var prefs : SharedPreferences
    lateinit var nav : NavController
    var firstRun: Boolean = true
    var securityEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // initialize Realm
        Realm.init(applicationContext)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        nav = Navigation.findNavController(this, R.id.nav_host)

    }

    override fun onResume() {
        super.onResume()

        firstRun = prefs.getBoolean("firstRun", true)
        securityEnabled = prefs.getBoolean("securityEnabled", false)
        when{
            firstRun -> nav.navigate(R.id.loginSetupFragment)
            !firstRun && securityEnabled -> nav.navigate(R.id.loginFragment)
            !firstRun && !securityEnabled -> nav.navigate(R.id.passwordListFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!firstRun){
            menuInflater.inflate(R.menu.main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
