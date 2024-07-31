package com.dongyu.movies.event

import android.view.View

interface OnCardItemClickListener {

    fun onCardItemClick(outerView: View, innerView: View, outerPosition: Int, innerPosition: Int)

}