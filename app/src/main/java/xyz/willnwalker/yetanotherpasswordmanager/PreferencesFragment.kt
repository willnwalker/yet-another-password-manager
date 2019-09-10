package xyz.willnwalker.yetanotherpasswordmanager


import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        Snackbar.make(requireView(), "$p1: ${p0!!.getBoolean(p1, false)}", Snackbar.LENGTH_LONG).show()
        when(p1){
            "securityEnabled" ->{

            }
            "fingerprintEnabled" ->{

            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
