package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog

/**
 * A simple [Fragment] subclass.
 *
 */
class LoginSetupFragment : Fragment() {

    private lateinit var contextConfirmed : Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        PreferenceManager.getDefaultSharedPreferences(this)
//        .edit().putBoolean("firstrun", false).commit();
        MaterialDialog.Builder(contextConfirmed)
                .title("Welcome!")
                .content(R.string.setup_login_dialog_message)
                .positiveText("Yes")
                .onPositive{_, _ ->
                    Toast.makeText(context, "Password Deleted", Toast.LENGTH_SHORT).show()
                }
                .negativeText("No")
                .onNegative{_, _ ->
                    PreferenceManager.getDefaultSharedPreferences(contextConfirmed).edit().putBoolean("firstRun",false).putBoolean("securityEnabled",false).apply()
                    MaterialDialog.Builder(contextConfirmed)
                            .content(R.string.setup_login_dialog_message_negative)
                            .positiveText("Okay")
                            .onPositive{dialog, _ ->
                                PreferenceManager.getDefaultSharedPreferences(contextConfirmed)
                                findNavController().navigate(R.layout.fragment_password_list)
                            }
                            .show()                }
                .show()

    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(_context: Context){
        super.onAttach(context)
        contextConfirmed = _context
    }

}
