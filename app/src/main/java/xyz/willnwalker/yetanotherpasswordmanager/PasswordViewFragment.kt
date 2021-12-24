package xyz.willnwalker.yetanotherpasswordmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.preference.PreferenceManager
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_password_view.*
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.kotlin.where
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner


/**
 * A simple [Fragment] subclass.
 *
 */
class PasswordViewFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel
    private lateinit var realm : Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        viewModel = requireActivity().getViewModel { SharedViewModel(prefs) }

//        realm = Realm.getInstance(viewModel.realmConfig!!)
        realm = Realm.getDefaultInstance()
        val uuid = PasswordViewFragmentArgs.fromBundle(arguments!!).uuid

        if(isNewPassword(uuid)){
            val entry = Entry()
            // Kyle - Save button OnClickListener
            initSaveClickListener(entry)
        }
        else{
                val entry = realm.where<Entry>().equalTo("id", uuid).findFirst()
                serviceName.setText(entry!!.title)
                serviceUsername.setText(entry.userName)
                passwordTextField.setText(entry.password)
                passwordTextField2.setText(entry.password)
                url.setText(entry.url)
                notes.setText(entry.notes)

                initSaveClickListener(entry)

        }

        button_genpassword.setOnClickListener{
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                input(
                        allowEmpty = false,
                        inputType = InputType.TYPE_CLASS_NUMBER,
                        maxLength = 2
                ){ _, text ->
                    val pass = genPassword(text.toString().toInt(), false)
                    passwordTextField.setText(pass)
                    passwordTextField2.setText(pass)
                }
                title(text = "Generate Password")
                message(text = "Specify password length:")
                positiveButton(text = "Generate")
                negativeButton(text = "Cancel")
            }
//            MaterialDialog.Builder(contextConfirmed)
//                    .title("Generate Password")
//                    .content("Specify password length:")
//                    .inputType(InputType.TYPE_CLASS_NUMBER)
//                    .inputRange(1,2)
//                    .input(null, "12") { dialog: MaterialDialog, input: CharSequence  ->
//                        var pass = genPassword(input.toString().toInt(), dialog.isPromptCheckBoxChecked)
//                        passwordTextField.setText(pass)
//                        passwordTextField2.setText(pass)
//                    }
//                    .positiveText("Generate")
//                    .negativeText("Cancel")
//                    .checkBoxPrompt("Allow Special Characters?", true, null)
//                    .show()
        }
    }

    private fun initSaveClickListener(entry: Entry){
        button_save.setOnClickListener {
            if(validateEntries()) {
                realm.beginTransaction()

                entry.title = serviceName.text.toString()
                entry.userName = serviceUsername.text.toString()
                entry.password = passwordTextField.text.toString()
                entry.url = url.text.toString()
                entry.notes = notes.text.toString()

                realm.copyToRealmOrUpdate(entry)
                realm.commitTransaction()
                findNavController(it).navigateUp()
            }
        }
    }

    private fun isNewPassword(uuid: String): Boolean{
        return uuid == "new_password"
    }

    private fun genPassword(length: Int, specialChars: Boolean): String {
        var pass = ""
        //Set the range of ascii values depending on special character allowance
        val range = when (specialChars) {
            true -> 94
            false -> 62
        }
        for (i in 0 until length) {
            try {
                val secRand = SecureRandom.getInstance("SHA1PRNG") //Most secure windows algorithm
                pass += if (specialChars)
                    (secRand.nextInt(range) + 33).toChar()
                else
                    genCharacter(secRand.nextInt(range))
            } catch (e: NoSuchAlgorithmException) {
                //Implement some type of "Try again" pop-up message
            }
        }
        return pass
    }

    private fun genCharacter(number: Int): Char {
        var num = number
        // when is like switch in java
        when {
            num <= 9 -> num += 48
            num <= 35 -> num += 55
            num <= 62 -> num += 61
            else -> println("secRand returned out of range $num")
        }
        return num.toChar()
    }

    private fun validateEntries(): Boolean {
        //Test cases
        var content = ""
        //Check for invalid entries
        if(url.text.toString() != "" && !isValidURL(url.text.toString())){
            content = "Please enter a valid URL"
        }
        if (passwordTextField.text.toString() != passwordTextField2.text.toString()) {
            content = "Please make sure that your passwords match."
        }
        if (passwordTextField.text.toString() == "") {
            content = "Please enter a password for your account"
        }
        if(serviceName.text.toString() == "") {
            content = "Please enter a title for your account."
        }


        //validateWith function to change the underline color for each edit text field
        serviceName.validateWith(null, null) { textView -> textView.text.isNotEmpty()}
        passwordTextField.validateWith(null,null) {textView -> textView.text.isNotEmpty()}
        passwordTextField2.validateWith(null,null) {textView -> textView.text.toString() == passwordTextField.text.toString()}
        if(url.text.toString() != "") url.validateWith(null,null) {textView -> isValidURL(textView.text.toString())}


        return if(content == "")
            true
        else {
            val toast = Toast.makeText(requireContext(), content, Toast.LENGTH_SHORT)
            toast.show()
            false
        }
    }

    private fun isValidURL(url: String): Boolean {
        //if(URLUtil.isValidUrl(url)) {
            if (Patterns.WEB_URL.matcher(url).matches())
                return true
        //}
        return false
    }

}
