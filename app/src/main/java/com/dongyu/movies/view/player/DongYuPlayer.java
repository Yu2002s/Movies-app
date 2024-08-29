package com.dongyu.movies.view.player;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dongyu.movies.BiliDanmukuParser;
import com.dongyu.movies.MoviesApplication;
import com.dongyu.movies.MyDanmakuLoader;
import com.dongyu.movies.R;
import com.dongyu.movies.config.SPConfig;
import com.dongyu.movies.databinding.LayoutControlBottomBinding;
import com.dongyu.movies.databinding.LayoutControlHeaderBinding;
import com.dongyu.movies.databinding.LayoutControlMiddleBinding;
import com.dongyu.movies.databinding.LayoutSmallProgressBinding;
import com.dongyu.movies.databinding.LayoutStatusBarBinding;
import com.dongyu.movies.databinding.LayoutToastBinding;
import com.dongyu.movies.databinding.LayoutVideoErrorBinding;
import com.dongyu.movies.dialog.ScreenProjectionDialog;
import com.dongyu.movies.utils.DisplayUtilsKt;
import com.dongyu.movies.utils.IOKt;
import com.dongyu.movies.utils.SpUtils;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.view.player.base.BasePlayer;
import com.dongyu.movies.view.player.base.PlayerStateListener;
import com.wanban.screencast.ScreenCastUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.ui.widget.DanmakuTouchHelper;
import master.flame.danmaku.ui.widget.DanmakuView;

public class DongYuPlayer extends BasePlayer {

    private static final String TAG = DongYuPlayer.class.getSimpleName();

    private static final String SP_NAME = "movie";

    private LayoutControlHeaderBinding headerBinding;

    private LayoutStatusBarBinding statusBarBinding;

    private LayoutControlBottomBinding bottomBinding;

    private LayoutControlMiddleBinding middleBinding;

    private LayoutVideoErrorBinding errorBinding;

    private TextView toastTextView;

    private ProgressBar smallProgressBar;

    private final Map<VisibilityMode, List<View>> visibilityMap = new HashMap<>();

    private static int DEFAULT_PADDING_BOTTOM;

    private static int FULLSCREEN_PADDING_BOTTOM;

    private static int DEFAULT_PADDING_HORIZONTAL;

    private static int FULLSCREEN_PADDING_HORIZONTAL;

    private static final String SP_VIDEO_SPEED = "video_speed";

    private static final String DEFAULT_LONG_PRESS_SPEED = "3.0";

    private BroadcastReceiver broadcastReceiver;

    private BatteryManager batteryManager;

    private PlayerStateListener playerStateListener;

    private DanmakuView mDanmakuView;

    private DanmakuContext mDanmakuContext;

    private BaseDanmakuParser mBaseDanmakuParser;

    private final List<String> mDanmakuUrlList = new ArrayList<>();

    private ScreenProjectionDialog screenProjectionDialog;

    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public DongYuPlayer(@NonNull Context context) {
        this(context, null);
    }

    public DongYuPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        DEFAULT_PADDING_BOTTOM = DisplayUtilsKt.dp2px(10, getContext());
        FULLSCREEN_PADDING_BOTTOM = DisplayUtilsKt.dp2px(32, getContext());
        DEFAULT_PADDING_HORIZONTAL = DisplayUtilsKt.dp2px(10, getContext());
        FULLSCREEN_PADDING_HORIZONTAL = FULLSCREEN_PADDING_BOTTOM;

        initLayout();
        initViewEvent();

