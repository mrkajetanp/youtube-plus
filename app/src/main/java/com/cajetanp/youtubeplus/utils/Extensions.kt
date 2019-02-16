package com.cajetanp.youtubeplus.utils

import android.app.Activity
import android.content.Context
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView

fun Activity.hideKeyboard() {
    val view: View = this.findViewById(android.R.id.content)
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * A helper method to iconify/expand SearchView and adjust its content based on the filterQuery
 * If the query is empty, it'll clear the content and iconify the SearchView
 * If the query is not empty, it'll make sure the SearchView is expanded and reflects it
 * Must be called on a Menu object reflecting the current OptionsMenu
 * Won't do anything if the Menu object doesn't contain a SearchView with a passed id
 * */
fun Menu.adjustSearchView(searchViewId: Int, filterQuery: String) {
    val searchView = this.findItem(searchViewId)?.actionView as SearchView?

    if (filterQuery.isEmpty()) {
        searchView?.setQuery("", false)
        searchView?.isIconified = true
    } else {
        searchView?.setQuery(filterQuery, false)
        searchView?.isIconified = false
    }

    searchView?.clearFocus()
}

