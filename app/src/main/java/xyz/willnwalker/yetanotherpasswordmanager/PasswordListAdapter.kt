package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import org.w3c.dom.Text

class PasswordListAdapter(
        realmConfig: RealmConfiguration,
        context: Context,
        private val data: RealmResults<Entry>,
        automaticUpdate: Boolean,
        animateIdType: Boolean,
        animateExtraColumnName: String) : RealmBasedRecyclerViewAdapter<Entry, PasswordListAdapter.ViewHolder>(
        context,
        data,
        automaticUpdate,
        animateIdType,
        animateExtraColumnName){

    private val realm = Realm.getInstance(realmConfig)

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
        holder.mTextView.text = data[position]!!.title
        //set list item onclicklistener here
        holder.container.setOnClickListener {
            val entry = data[position] as Entry

            var dialog : MaterialDialog = MaterialDialog.Builder(context)
                    //.title(entry.title)
                    .autoDismiss(false)
                    .customView(R.layout.password_view_dialog, true)


/*
                    .content("Username: " + entry.userName
                            + "\n" + "Password: " + entry.password
                            + "\n"  + "Notes: " + entry.notes
                            + "\n"  + "URL: " + entry.url)
                    */

                    .positiveText("Edit")
                    .onPositive{ dialog, _ ->
                        dialog.dismiss()
                        val args = PasswordListFragmentDirections.actionNewPassword().setUuid(entry.id);
                        findNavController(it).navigate(args)
//                        dialog.dismiss()
//                        val bundle = Bundle()
//                        bundle.putString("uuid", entry.id)
//                        findNavController(it).navigate(R.id.action_new_password, bundle)
                    }
                    .negativeText("Delete")
                    .onNegative { dialog, _ ->
                        MaterialDialog.Builder(context)
                                .title("Are you sure you want to delete this entry?")
                                .positiveText("Yes")
                                .onPositive{_, _ ->
                                    dialog.dismiss()
                                    realm.beginTransaction()
                                    entry.deleteFromRealm()
                                    realm.commitTransaction()
                                    Snackbar.make(it, "Password Deleted.", Snackbar.LENGTH_LONG).show()
                                }
                                .negativeText("No")
                                .show()
                    }
                    .show()

            val customView = dialog.customView
            val serviceName: TextView = customView!!.findViewById(R.id.title)
            val username: TextView = customView.findViewById(R.id.username)
            val password: TextView = customView.findViewById(R.id.password)
            val notes: TextView = customView.findViewById(R.id.notes)
            val url: TextView = customView.findViewById(R.id.url)

            /*serviceName.text = entry.title
            username.text = entry.userName
            password.text = entry.password
            notes.text = entry.notes
            url.text = entry.url*/

            serviceName.text = entry.title
            username.append(" " + entry.userName)
            password.append(" " + entry.password)
            notes.append(" " + entry.notes)
            url.append(" " + entry.url)

            dialog.show()
            
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return data.size
    }


}