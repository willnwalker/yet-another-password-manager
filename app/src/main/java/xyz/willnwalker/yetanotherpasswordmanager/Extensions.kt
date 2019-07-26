package xyz.willnwalker.yetanotherpasswordmanager

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.TextView

internal fun EditText.validateWith(passIcon: Drawable?, failIcon: Drawable?, validator: (TextView) -> Boolean): Boolean {
    setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, if(validator(this)) passIcon else failIcon, null)
    if(!validator(this)) background.mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
    else background.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
    return validator(this)
}
