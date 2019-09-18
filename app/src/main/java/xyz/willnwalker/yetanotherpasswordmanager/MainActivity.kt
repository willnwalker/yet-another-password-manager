package xyz.willnwalker.yetanotherpasswordmanager

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import com.venmo.android.pin.PinListener


class MainActivity : AppCompatActivity(), UIListener, PinListener{

    private lateinit var viewModel: SharedViewModel
    private lateinit var prefs : SharedPreferences
    private lateinit var nav : NavController
    private lateinit var realmConfig: RealmConfiguration
    private var firstRun: Boolean = true
    private var securityEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // initialize Realm
        Realm.init(applicationContext)
//        viewModel = ViewModelProviders.of(this)[SharedViewModel::class.java]
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel = getViewModel{ SharedViewModel(prefs) }
        nav = Navigation.findNavController(this, R.id.nav_host)
        firstRun = prefs.getBoolean("firstRun", true)
        securityEnabled = prefs.getBoolean("securityEnabled", false)

    }

    override fun onResume() {
        super.onResume()

        firstRun = prefs.getBoolean("firstRun", true)
        Toast.makeText(this, "firstRun: $firstRun", Toast.LENGTH_SHORT).show()
//        prefs.edit().putBoolean("firstRun", false).apply()
        securityEnabled = prefs.getBoolean("securityEnabled", false)

//        nav.navigate(R.id.loadingFragment)
//
//        when{
//            firstRun || securityEnabled -> nav.navigate(R.id.loginFragment)
//            else -> nav.navigate(R.id.passwordListFragment)
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                nav.navigate(R.id.settingsFragment)
//                val pinFragment = if (PinHelper.hasDefaultPinSaved(this))
//                    PinSupportFragment.newInstanceForVerification()
//                else
//                    PinSupportFragment.newInstanceForCreation()
//
//                supportFragmentManager.beginTransaction()
////                        .remove(nav_host)
//                        .replace(R.id.nav_host, pinFragment)
//                        .addToBackStack(null)
//                        .commit()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun setRealmConfig(realmConfig: RealmConfiguration) {
        this.realmConfig = realmConfig
    }

    override fun getRealmConfig(): RealmConfiguration {
        return realmConfig
    }

    override fun onPinCreated() {
        nav.navigateUp()
    }

    override fun onValidated() {
        Toast.makeText(this,"Correct PIN!", Toast.LENGTH_SHORT).show()
        nav.navigateUp()
    }
}
