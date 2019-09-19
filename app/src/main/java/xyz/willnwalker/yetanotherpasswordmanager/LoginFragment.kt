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
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.venmo.android.pin.PinListener
import io.realm.RealmConfiguration


/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : Fragment(), PinListener{

    private lateinit var uiListener: UIListener
    private lateinit var prefs: SharedPreferences
    private lateinit var nav: NavController

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
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
    override fun onAttach(context: Context){
        super.onAttach(context)
        uiListener = requireContext() as UIListener
    }

    private fun showSetupFlow(flow: String){
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            title(text = "Welcome!")
            message(R.string.setup_login_dialog_message)
            positiveButton(text = "Yes"){
                val manager = FingerprintManagerCompat.from(requireContext())
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
                        dialog.show(requireFragmentManager(), FingerprintDialog.FRAGMENT_TAG)
                    }
                }
            }
            negativeButton(text = "No"){
                prefs.edit().putBoolean("firstRun",false).apply()
                prefs.edit().putBoolean("securityEnabled",false).apply()
                uiListener.setRealmConfig(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
                showLaterMessage("")
            }
        }
    }

    private fun showAuthFlow() {
        val dialog = FingerprintDialog.newInstance(
                "Sign In",
                "Confirm fingerprint to continue.",
                "auth",
                nav
        )
        dialog.show(requireFragmentManager(), FingerprintDialog.FRAGMENT_TAG)
    }

    private fun showLaterMessage(extraMessage : String){
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            message(text = "$extraMessage You can always secure your passwords later. Just go to ...")
            positiveButton(text = "Okay")
            onDismiss {
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
            }
        }
    }

    override fun onPinCreated() {
        nav.navigateUp()
    }

    override fun onValidated() {
        Toast.makeText(requireActivity(),"Correct PIN!", Toast.LENGTH_SHORT).show()
        nav.navigateUp()
    }

}
