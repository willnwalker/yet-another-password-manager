package xyz.willnwalker.yetanotherpasswordmanager

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.navigation.Navigation.findNavController

import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.fragment_password_view.*
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import android.widget.TextView
import android.R.drawable.edit_text
import android.widget.EditText
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_password_view.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PasswordViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PasswordViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordViewFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var config: RealmConfiguration
    private lateinit var realm : Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        realm = Realm.getInstance(config)
        val uuid = arguments!!.getString("uuid")

        if(uuid != "NEW_PASSWORD"){
            var entry = realm.where<Entry>().equalTo("id",uuid).findFirst()
            serviceName.setText(entry!!.title)
            serviceUsername.setText(entry.userName)
            passwordTextField.setText(entry.password)
            passwordTextField2.setText(entry.password)
            url.setText(entry.url)
            notes.setText(entry.notes)
            button_save.setOnClickListener {
                realm.beginTransaction()

                entry.title = serviceName.text.toString()
                entry.userName = serviceUsername.text.toString()
                entry.password = passwordTextField.text.toString()
                entry.url = url.text.toString()
                entry.notes = notes.text.toString()

                realm.commitTransaction()
                findNavController(it).navigateUp()
            }
        }
        else{
            var entry = Entry()
            // Kyle - Save button OnClickListener
            button_save.setOnClickListener {
                entry.title = serviceName.text.toString()
                entry.userName = serviceUsername.text.toString()
                entry.password = passwordTextField.text.toString()
                entry.url = url.text.toString()
                entry.notes = notes.text.toString()

                realm.beginTransaction()
                realm.copyToRealm(entry)
                realm.commitTransaction()
                findNavController(it).navigateUp()
            }
        }



        button_genpassword.setOnClickListener{
            val pass = genPassword(10, true)
            passwordTextField.setText(pass)
            passwordTextField2.setText(pass)
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    fun genPassword(length: Int, specialChars: Boolean): String {
        val range: Int
        var pass = ""
        if (specialChars) {
            range = 94
        } else {
            range = 62
        }
        for (i in 0 until length) {
            try {
                val secRand = SecureRandom.getInstance("SHA1PRNG")
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

    fun genCharacter(num: Int): Char {
        var num = num
        // when is like switch in java
        when {
            num <= 9 -> num += 48
            num <= 35 -> num += 55
            num <= 62 -> num += 61
            else -> println("secRand returned out of range $num")
        }
        return num.toChar()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PasswordViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): PasswordViewFragment {
            val fragment = PasswordViewFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
