package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.fragment_password_view.*
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.kotlin.where
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import android.widget.Toast


/**
 * A simple [Fragment] subclass.
 *
 */
class PasswordViewFragment : Fragment() {
    private lateinit var contextConfirmed : Context
    private lateinit var config: RealmConfiguration
    private lateinit var realm : Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        realm = Realm.getInstance(config)
        val uuid = PasswordViewFragmentArgs.fromBundle(arguments).uuid

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
            MaterialDialog.Builder(contextConfirmed)
                    .title("Generate Password")
                    .content("Specify password length:")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .inputRange(0,2)
                    .input("Password Length", null){dialog: MaterialDialog, input: CharSequence  ->
                        val pass = genPassword(input.toString().toInt(), dialog.isPromptCheckBoxChecked)
                        passwordTextField.setText(pass)
                        passwordTextField2.setText(pass)
                    }
                    .positiveText("Generate")
                    .negativeText("Cancel")
                    .checkBoxPrompt("Allow Special Characters?", true, null)
                    .show()
        }
    }

    private fun initSaveClickListener(entry: Entry){
        button_save.setOnClickListener {
            if(validateEntries()){
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
        val range: Int
        var pass = ""
        //Set the range of ascii values depending on special character allowance
        when (specialChars) {
            true -> range = 94
            false -> range = 62
        }
        for (i in 0 until length) {
            try {
                val secRand = SecureRandom.getInstance("SHA1PRNG") //Most secure windows algorithm
                if (specialChars)
                    pass += (secRand.nextInt(range) + 33).toChar()
                else
                    pass += genCharacter(secRand.nextInt(range))
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
        if (passwordTextField.text.toString() != passwordTextField2.text.toString()) {
            content = "Please make sure that your passwords match."
        }
        if (passwordTextField.text.toString() == ""){
            content = "Please enter a password for your account."
        }
        if(serviceName.text.toString() == "") {
            content = "Please enter a title for your account."
        }
        return if(content == "")
            true
        else{
            //Create alert dialog
            MaterialDialog.Builder(contextConfirmed)
                    .positiveText("Okay")
                    .content(content)
                    .show()
            false
        }
    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(_context: Context){
        super.onAttach(_context)
        contextConfirmed = _context
    }

}
