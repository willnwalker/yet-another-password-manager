package xyz.willnwalker.yetanotherpasswordmanager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.afollestad.materialdialogs.DialogAction
import io.realm.*
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.kotlin.deleteFromRealm

class PasswordListAdapter(
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

    private val config: RealmConfiguration = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
    private val realm = Realm.getInstance(config)

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

    // Create new views (invoked by the layout manager)
    override fun onCreateRealmViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(v as FrameLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindRealmViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.text = data[position]!!.title
        //set list item onclicklistener here
        holder.container.setOnClickListener {
            val entry = data[position] as Entry
//            PasswordListFragmentDirections.actionNewPassword().setUuid(entry.id)
//            findNavController(it).navigate(R.id.action_new_password)
            MaterialDialog.Builder(context)
                    .title(entry.title)
                    .autoDismiss(false)
                    .content("Username: " + entry.userName
                            + "\n" + "Password: " + entry.password
                            + "\n"  + "Notes: " + entry.notes
                            + "\n"  + "URL: " + entry.url)
                    .positiveText("Edit")
                    .onPositive{ dialog, _ ->
//                        PasswordListFragmentDirections.actionNewPassword().setUuid(entry.id);
//                        findNavController(it).navigate(R.id.action_new_password)
                        dialog.dismiss()
                        val bundle = Bundle()
                        bundle.putString("uuid", entry.id)
                        findNavController(it).navigate(R.id.action_new_password, bundle)
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
                                    Toast.makeText(context, "Password Deleted", Toast.LENGTH_SHORT).show()
                                }
                                .negativeText("No")
                                .show()
                    }
                    .show()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return data.size
    }


}