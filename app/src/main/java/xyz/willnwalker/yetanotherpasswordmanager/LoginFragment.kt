package xyz.willnwalker.yetanotherpasswordmanager

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.venmo.android.pin.PinSupportFragment
import com.venmo.android.pin.util.PinHelper
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : Fragment(){

    private lateinit var nav: NavController
    private lateinit var viewModel: SharedViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("xyz.willnwalker.yapm","LoginFragment created.")

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        viewModel = requireActivity().getViewModel { SharedViewModel(prefs) }
        nav = findNavController()
        viewModel.pinCreated.observe(viewLifecycleOwner, Observer { pinCreated ->
            if(pinCreated){
                Log.d("xyz.willnwalker.yapm","pinCreated is true")
                viewModel.pinCreated.value = false
                nav.navigateUp()
            }
        })
        viewModel.pinValidated.observe(viewLifecycleOwner, Observer { pinValidated ->
            if(pinValidated){
                Log.d("xyz.willnwalker.yapm","pinValidated is true")
                Toast.makeText(requireActivity(),"Correct PIN!", Toast.LENGTH_SHORT).show()
                viewModel.pinValidated.value = false
                if(viewModel.resumedLogin.value!!){
                    viewModel.resumedLogin.value = false
                    nav.navigateUp()
                }
                else{
                    nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
                }
            }
        })
        if(prefs.getBoolean("fingerprintEnabled", false)){
            showFingerprintAuthFlow(prefs)
        }
        else{
            showPinFragment()
        }
    }

    private fun showFingerprintAuthFlow(prefs: SharedPreferences) {
        val executor = Executors.newSingleThreadExecutor()
        val callback = object: BiometricPrompt.AuthenticationCallback() {
            fun disableFingerprint(){
                prefs.edit().putBoolean("fingerprintEnabled",false).apply()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    Snackbar.make(requireView(), "Authentication unsuccessful.", Snackbar.LENGTH_LONG)
                            .show()
                }
                disableFingerprint()
                showPinFragment()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Snackbar.make(requireView(), "Fingerprint authentication successful.", Snackbar.LENGTH_LONG)
                        .show()
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showPinFragment()
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate to access your saved passwords.")
                .setDescription("Click cancel to use your backup PIN.")
                .setNegativeButtonText("Cancel")
                .build()
        val biometricPrompt = BiometricPrompt(this, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showPinFragment(){
        val pinFragment = if (PinHelper.hasDefaultPinSaved(requireActivity()))
            PinSupportFragment.newInstanceForVerification()
        else
            PinSupportFragment.newInstanceForCreation()
        requireFragmentManager().beginTransaction()
                .replace(R.id.blankFragment, pinFragment)
                .addToBackStack(null)
                .commit()
    }

//    private fun showLaterMessage(extraMessage : String){
//        MaterialDialog(requireContext()).show {
//            lifecycleOwner(viewLifecycleOwner)
//            message(text = "$extraMessage You can always secure your passwords later. Just go to ...")
//            positiveButton(text = "Okay")
//            onDismiss {
//                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
//            }
//        }
//    }
}
