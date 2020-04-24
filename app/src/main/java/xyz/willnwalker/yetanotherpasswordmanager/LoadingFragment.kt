package xyz.willnwalker.yetanotherpasswordmanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager

/**
 * A simple [Fragment] subclass.
 *
 */
class LoadingFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var nav : NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nav = Navigation.findNavController(requireActivity(), R.id.nav_host)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        viewModel = requireActivity().getViewModel { SharedViewModel(prefs) }
    }

    override fun onResume() {
        super.onResume()
        Log.i("xyz.willnwalker.yapm","Resuming Loading Fragment.")
        viewModel.securityEnabled.observe(this, Observer { securityEnabled ->
            if(securityEnabled){
                nav.navigate(R.id.loginFragment)
            }
            else{
                nav.navigate(LoadingFragmentDirections.actionLoadingFragmentToPasswordListFragment())
            }
        })
    }

}
