package xyz.willnwalker.yetanotherpasswordmanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Kyle: this class is used to add custom decorations to PasswordListFragment
 */
class PasswordListItemDecoration(
        context: Context,
        private val paddingLeft: Int,
        private val paddingRight: Int
) : RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null

    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider)
    }

    /**
     * Draws decorations for child views. Decorations are drawn after child views are drawn and
     * therefore appears over child views.
     *
     * Actually draws the line divider
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        // sets right and left padding
        val left = parent.paddingLeft + paddingLeft
        val right = parent.width - parent.paddingRight - paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i) // Gets current child
            val params = child.layoutParams as RecyclerView.LayoutParams // gets params for child
            val top = child.bottom + params.bottomMargin // gets top margin
            val bottom = top + (mDivider?.intrinsicHeight ?: 0) // gets bottom margin, if no mDivider, sets to 0

            // draws divider
            mDivider?.let {
                it.setBounds(left, top, right, bottom)
                it.draw(c)
            }

        }
    }
}
