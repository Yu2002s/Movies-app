package com.dongyu.movies.fragment.video;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;

import com.dongyu.movies.R;
import com.dongyu.movies.activity.MainActivity;
import com.dongyu.movies.utils.TimeUtilsKt;

/**
 * 播放器设置相关
 */
public class PlayerSettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle bundle, @Nullable String s) {
          setPreferencesFromResource(R.xml.preference_player_setting, s);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (requireActivity() instanceof MainActivity) {
            TypedArray typedArray = requireContext().obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorSurface});

            view.setBackgroundColor(typedArray.getColor(0, Color.WHITE));

            typedArray.recycle();
        }

        SeekBarPreference startTime = findPreference("skip_start_time");
        assert startTime != null;
        startTime.setSummary("时间: " + TimeUtilsKt.getTime(startTime.getValue()));
        startTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object o) {
                long time = (int)o;
                preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
                return true;
            }
        });

        SeekBarPreference endTime = findPreference("skip_end_time");
        assert endTime != null;
        endTime.setSummary("时间: " + TimeUtilsKt.getTime(endTime.getValue()));
        endTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object o) {
                long time = (int)o;
                preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
                return true;
            }
        });
    }
}
