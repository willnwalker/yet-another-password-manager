package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.fragment.app.DialogFragment
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.dialog_fingerprint.*
import java.io.File
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


/**
 * DialogFragment that prompts the user to authenticate their fingerprint.
 */
class FingerprintDialog : DialogFragment(), FingerprintController.Callback {

    private lateinit var nav: NavController
    private lateinit var prefs: SharedPreferences
    private lateinit var uiListener: UIListener
    private var title: String = ""
    private var subtitle: String = ""
    private var flow: String = ""

    private val controller: FingerprintController by lazy {
        FingerprintController(
                FingerprintManagerCompat.from(requireContext()),
                this,
                titleTextView,
                subtitleTextView,
                errorTextView,
                iconFAB
        )
    }

    /**
     * CryptoObject is a wrapper class for any cryptography required by the FingerprintManager.
     * https://developer.android.com/reference/android/support/v4/hardware/fingerprint/FingerprintManagerCompat.CryptoObject.html
     */
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null

    /**
     * KeyStore is the device's storage for any cryptographic keys and certificates. We use this to get a key for the fingerprint manager.
     * https://developer.android.com/reference/java/security/KeyStore.html
     */
    private var keyStore: KeyStore? = null

    /**
     * This class is used to generate the keys that were reference from the [keyStore].
     * https://developer.android.com/reference/javax/crypto/KeyGenerator.html
     */
    private var keyGenerator: KeyGenerator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.dialog_fingerprint, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        controller.setTitle(arguments.getString(ARG_TITLE))
//        controller.setSubtitle(arguments.getString(ARG_SUBTITLE))
        controller.setTitle(this.title)
        controller.setSubtitle(this.subtitle)
        cancelButton.setOnClickListener {
            onError()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        }

        createKey(DEFAULT_KEY_NAME, false)

        val defaultCipher: Cipher
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        }

        if (initCipher(defaultCipher, DEFAULT_KEY_NAME)) {
            cryptoObject = FingerprintManagerCompat.CryptoObject(defaultCipher)
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cryptoObject?.let {
            controller.startListening(it)
        }
    }

    override fun onPause() {
        super.onPause()
        controller.stopListening()
    }

    override fun onAuthenticated() {

//        MaterialDialog.Builder(contextConfirmed)
//                .title("Success!")
//                .content("Authenticated with fingerprint successfully.")
//                .positiveText("Okay")
//                .show()
        this.dismiss()
        when{
            flow.equals("setup") -> {
//                val newKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//                val keyGenParameterSpec = KeyGenParameterSpec.Builder("RealmKey",
//                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                        .build()
//                newKeyGenerator.init(keyGenParameterSpec)
//                val secretKey = newKeyGenerator.generateKey()
                val key = ByteArray(64)
                SecureRandom().nextBytes(key)
                prefs.edit().putString("RealmKey", Base64.encodeToString(key, Base64.NO_WRAP)).apply()
                buildConfig(key)
                prefs.edit().putBoolean("firstRun",false).apply()
                prefs.edit().putBoolean("securityEnabled",true).apply()
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)

            }
            flow.equals("migrate") -> {
                val key = ByteArray(64)
                SecureRandom().nextBytes(key)
                prefs.edit().putString("RealmKey", Base64.encodeToString(key, Base64.NO_WRAP)).apply()


                val oldRealm = Realm.getInstance(uiListener.getRealmConfig())
                val path = uiListener.getRealmConfig().path
                File(path+"temp_realm").delete()
                oldRealm.writeEncryptedCopyTo(File(path+"temp_realm"),key)
                oldRealm.close()
                Realm.deleteRealm(uiListener.getRealmConfig())

                val tempRealm = Realm.getInstance(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().directory(File(path+"temp_realm")).encryptionKey(key).build())
                tempRealm.writeCopyTo(File(Realm.DEFAULT_REALM_NAME))

                buildConfig(key)
                prefs.edit().putBoolean("securityEnabled",true).apply()
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)

            }
            else -> {
//                val keyStore = KeyStore.getInstance("AndroidKeyStore")
//                keyStore.load(null)
//                val secretKey = keyStore.getKey("RealmKey", null)
                val key = Base64.decode(prefs.getString("RealmKey",""), Base64.NO_WRAP)
                buildConfig(key)
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
            }
        }
    }

    override fun onError() {
        this.dismiss()
        when{
            flow == "setup" -> {
                prefs.edit().putBoolean("securityEnabled",false).apply()
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(text = "Failure!")
                    message(text = "Fingerprint security setup was not successful. You can always enable it later in the 'Settings' menu.")
                    positiveButton(text = "Okay")
                }
                uiListener.setRealmConfig(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
                nav.navigate(R.id.action_loginSetupFragment_to_passwordListFragment)
            }
            flow == "migrate" -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(text = "Failure!")
                    message(text = "Fingerprint authentication was not successful. Returning to password list.")
                    positiveButton(text = "Okay")
                    onDismiss { nav.navigate(R.id.passwordListFragment) }
                }
//                MaterialDialog.Builder(requireContext())
//                        .title("Failure!")
//                        .content("")
//                        .positiveText("")
//                        .onAny { _, _ -> nav.navigate(R.id.passwordListFragment) }
//                        .show()
            }
            else -> {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(text = "Failure!")
                    message(text = "Fingerprint authentication was not successful. Exiting.")
                    positiveButton(text = "Okay")
                    onDismiss { requireActivity().finish() }
                }
