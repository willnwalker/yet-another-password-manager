package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import io.realm.*
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.kotlin.deleteFromRealm
import android.content.ClipData
import android.content.ClipboardManager
import android.text.SpannableString
import android.text.style.ClickableSpan
import kotlinx.android.synthetic.main.password_view_dialog.view.*
import android.content.Intent
import android.net.Uri
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.webkit.URLUtil
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner

class PasswordListAdapter(
        private val realmConfig: RealmConfiguration,
        context: Context,
        private val lifecycleOwner: LifecycleOwner,
        private val data: RealmResults<Entry>,
        automaticUpdate: Boolean,
        animateIdType: Boolean,
        animateExtraColumnName: String) : RealmBasedRecyclerViewAdapter<Entry, PasswordListAdapter.ViewHolder>(
        context,
        data,
        automaticUpdate,
        animateIdType,
        animateExtraColumnName){

    private var realm = Realm.getInstance(realmConfig)
    private lateinit var clipboard: ClipboardManager
    private var clip: ClipData? = null
    private lateinit var lastDialog: MaterialDialog
    private var lastDialogAvailable = false

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     * each data item is just a string in this case
     */
    inner class ViewHolder(var container: FrameLayout) : RealmViewHolder(container) {
        var mTextView: TextView = container.findViewById<View>(R.id.title) as TextView

        init {
            mTextView = container.findViewById<View>(R.id.title) as TextView
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateRealmViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(v as FrameLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindRealmViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        holder.mTextView.text = data[position]!!.title
        //set list item onclicklistener here
        holder.container.setOnClickListener {container ->
            val entry = data[position] as Entry

            val lastDialog = MaterialDialog(context).show {
                lifecycleOwner(lifecycleOwner)
                noAutoDismiss()
                customView(R.layout.password_view_dialog)
                positiveButton(text = "Edit"){
                    it.dismiss()
                    val args = PasswordListFragmentDirections.actionNewPassword(entry.id)
                    findNavController(container).navigate(args)
                }
                negativeButton(text = "Delete"){
                    MaterialDialog(context).show {
                        lifecycleOwner(lifecycleOwner)
                        title(text = "Are you sure you want to delete this entry?")
                        positiveButton(text = "Yes"){
                            it.dismiss()
                            realm.beginTransaction()
                            entry.deleteFromRealm()
                            realm.commitTransaction()
                            Snackbar.make(container, "Password Deleted.", Snackbar.LENGTH_LONG).show()
                        }
                        negativeButton(text = "No")
                    }
                }
            }
//            lastDialog = MaterialDialog.Builder(context)
//                    //.title(entry.title)
//                    .autoDismiss(false)
//                    .customView(R.layout.password_view_dialog, true)
//
//                    .positiveText("Edit")
//                    .onPositive{ dialog, _ ->
//                        dialog.dismiss()
//                        val args = PasswordListFragmentDirections.actionNewPassword().setUuid(entry.id)
//                        findNavController(it).navigate(args)
//                    }
//                    .negativeText("Delete")
//                    .onNegative { dialog, _ ->
//                        MaterialDialog.Builder(context)
//                                .title("Are you sure you want to delete this entry?")
//                                .positiveText("Yes")
//                                .onPositive{_, _ ->
//                                    dialog.dismiss()
//                                    realm.beginTransaction()
//                                    entry.deleteFromRealm()
//                                    realm.commitTransaction()
//                                    Snackbar.make(it, "Password Deleted.", Snackbar.LENGTH_LONG).show()
//                                }
//                                .negativeText("No")
//                                .show()
//                    }
//                    .show()

            val customView = lastDialog.getCustomView()
            val serviceName: TextView = customView.findViewById(R.id.title)
            val username: TextView = customView.findViewById(R.id.username)
            val password: TextView = customView.findViewById(R.id.password)
            val notes: TextView = customView.findViewById(R.id.notes)
            val url: TextView = customView.findViewById(R.id.url)

            serviceName.text = entry.title
            username.append(entry.userName)
            password.movementMethod = ScrollingMovementMethod()
            password.append(entry.password)
            notes.append(entry.notes)

            val spannableString = SpannableString("URL: "+entry.url)
            val clickableSpan = object: ClickableSpan(){
                override fun onClick(textView: View) {
                    var urlUri = Uri.parse(entry.url)
                    if(!URLUtil.isValidUrl(entry.url)){
                        urlUri = Uri.parse("http://"+entry.url)
                    }
                    val intent = Intent(Intent.ACTION_VIEW, urlUri)
                    context.startActivity(intent)
                }
            }
            spannableString.setSpan(clickableSpan, 5, 5+entry.url.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            url.text = spannableString
            url.movementMethod = LinkMovementMethod.getInstance()

            customView.button_copy_password.setOnClickListener {
                clip = ClipData.newPlainText("password", entry.password)
                clipboard.setPrimaryClip(clip!!)
                Toast.makeText(context, "Password Copied!", Toast.LENGTH_SHORT).show()
            }

            lastDialogAvailable = true
            lastDialog.show()

        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return data.size
    }

    fun onPause(){
//        if(lastDialogAvailable){
//            lastDialog.dismiss()
//        }
        realm.close()
    }

    fun onResume(){
        realm = Realm.getInstance(realmConfig)
    }

}