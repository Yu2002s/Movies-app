package com.dongyu.movies.fragment.setting

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.dongyu.movies.R
import com.dongyu.movies.activity.MainActivity

class QuestionFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.preference_question, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity() is MainActivity) {
            val typedArray = requireContext()
                .obtainStyledAttributes(intArrayOf(com.google.android.material.R.attr.colorSurface))

            view.setBackgroundColor(typedArray.getColor(0, Color.WHITE))

            typedArray.recycle()
        }
    }
}