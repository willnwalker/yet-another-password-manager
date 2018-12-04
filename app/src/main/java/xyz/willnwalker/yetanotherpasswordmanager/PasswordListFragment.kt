package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_password_list.*


/**
 * A simple [Fragment] subclass.
 *
 */
class PasswordListFragment : Fragment() {

    private lateinit var contextConfirmed : Context
    private lateinit var prefs: SharedPreferences
    private var firstRun = true
    private var securityEnabled = false
    private lateinit var uiListener: UIListener
    private lateinit var realmConfig: RealmConfiguration

    // Kyle: initialize linearLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            findNavController(it).navigate(R.id.action_new_password)
        }

        linearLayoutManager = LinearLayoutManager(activity)

        // Kyle: adds a horizontal line separator between each item
        passwordList.addItemDecoration(PasswordListItemDecoration(contextConfirmed, 40, 40))

//        val config: RealmConfiguration = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        val realm = Realm.getInstance(realmConfig)
        val entries = realm.where<Entry>().findAllAsync()
        passwordList.setAdapter(PasswordListAdapter(realmConfig, contextConfirmed, entries, true, false, ""))

    }

    override fun onResume() {
        super.onResume()
//        uiListener.toggleOptionsMenu()
    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(_context: Context){
        super.onAttach(context)
        contextConfirmed = _context
        uiListener = context as UIListener
        prefs = PreferenceManager.getDefaultSharedPreferences(contextConfirmed)
        firstRun = prefs.getBoolean("firstRun", true)
        securityEnabled = prefs.getBoolean("securityEnabled", false)
        when{
            !securityEnabled -> {
                realmConfig = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
                uiListener.setRealmConfig(realmConfig)
            }
            else -> {
                val realmKey = Base64.decode(prefs.getString("RealmKey",""), Base64.NO_WRAP)
                realmConfig = RealmConfiguration.Builder().encryptionKey(realmKey).deleteRealmIfMigrationNeeded().build()
                uiListener.setRealmConfig(realmConfig)

            }
        }
        this.realmConfig = uiListener.getRealmConfig()
    }

}
