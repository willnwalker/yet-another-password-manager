package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.RealmConfiguration


/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : androidx.fragment.app.Fragment(){

    private lateinit var uiListener: UIListener
    private lateinit var contextConfirmed : Context
    private lateinit var prefs: SharedPreferences
    private lateinit var viewConfirmed: View
    private lateinit var nav: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewConfirmed = view
        prefs = PreferenceManager.getDefaultSharedPreferences(contextConfirmed)
        nav = findNavController()

    }

    override fun onResume() {
        super.onResume()
        val firstRun = prefs.getBoolean("firstRun", true)
        val securityEnabled = prefs.getBoolean("securityEnabled", false)

        when{
            firstRun -> showSetupFlow("setup")
            securityEnabled -> showAuthFlow()
            !firstRun && !securityEnabled -> {
                showSetupFlow("migrate")
            }
        }

    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(_context: Context){
        super.onAttach(context)
        contextConfirmed = _context
        uiListener = contextConfirmed as UIListener
    }

    private fun showSetupFlow(flow: String){
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
                                    "Confirm fingerprint to enable security.",
                                    flow,
                                    nav
                            )
                            dialog.show(fragmentManager, FingerprintDialog.FRAGMENT_TAG)
                        }
                    }
                }
                .negativeText("No")
                .onNegative{_, _ ->
                    prefs.edit().putBoolean("firstRun",false).apply()
                    prefs.edit().putBoolean("securityEnabled",false).apply()
                    uiListener.setRealmConfig(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
                    showLaterMessage("")
                }
                .show()
    }

    private fun showAuthFlow() {
        val dialog = FingerprintDialog.newInstance(
                "Sign In",
                "Confirm fingerprint to continue.",
                "auth",
                nav
        )
        dialog.show(fragmentManager, FingerprintDialog.FRAGMENT_TAG)
    }

    private fun showLaterMessage(extraMessage : String){
        MaterialDialog.Builder(contextConfirmed)
                .content(extraMessage + getString(R.string.setup_login_dialog_message_negative))
                .positiveText("Okay")
                .onPositive{_, _ ->
                    nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
                }
                .show()
    }

}
