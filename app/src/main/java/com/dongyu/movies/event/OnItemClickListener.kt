package com.dongyu.movies.event

import android.view.View

interface OnItemClickListener {

  fun onItemClick(view: View, position: Int)
}