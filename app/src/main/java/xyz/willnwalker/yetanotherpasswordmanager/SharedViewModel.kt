package xyz.willnwalker.yetanotherpasswordmanager

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class BaseViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return creator() as T
    }
}

class LiveSharedPreferences(private val prefs: SharedPreferences, private val requestedKey: String, private val initialValue: Boolean): LiveData<Boolean>(){

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener {
        prefs, prefKey ->
        if(prefKey == requestedKey){
            value = prefs.getBoolean(requestedKey, initialValue)
        }
    }

    override fun onActive() {
        super.onActive()
        value = prefs.getBoolean(requestedKey, initialValue)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onInactive() {
        super.onInactive()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

}

class SharedViewModel(prefs: SharedPreferences): ViewModel(){

    val firstRun = LiveSharedPreferences(prefs, "firstRun", true)
    val securityEnabled = LiveSharedPreferences(prefs, "securityEnabled", false)
    val fingerprintEnabled = LiveSharedPreferences(prefs, "fingerprintEnabled", false)

    val pinCreated = MutableLiveData<Boolean>(false)
    val pinValidated = MutableLiveData<Boolean>(false)
    val resumedLogin = MutableLiveData<Boolean>(false)

}