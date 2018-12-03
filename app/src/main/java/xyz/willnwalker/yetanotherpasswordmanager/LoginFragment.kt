package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog


/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : Fragment(){

    private lateinit var contextConfirmed : Context
    private lateinit var prefs: SharedPreferences
    private lateinit var viewConfirmed: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewConfirmed = view
        prefs = PreferenceManager.getDefaultSharedPreferences(contextConfirmed)

    }

    override fun onResume() {
        super.onResume()
        val firstRun = prefs.getBoolean("firstRun", true)
        val securityEnabled = prefs.getBoolean("securityEnabled", false)

        when{
            firstRun -> showSetupDialogs()
            securityEnabled -> showAuthDialogs()
        }

    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(_context: Context){
        super.onAttach(context)
        contextConfirmed = _context
    }

    private fun showSetupDialogs(){
        MaterialDialog.Builder(contextConfirmed)
                .title("Welcome!")
                .content(R.string.setup_login_dialog_message)
                .positiveText("Yes")
                .onPositive { _, _ ->
                    val manager = FingerprintManagerCompat.from(contextConfirmed)
                    when {
                        !manager.isHardwareDetected -> showLaterMessage("No Fingerprint reader detected. ")
                        !manager.hasEnrolledFingerprints() -> showLaterMessage("No Fingerprints saved in phone. ")
                        else -> {
                            val dialog = FingerprintDialog.newInstance(
                                    "Sign In",
                                    "Confirm fingerprint to enable security.")
                            dialog.show(fragmentManager, FingerprintDialog.FRAGMENT_TAG)
                        }
                    }
                }
                .negativeText("No")
                .onNegative{_, _ ->
                    prefs.edit().putBoolean("firstRun",false).apply()
                    prefs.edit().putBoolean("securityEnabled",false).apply()
                    showLaterMessage("")
                }
                .show()
    }

    private fun showAuthDialogs() {
        val dialog = FingerprintDialog.newInstance(
                "Sign In",
                "Confirm fingerprint to continue."
        )
        dialog.show(fragmentManager, FingerprintDialog.FRAGMENT_TAG)
    }

    private fun showLaterMessage(extraMessage : String){
        MaterialDialog.Builder(contextConfirmed)
                .content(extraMessage + getString(R.string.setup_login_dialog_message_negative))
                .positiveText("Okay")
                .onPositive{_, _ ->
                    findNavController().navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
                }
                .show()
    }

}