//                MaterialDialog.Builder(requireContext())
//                        .title("Failure!")
//                        .content("Fingerprint authentication was not successful. Exiting.")
//                        .positiveText("Okay")
//                        .onAny { _, _ -> uiListener.exit() }
//                        .show()
            }
        }
    }

    /**
     * Lifted code from the Google samples - https://github.com/googlesamples/android-FingerprintDialog/blob/master/kotlinApp/app/src/main/java/com/example/android/fingerprintdialog/MainActivity.kt
     *
     * Initialize the [Cipher] instance with the created key in the
     * [.createKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization is successful, `false` if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            keyStore?.load(null)
            val key = keyStore?.getKey(keyName, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

    /**
     * Lifted code from the Google Samples - https://github.com/googlesamples/android-FingerprintDialog/blob/master/kotlinApp/app/src/main/java/com/example/android/fingerprintdialog/MainActivity.kt
     *
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not
     * be invalidated even if a new fingerprint is enrolled.
     * The default value is `true`, so passing
     * `true` doesn't change the behavior
     * (the key will be invalidated if a new fingerprint is
     * enrolled.). Note that this parameter is only valid if
     * the app works on Android N developer preview.
     */
    private fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            keyStore?.load(null)
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            val builder = KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }
            keyGenerator?.init(builder.build())
            keyGenerator?.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun buildConfig(bytes: ByteArray){
        val realmConfig = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().encryptionKey(bytes).build()
        uiListener.setRealmConfig(realmConfig)
    }

    companion object {
        /**
         * Fragment tag that is used when this dialog is shown.
         */
        val FRAGMENT_TAG: String = FingerprintDialog::class.java.simpleName

        // Bundle keys for each of the arguments of the newInstance method.
//        private val ARG_TITLE = "ArgTitle"
//        private val ARG_SUBTITLE = "ArgSubtitle"

        private const val DEFAULT_KEY_NAME = "default_key"

        /**
         * Creates a new FingerprintDialog instance with initial text setup.
         *
         * @param[title] The title of this FingerprintDialog.
         * @param[subtitle] The subtitle or description of the dialog.
         */
        fun newInstance(title: String, subtitle: String, flow: String, nav: NavController): FingerprintDialog {
//            val args = Bundle()
//            args.putString(ARG_TITLE, title)
//            args.putString(ARG_SUBTITLE, subtitle)

            val fragment = FingerprintDialog()
            fragment.setVars(title, subtitle, flow, nav)
//            fragment.arguments = args

            return fragment
        }
    }

    fun setVars(title: String, subtitle: String, flow: String, nav: NavController){
        this.title = title
        this.subtitle = subtitle
        this.flow = flow
        this.nav = nav
    }

    // Need this because context doesn't exist until fragment attached to navigation controller
    override fun onAttach(context: Context){
        super.onAttach(context)
        uiListener = requireContext() as UIListener
    }
}