package xyz.willnwalker.yetanotherpasswordmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), UIListener{

    private lateinit var nav : NavController
    private lateinit var realmConfig: RealmConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        nav = findNavController(this, R.id.nav_host)

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

    override fun setRealmConfig(realmConfig: RealmConfiguration) {
        this.realmConfig = realmConfig
    }

    override fun getRealmConfig(): RealmConfiguration {
        return realmConfig
    }
}
