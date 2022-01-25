package xyz.willnwalker.yetanotherpasswordmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.preference.PreferenceManager
import com.github.tntkhang.realmencryptionhelper.RealmEncryptionHelper
import com.venmo.android.pin.PinListener
import com.venmo.android.pin.util.PinHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), PinListener{

    private lateinit var viewModel: SharedViewModel
    private lateinit var nav : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val reh = RealmEncryptionHelper.initHelper(this, getString(R.string.app_name))
        val config = RealmConfiguration.Builder()
                .name("realm_encrypt.realm")
                .encryptionKey(reh.encryptKey)
                .build()
        Realm.setDefaultConfiguration(config)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel = getViewModel { SharedViewModel(prefs) }

//        nav = findNavController(this, R.id.nav_host)

    }

    override fun onResume() {
        nav = findNavController(this, R.id.nav_host)
        super.onResume()
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
        return when (item.itemId) {
            R.id.action_settings -> {
                nav.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        if(PinHelper.hasDefaultPinSaved(this)&&viewModel.securityEnabled.value!!){
            viewModel.resumedLogin.value = true
            nav.navigate(R.id.loginFragment)
        }
    }

    override fun onPinCreated() {
        viewModel.pinCreated.value = true
    }

    override fun onValidated() {
        viewModel.pinValidated.value = true
    }
}