        setPlayerPadding();
    }

    public LayoutControlHeaderBinding getHeaderBinding() {
        return headerBinding;
    }

    public LayoutControlBottomBinding getBottomBinding() {
        return bottomBinding;
    }

    public LayoutControlMiddleBinding getMiddleBinding() {
        return middleBinding;
    }

    public DanmakuView getDanmakuView() {
        return mDanmakuView;
    }

    private void setPlayerPadding() {
        setPlayerPadding(false);
    }

    private void setPlayerPadding(boolean isFullScreen) {
        // int paddingTop = isFullScreen ? getStatusBarHeight() : getStatusBarHeight() + 30;
        int paddingBottom = isFullScreen ? FULLSCREEN_PADDING_BOTTOM : DEFAULT_PADDING_BOTTOM;
        int paddingHorizontal = isFullScreen ? FULLSCREEN_PADDING_HORIZONTAL : DEFAULT_PADDING_HORIZONTAL;

        LinearLayout header = headerBinding.getRoot();
        header.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        ConstraintLayout bottom = bottomBinding.getRoot();
        bottom.setPadding(paddingHorizontal, 0, paddingHorizontal, paddingBottom);
        FrameLayout middle = middleBinding.getRoot();
        middle.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
    }

    private void initLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mDanmakuView = new DanmakuView(getContext());
        mDanmakuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDanmakuView);
        headerBinding = LayoutControlHeaderBinding.inflate(inflater, this, true);
        statusBarBinding = headerBinding.statusBar;
        bottomBinding = LayoutControlBottomBinding.inflate(inflater, this, true);
        middleBinding = LayoutControlMiddleBinding.inflate(inflater, this, true);

        initVisibilityMode(headerBinding.getRoot(), middleBinding.getRoot(), bottomBinding);

        initDanmakuView();
        initScaleMode();
    }

    private void initScaleMode() {
        bottomBinding.scale.setText(getScaleMode());
    }

    private void initDanmakuView() {
        // 设置最大显示行数
        Map<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3); // 滚动弹幕最大显示5行

        // 设置是否禁止重叠
        Map<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.0f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuMargin(20);
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                if (isPlaying()) {
                    mDanmakuView.start(getRealCurrentProgress());
                }
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {
                mDanmakuView.stop();
            }
        });

        mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
            @Override
            public boolean onDanmakuClick(IDanmakus danmakus) {
                // switchController();
                return true;
            }

            @Override
            public boolean onDanmakuLongClick(IDanmakus danmakus) {
                return false;
            }

            @Override
            public boolean onViewClick(IDanmakuView view) {
                // switchController();
                return true;
            }
        });
        // mDanmakuView.showFPS(true);
        mDanmakuView.enableDanmakuDrawingCache(true);

        Boolean isShowDanmaku = SpUtils.INSTANCE.getOrDefault(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, true);
        bottomBinding.danmakuVisible.setImageResource(Boolean.TRUE.equals(isShowDanmaku)
                ? R.drawable.baseline_visibility_24 : R.drawable.baseline_visibility_off_24);
    }

    public DongYuPlayer setDanmakus(List<String> danmakus) {
        mDanmakuUrlList.clear();
        mDanmakuUrlList.addAll(danmakus);
        return this;
    }

    public void startDanmaku() {
        if (mDanmakuUrlList.isEmpty()) {
            return;
        }
        startDanmaku(mDanmakuUrlList.get(0));
    }

    public void startDanmaku(int selection) {
        Log.d(TAG, "startDanmaku, selection: " + selection);
        if (selection <= 0 || selection > mDanmakuUrlList.size()) {
            return;
        }
        startDanmaku(mDanmakuUrlList.get(selection - 1));
    }

    public boolean isShowDanmaku() {
        return mDanmakuView.isShown();
    }

    public boolean isPreparedDanmaku() {
        return mDanmakuView.isPrepared();
    }

    public void showDanmaku() {
        if (!isShowDanmaku() && mDanmakuView.isPrepared()) {
            mDanmakuView.show();
            checkDanmakuCurrentTime();
        }

        SpUtils.INSTANCE.put(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, true);
        bottomBinding.danmakuVisible.setImageResource(R.drawable.baseline_visibility_24);
    }

    public void hideDanmaku() {
        if (isShowDanmaku()) {
            mDanmakuView.hide();
        }
        SpUtils.INSTANCE.put(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, false);
        bottomBinding.danmakuVisible.setImageResource(R.drawable.baseline_visibility_off_24);
    }

    public void startDanmaku(String url) {
        Boolean isShowDanmaku = SpUtils.INSTANCE.getOrDefault(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, true);
        if (Boolean.FALSE.equals(isShowDanmaku)) {
            return;
        }
        Log.d(TAG, "startDanmaku: " + url);
        ILoader loader = MyDanmakuLoader.instance();

        IOKt.ioThread(() -> {
            try {
                loader.load(url);
            } catch (IllegalDataException e) {
                Log.e(TAG, e.toString());
            }
            IDataSource<?> dataSource = loader.getDataSource();

            if (mBaseDanmakuParser != null) {
                mBaseDanmakuParser.release();
            }

            mBaseDanmakuParser = new BiliDanmukuParser();

            mBaseDanmakuParser.load(dataSource);

            mDanmakuView.prepare(mBaseDanmakuParser, mDanmakuContext);
            return null;
        });
    }

    private void checkDanmakuCurrentTime() {
        if (mDanmakuView.isPrepared() && mDanmakuView.getCurrentTime() != getCurrentProgress()) {
            Log.i(TAG, "DanMuKuCurrentTime: " + mDanmakuView.getCurrentTime() + ", currentProgress: " + getCurrentProgress());
            mDanmakuView.seekTo(getCurrentProgress());
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (mDanmakuView != null) {
            mDanmakuView.resume();
            checkDanmakuCurrentTime();
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (mDanmakuView != null) {
            mDanmakuView.release();
        }
        if (mBaseDanmakuParser != null) {
            mBaseDanmakuParser.release();
        }
    }

    public void setTitle(CharSequence title) {
        headerBinding.videoTitle.setText(title);
    }

    public CharSequence getTitle() {
        return headerBinding.videoTitle.getText();
    }

    @Override
    protected View getMaskView(FrameLayout maskLayout) {
        // 返回一个布局用于遮罩
        if (isError()) {
            if (errorBinding == null) {
                errorBinding = LayoutVideoErrorBinding
                        .inflate(LayoutInflater.from(getContext()), maskLayout, false);
                errorBinding.btnRefresh.setOnClickListener(v -> {
                    refresh();
                    hideMaskView();
                });
            }
            return errorBinding.getRoot();
        }
        return super.getMaskView(maskLayout);
    }

    @Override
    protected Drawable getMaskViewBackground() {
        return new ColorDrawable(Color.BLACK);
    }

    private void initVisibilityMode(View header, View middle,
                                    LayoutControlBottomBinding bottom) {
        visibilityMap.put(VisibilityMode.VISIBILITY_ALL,
                Arrays.asList(header, middle, bottom.getRoot()));
        visibilityMap.put(VisibilityMode.VISIBILITY_HEADER,
                Collections.singletonList(header));
        visibilityMap.put(VisibilityMode.VISIBILITY_BOTTOM,
                Collections.singletonList(bottom.getRoot()));
        visibilityMap.put(VisibilityMode.VISIBILITY_LOCK,
                Arrays.asList(middleBinding.btnLock, middleBinding.btnLock2));
        visibilityMap.put(VisibilityMode.VISIBILITY_HEADER_BOTTOM,
                Arrays.asList(header, bottom.getRoot()));
        visibilityMap.put(VisibilityMode.VISIBILITY_EXCLUDE_LOCK,
                Arrays.asList(header, bottom.getRoot(),
                        middleBinding.forward, middleBinding.rewind));
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void setControllerVisibility(int visibility, VisibilityMode visibilityMode) {
        List<View> views = visibilityMap.getOrDefault(visibilityMode, Collections.emptyList());
        assert views != null;
        for (View view : views) {
            setViewVisibility(view, visibility);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewEvent() {
        bottomBinding.btnPlay.setOnClickListener(v -> switchPlay());
        bottomBinding.btnSwitchOrientation.setOnClickListener(v -> switchFullScreen());

        OnClickListener lockListener = v -> switchLockScreen();

        middleBinding.btnLock.setOnClickListener(lockListener);
        middleBinding.btnLock2.setOnClickListener(lockListener);

        headerBinding.backBtn.setOnClickListener(v -> onBackPressed());

        bottomBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 用户主动拖动的
                    setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setSeeking(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSeeking(false);
                hideMessage();
                seekTo();
            }
        });

        middleBinding.rewind.setOnClickListener(v -> seekTo(getCurrentProgress() - 10000));

        middleBinding.forward.setOnClickListener(v -> seekTo(getCurrentProgress() + 10000));

        bottomBinding.speed.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.menu_speed);

            float videoSpeed = getVideoSpeed();

            Menu menu = popupMenu.getMenu();
            getCurrentSpeedForMenu(menu, videoSpeed);

            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                item.setChecked(true);
                float speed = getSpeedForMenu(item);
                setVideoSpeed(speed);
                return true;
            });
        });

        bottomBinding.scale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = createVideoScaleMenu(v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        VideoScaleMode mode = VideoScaleMode.getModeForTitle((String) item.getTitle());
                        setScaleMode(mode);
                        return true;
                    }
                });
                menu.show();
            }
        });

        DanmakuTouchHelper mTouchHelper = null;
        try {
            Field field = mDanmakuView.getClass().getDeclaredField("mTouchHelper");
            field.setAccessible(true);
            Object obj = field.get(mDanmakuView);
            if (obj instanceof DanmakuTouchHelper) {
                mTouchHelper = (DanmakuTouchHelper) obj;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        DanmakuTouchHelper finalMTouchHelper = mTouchHelper;
        AtomicBoolean canTouch = new AtomicBoolean(true);
        mDanmakuView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!canTouch(event.getX(), event.getY())) {
                    canTouch.set(false);
                } else {
                    canTouch.set(true);
                }
            }
            if (!canTouch.get()) {
                return false;
            }
            boolean isEvent = onTouchEvent(event);
            boolean isEventConsumed = false;
            if (finalMTouchHelper != null) {
                isEventConsumed = finalMTouchHelper.onTouchEvent(event);
            }
            return true;
        });

        headerBinding.videoProjection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                screenProjectionDialog = new ScreenProjectionDialog();
                Bundle bundle = new Bundle();
                bundle.putString("url", getIjkMediaPlayer().getDataSource());
                bundle.putString("title", getTitle().toString());
                screenProjectionDialog.setArguments(bundle);
                screenProjectionDialog.show(getActivity().getSupportFragmentManager(), "screenProjection");
            }
        });
    }

    private void setVideoSpeed(float speed) {
        setVideoSpeed(speed, true);
    }

    private void setVideoSpeed(float speed, boolean isSave) {
        if (isSave) {
            SpUtils.INSTANCE.put(SP_NAME, SP_VIDEO_SPEED, speed);
            setSpeed(speed);
        }
        bottomBinding.speed.setText(String.format(Locale.CHINA, "%sX", speed));
    }

    private float getVideoSpeed() {
        Float speed = SpUtils.INSTANCE.getOrDefaultNumber(SP_NAME, SP_VIDEO_SPEED, 1f);
        assert speed != null;
        return speed;
    }

    private float getSpeedForMenu(MenuItem menuItem) {
        assert menuItem.getTitle() != null;
        return Float.parseFloat(menuItem.getTitle()
                .toString().replace("X", ""));
    }

    private void getCurrentSpeedForMenu(Menu menu, float videoSpeed) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (videoSpeed == getSpeedForMenu(menuItem)) {
                menuItem.setChecked(true);
                break;
            }
        }
    }

    private PopupMenu createVideoScaleMenu(View anchor) {
        PopupMenu menu = new PopupMenu(getContext(), anchor);
        for (VideoScaleMode mode : VideoScaleMode.values()) {
            menu.getMenu().add(mode.getTitle());
        }
        return menu;
    }

    public void setScaleMode(VideoScaleMode mode) {
        SpUtils.INSTANCE.put(SP_NAME, SPConfig.PLAYER_SCALE_MODE, mode.name());
        // 重置播放画面
        setSurfaceView();
        bottomBinding.scale.setText(mode.getTitle());
    }

    private String getScaleMode() {
        String mode = SpUtils.INSTANCE.getOrDefault(SP_NAME, SPConfig.PLAYER_SCALE_MODE, VideoScaleMode.AUTO.name());
        return VideoScaleMode.valueOf(mode).getTitle();
    }

    private void getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String hourStr = hour < 10 ? ("0" + hour) : String.valueOf(hour);
        String minuteStr = minute < 10 ? ("0" + minute) : String.valueOf(minute);
        statusBarBinding.tvTime.setText(hourStr + ":" + minuteStr);
    }

    private void getNowBattery(@Nullable Intent intent) {
        int battery;
        if (intent == null) {
            if (batteryManager == null) {
                batteryManager = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
            }
            battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            battery = intent.getIntExtra("level", 0);
        }
        statusBarBinding.tvBattery.setText(battery + "%");
    }

    private void startBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                // 接收广播
                switch (action) {
                    case Intent.ACTION_TIME_TICK:
                        getNowTime();
                        break;
                    case Intent.ACTION_BATTERY_CHANGED:
                        getNowBattery(intent);
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        getNowTime();
        getNowBattery(null);
    }

    private void stopBroadcastReceiver() {
        if (broadcastReceiver == null) {
            return;
        }
        getActivity().unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    private void getPlayerSmallProgressBar() {
        if (!isShowController() && isFullScreen()) {
            boolean isShowSmallProgressBar = Boolean.TRUE
                    .equals(SpUtils.INSTANCE.getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_SMALL_PROGRESS, false));
             if (!isShowSmallProgressBar) {
                 if (smallProgressBar != null) {
                     smallProgressBar.setVisibility(GONE);
                 }
                 return;
             }
            if (smallProgressBar == null) {
                smallProgressBar = LayoutSmallProgressBinding
                        .inflate(LayoutInflater.from(getContext()), this, false).getRoot();
                addView(smallProgressBar);
            }
            smallProgressBar.setVisibility(VISIBLE);
            smallProgressBar.setMax((int) getEndProgress());
        }
    }

    /**
     * 设置是否显示全屏底部小进度条
     */
    public void setSmallProgressBarStatus(boolean isShow) {
        SpUtils.INSTANCE.put(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_SMALL_PROGRESS, isShow);
        if (!isFullScreen()) {
            return;
        }
        if (isShow) {
            hideController();
            getPlayerSmallProgressBar();
        } else if (smallProgressBar != null) {
            smallProgressBar.setVisibility(GONE);
        }
    }

    public void showToast(String msg) {
        ConstraintLayout bottom = bottomBinding.getRoot();
        if (toastTextView == null) {
            toastTextView = LayoutToastBinding.inflate(
                    LayoutInflater.from(getContext()), this, true).getRoot();
            bottom.post(() -> {
                ViewGroup.MarginLayoutParams params = (MarginLayoutParams) toastTextView.getLayoutParams();
                params.bottomMargin = bottom.getHeight() + params.bottomMargin;
                params.leftMargin = bottom.getPaddingLeft();
                toastTextView.setVisibility(VISIBLE);
                toastTextView.setText(msg);
            });
        }
        int visibility = toastTextView.getVisibility();
        if (visibility == VISIBLE) {
            toastTextView.setText(msg);
            return;
        }
        if (bottom.getHeight() != 0) {
            toastTextView.setVisibility(VISIBLE);
            toastTextView.setText(msg);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            toastTextView.setVisibility(GONE);
        }, 5000);
    }

    @Override
    public void onPlayerCreated(BasePlayer player) {
        super.onPlayerCreated(player);
        setVideoSpeed(getVideoSpeed(), false);
        // 此处设置播放进度（数据库中）
        if (playerStateListener != null) {
            playerStateListener.onPlayerCreated(this);
        }
    }

    @Override
    protected void onInsetChanged(int statusBarHeight) {
        super.onInsetChanged(statusBarHeight);
        FrameLayout statusBar = statusBarBinding.getRoot();
        ViewGroup.LayoutParams statusBarLayoutParams = statusBar.getLayoutParams();
        statusBarLayoutParams.height = statusBarHeight + 30;
        statusBar.setLayoutParams(statusBarLayoutParams);
    }

    /**
     * 屏幕方向改变了
     *
     * @param isFullScreen 是否全屏
     */
    @Override
    public void onScreenChanged(boolean isFullScreen) {
        super.onScreenChanged(isFullScreen);

        statusBarBinding.getRoot().setVisibility(isFullScreen ? VISIBLE : INVISIBLE);

        int orientationIcon = isFullScreen ? R.drawable.baseline_zoom_in_map_24
                : R.drawable.baseline_zoom_out_map_24;
        bottomBinding.btnSwitchOrientation.setImageResource(orientationIcon);

        // bottomBinding.playNext.setVisibility(isFullScreen ? View.VISIBLE : View.GONE);
        bottomBinding.speed.setVisibility(isFullScreen ? View.VISIBLE : View.GONE);

        bottomBinding.selections.setVisibility(isFullScreen ? View.VISIBLE : View.GONE);

        bottomBinding.scale.setVisibility(isFullScreen ? VISIBLE : GONE);

        setPlayerPadding(isFullScreen);

        if (isFullScreen) {
            startBroadcastReceiver();
        } else {
            stopBroadcastReceiver();
        }
    }

    @Override
    public void onProgressChanged(long currentProgress) {
        super.onProgressChanged(currentProgress);

        bottomBinding.currentTime.setText(TimeUtilsKt.getTime(currentProgress));

        // 修改进度信息
        bottomBinding.seekbar.setProgress((int) currentProgress, false);

        if (isSeeking()) {
            showMessage(getCurrentAndEndTime());
        }

        if (smallProgressBar != null && smallProgressBar.getVisibility() == VISIBLE) {
            smallProgressBar.setProgress((int) currentProgress);
        }

        if (playerStateListener != null) {
            playerStateListener.onProgressChanged(currentProgress);
        }
    }

    @Override
    public void onVideoPrepared(BasePlayer player) {
        super.onVideoPrepared(player);
        bottomBinding.seekbar.setMax((int) player.getEndProgress());
        bottomBinding.endTime.setText(TimeUtilsKt.getTime(player.getEndProgress()));

        // 视屏准备完成时，等待播放，此时显示弹幕
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.start(player.getRealCurrentProgress());
        }

        if (playerStateListener != null) {
            playerStateListener.onVideoPrepared(this);
        }
    }

    @Override
    public void onPlayStateChanged(int newState) {
        super.onPlayStateChanged(newState);

        // Log.i(TAG, "onPlayStateChanged: " + newState);

        switch (newState) {
            case STATE_PLAYING:
                if (mDanmakuView.isPaused()) {
                    mDanmakuView.resume();
                }
                break;
            case STATE_BUFFERING:
            case STATE_PAUSED:
                mDanmakuView.pause();
                break;
            case STATE_ERROR:
                mDanmakuView.pause();
                // 播放错误处理一些逻辑
                // 用户点击刷新按钮
                // 重新调用play方法进行播放
                Log.e(TAG, "play error");
                showMaskView();
                break;
            case STATE_COMPLETED:
                break;
        }

        // 播放状态
        bottomBinding.btnPlay.setImageResource(newState == STATE_PLAYING ? R.drawable.baseline_pause_24
                : R.drawable.baseline_play_arrow_24);

        bottomBinding.btnPlay.setEnabled(newState == STATE_PAUSED || newState == STATE_PLAYING);

        if (playerStateListener != null) {
            playerStateListener.onPlayStateChanged(newState);
        }
    }

    @Override
    public void setEndProgress(long endProgress) {
        super.setEndProgress(endProgress);
        bottomBinding.seekbar.setMax((int) endProgress);
    }

    @Override
    public void onShowController() {
        super.onShowController();
        setControllerVisibility(VISIBLE, isLockScreen() ?
                VisibilityMode.VISIBILITY_LOCK
                : VisibilityMode.VISIBILITY_ALL);

        if (smallProgressBar != null) {
            smallProgressBar.setVisibility(GONE);
        }
    }

    @Override
    public void onHideController() {
        super.onHideController();
        setControllerVisibility(GONE, isLockScreen() ?
                VisibilityMode.VISIBILITY_LOCK
                : VisibilityMode.VISIBILITY_ALL);
        getPlayerSmallProgressBar();
    }

    private void setControllerVisibility(int visibility) {
        setControllerVisibility(visibility, VisibilityMode.VISIBILITY_ALL);
    }

    @Override
    public void onLockStateChanged(boolean isLockScreen) {
        super.onLockStateChanged(isLockScreen);
        int resId = isLockScreen ? R.drawable.baseline_lock_24 : R.drawable.baseline_lock_open_24;
        middleBinding.btnLock.setImageResource(resId);
        middleBinding.btnLock2.setImageResource(resId);

        setControllerVisibility(isLockScreen ? GONE : VISIBLE, VisibilityMode.VISIBILITY_EXCLUDE_LOCK);
    }

    @Override
    public void onSingleClick() {
        super.onSingleClick();
    }

    @Override
    public void onStartLongClick() {
        super.onStartLongClick();
        float longPressSpeed = Float.parseFloat(Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_LONG_PRESS_SPEED, DEFAULT_LONG_PRESS_SPEED)));
        setSpeed(longPressSpeed);
        float speed = getIjkMediaPlayer().getSpeed(1f);
        showMessage(R.drawable.baseline_fast_forward_24, speed + "倍速播放中");
        // mDanmakuContext.setScrollSpeedFactor(speed);
        if (mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    public void onStopLongClick() {
        super.onStopLongClick();
        setSpeed(getVideoSpeed());
        if (mDanmakuView.isPrepared()) {
            mDanmakuView.seekTo(getRealCurrentProgress());
        }
    }

    @Override
    public void onCancelHorizontalMove() {
        // 屏幕锁定时禁止响应取消事件
        if (isLockScreen())
            return;
        super.onCancelHorizontalMove();
        // 取消拖动
        showMessage("松手取消");
    }

    @Override
    public void onHorizontalMove(float distanceX, boolean isForward) {
        if (isLockScreen()) {
            return;
        }
        super.onHorizontalMove(distanceX, isForward);
        // 水平拖动
        long current = getPlayerProgress();

        long to = (long) (current + (distanceX / (bottomBinding.seekbar.getWidth() * 4L) * getEndProgress()));

        if (to < 0) {
            to = 10;
        } else if (to > getEndProgress()) {
            to = getEndProgress();
        }

        setProgress(to);

        int seekIcon = isForward ? R.drawable.baseline_fast_forward_24
                : R.drawable.baseline_fast_rewind_24;
        showMessage(seekIcon, getCurrentAndEndTime());
    }

    @Override
    public void onHorizontalMoveEnd(boolean isCancel) {
        if (isLockScreen()) {
            return;
        }
        super.onHorizontalMoveEnd(isCancel);
        if (!isCancel) {
            seekTo();
        }
        hideMessage();
    }

    @Override
    public void seekTo(long to) {
        super.seekTo(to);
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.seekTo(to);
            mDanmakuView.pause();
        }
    }

    @Override
    public void setPlayerSize(LayoutParams layoutParams, int width, int height) {
        super.setPlayerSize(layoutParams, width, height);
        String mode = SpUtils.INSTANCE.getOrDefault(SP_NAME, SPConfig.PLAYER_SCALE_MODE, VideoScaleMode.AUTO.name());
        assert mode != null;
        switch (mode) {
            case "AUTO":
                super.setPlayerSize(layoutParams, width, height);
                break;
            case "FULL":
                layoutParams.width = DisplayUtilsKt.getWindowWidth();
                if (isFullScreen()) {
                    layoutParams.height = DisplayUtilsKt.getWindowHeight();
                } else {
                    layoutParams.height = DEFAULT_PLAYER_HEIGHT;
                }
                break;
        }
    }
}
