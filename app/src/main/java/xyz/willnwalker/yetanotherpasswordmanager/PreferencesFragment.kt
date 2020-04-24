package xyz.willnwalker.yetanotherpasswordmanager


import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.venmo.android.pin.util.PinHelper
import java.util.concurrent.Executors


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var prefs: SharedPreferences
    private lateinit var nav: NavController
    private val listener = OnSharedPreferenceChangeListener{ prefs, key ->
        when(key){
            "securityEnabled" -> {
                if(prefs.getBoolean("securityEnabled",false)){
                    nav.navigate(R.id.action_settingsFragment_to_loginFragment)
                }
                else{
                    // De-register saved PIN
                    PinHelper.resetDefaultSavedPin(requireActivity())
                }
            }
            "fingerprintEnabled" -> {
                if(prefs.getBoolean("fingerprintEnabled",false)){
                    val executor = Executors.newSingleThreadExecutor()
                    val callback = object: BiometricPrompt.AuthenticationCallback() {
                        fun disableFingerprint(){
                            prefs.edit().putBoolean("fingerprintEnabled",false).apply()
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)

                            if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                                Snackbar.make(requireView(), "User clicked negative button.", Snackbar.LENGTH_LONG)
                                        .show()
                            }
                            disableFingerprint()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            Snackbar.make(requireView(), "Fingerprint setup successful.", Snackbar.LENGTH_LONG)
                                    .show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            disableFingerprint()
                        }
                    }
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Fingerprint Setup")
                            .setSubtitle("This will let you use your fingerprint to unlock your saved passwords.")
                            .setDescription("Click cancel to use your backup PIN.")
                            .setNegativeButtonText("Cancel")
                            .build()
                    val biometricPrompt = BiometricPrompt(this, executor, callback)
                    biometricPrompt.authenticate(promptInfo)
                }
                else{
                    // De-register saved fingerprint

                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nav = findNavController()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
