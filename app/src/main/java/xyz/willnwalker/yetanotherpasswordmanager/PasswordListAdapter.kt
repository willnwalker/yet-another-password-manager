package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import io.realm.*
import io.realm.kotlin.delete
import io.realm.kotlin.deleteFromRealm
import xyz.willnwalker.yetanotherpasswordmanager.R.id.action_new_password

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

    // Provide a suitable constructor (depends on the kind of dataset)
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    // each data item is just a string in this case
    inner class ViewHolder(var container: FrameLayout) : RealmViewHolder(container) {
        var mTextView: TextView = container.findViewById<View>(R.id.textView) as TextView

        init {
            mTextView = container.findViewById<View>(R.id.textView) as TextView
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
            /*val i = Intent(applicationContext, NewPasswordActivity::class.java)
            val b = Bundle()
            val e = data[position]
            b.putLong("entryID", e!!.uuid)
            b.putString("service", e.title)
            b.putString("password", e.password)
            i.putExtras(b)
            startActivityForResult(i, oldPassword)*/
            /*val bundle = Bundle()
            bundle.putString("uuid",data[position]!!.uuid)*/
            val entry = data[position] as Entry
            //val action = PasswordListFragmentDirections.ActionNewPassword(entry.uuid)
            PasswordListFragmentDirections.actionNewPassword().setUuid(entry.id)
            findNavController(it).navigate(R.id.action_new_password)//, bundle)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return data.size
    }


}