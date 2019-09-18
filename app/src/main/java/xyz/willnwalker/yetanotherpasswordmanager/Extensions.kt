package xyz.willnwalker.yetanotherpasswordmanager

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

internal fun EditText.validateWith(passIcon: Drawable?, failIcon: Drawable?, validator: (TextView) -> Boolean): Boolean {
    setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, if(validator(this)) passIcon else failIcon, null)
    if(!validator(this)) background.mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
    else background.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    return validator(this)
}

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> AppCompatActivity.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, BaseViewModelFactory(creator)).get(T::class.java)
}
