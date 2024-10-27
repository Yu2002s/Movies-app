package com.dongyu.movies.fragment.video;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;

import com.dongyu.movies.R;
import com.dongyu.movies.activity.LiveVideoActivity;
import com.dongyu.movies.activity.MainActivity;
import com.dongyu.movies.activity.VideoActivity;
import com.dongyu.movies.config.SPConfig;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.view.player.DongYuPlayer;

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
        startTime.setOnPreferenceChangeListener((preference, o) -> {
            long time = (int)o;
            preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
            return true;
        });

        SeekBarPreference endTime = findPreference("skip_end_time");
        assert endTime != null;
        endTime.setSummary("时间: " + TimeUtilsKt.getTime(endTime.getValue()));
        endTime.setOnPreferenceChangeListener((preference, o) -> {
            long time = (int)o;
            preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
            return true;
        });

        SeekBarPreference danmakuLine = findPreference(SPConfig.PLAYER_DANMAKU_LINE);
        assert danmakuLine != null;
        danmakuLine.setOnPreferenceChangeListener((preference, o) -> {
            int line = (int) o;
            DongYuPlayer player = getPlayer();
            if (player != null) {
                player.setDanmakuLine(line);
            }
            return true;
        });

        SeekBarPreference danmakuAlpha = findPreference(SPConfig.PLAYER_DANMAKU_ALPHA);
        assert danmakuAlpha != null;
        danmakuAlpha.setOnPreferenceChangeListener((preference, o) -> {
            int alpha = (int) o;
            DongYuPlayer player = getPlayer();
            if (player != null) {
                player.setDanmakuAlpha(alpha / 10f);
            }
            return true;
        });

        SeekBarPreference danmakuSize = findPreference(SPConfig.PLAYER_DANMAKU_SIZE);
        assert danmakuSize != null;
        danmakuSize.setOnPreferenceChangeListener((preference, o) -> {
            int size = (int) o;
            DongYuPlayer player = getPlayer();
            if (player != null) {
                player.setDanmakuSize(size / 10f);
            }
            return true;
        });

        SeekBarPreference danmakuMargin = findPreference(SPConfig.PLAYER_DANMAKU_MARGIN);
        assert danmakuMargin != null;
        danmakuMargin.setOnPreferenceChangeListener((preference, o) -> {
            int margin = (int) o;
            DongYuPlayer player = getPlayer();
            if (player != null) {
                player.setDanmakuMargin(margin);
            }
            return true;
        });

        SeekBarPreference danmakuSpeed = findPreference(SPConfig.PLAYER_DANMAKU_SPEED);
        assert danmakuSpeed != null;
        danmakuSpeed.setOnPreferenceChangeListener((preference, o) -> {
            int speed = (int) o;
            DongYuPlayer player = getPlayer();
            if (player != null) {
                player.setDanmakuSpeed(speed / 10f);
            }
            return true;
        });
    }

    @Nullable
    private DongYuPlayer getPlayer() {
        FragmentActivity activity = requireActivity();
        if (!(activity instanceof VideoActivity || activity instanceof LiveVideoActivity)) {
            return null;
        }
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        ViewGroup parentView = (ViewGroup) contentView.getChildAt(0);
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (child instanceof DongYuPlayer) {
                return (DongYuPlayer) child;
            }
        }
        return null;
    }
}
