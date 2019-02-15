package com.cajetanp.youtubeplus.utils

import android.app.Activity
import android.view.View

/**
 * Class responsible for changing the view from full screen to non-full screen and vice versa
 *
 * Created by Pierfrancesco Soffritti, converted to Kotlin by Cajetan Puchalski
 * @author Pierfrancesco Soffritti, Cajetan Puchalski
 */
class FullScreenHelper(val context: Activity, vararg val views: View) {

    fun enterFullScreen() {
        context.window.decorView.hideSystemUi()

        for (view in views) {
            view.visibility = View.VISIBLE
            view.invalidate()
        }
    }

    fun exitFullScreen() {
        context.window.decorView.showSystemUI(context)

        for (view in views) {
            view.visibility = View.VISIBLE
            view.invalidate()
        }
    }

    private fun View.hideSystemUi() {
        this.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun View.showSystemUI(context: Activity) {
        this.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        context.invalidateOptionsMenu()
    }
}