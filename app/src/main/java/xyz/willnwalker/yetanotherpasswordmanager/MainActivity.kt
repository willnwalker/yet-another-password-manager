package xyz.willnwalker.yetanotherpasswordmanager

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import com.venmo.android.pin.PinFragment
import com.venmo.android.pin.util.PinHelper



class MainActivity : AppCompatActivity(), UIListener, PinFragment.Listener{

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
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        nav = Navigation.findNavController(this, R.id.nav_host)
        firstRun = prefs.getBoolean("firstRun", true)
        securityEnabled = prefs.getBoolean("securityEnabled", false)

    }

    override fun onResume() {
        super.onResume()

        firstRun = prefs.getBoolean("firstRun", true)
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
//        if(!firstRun){
            if(!securityEnabled){
                menu.findItem(R.id.action_settings).title = "Enable Encryption"
            }
            //TODO: Set Settings action to decrypt if security enabled
//        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
//                nav.navigate(R.id.loginFragment)
                val toShow = if (PinHelper.hasDefaultPinSaved(this))
                    PinFragment.newInstanceForVerification()
                else
                    PinFragment.newInstanceForCreation()

                fragmentManager.beginTransaction()
                        .replace(R.id.nav_host, toShow)
                        .commit()
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

    override fun exit(){
        finish()
    }

    override fun onPinCreated() {
        nav.popBackStack()
    }

    override fun onValidated() {
        Toast.makeText(this,"Correct PIN!", Toast.LENGTH_SHORT).show()
        nav.popBackStack()
    }
}
