package com.dongyu.movies.view.player;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dongyu.movies.R;
import com.dongyu.movies.config.SPConfig;
import com.dongyu.movies.databinding.LayoutControlBottomBinding;
import com.dongyu.movies.databinding.LayoutControlHeaderBinding;
import com.dongyu.movies.databinding.LayoutControlMiddleBinding;
import com.dongyu.movies.databinding.LayoutScreenCastBinding;
import com.dongyu.movies.databinding.LayoutSmallProgressBinding;
import com.dongyu.movies.databinding.LayoutStatusBarBinding;
import com.dongyu.movies.databinding.LayoutToastBinding;
import com.dongyu.movies.databinding.LayoutVideoErrorBinding;
import com.dongyu.movies.dialog.BaseAppCompatDialog;
import com.dongyu.movies.dialog.ScreenProjectionDialog;
import com.dongyu.movies.event.OnScreencastListener;
import com.dongyu.movies.event.OnSourceItemChangeListener;
import com.dongyu.movies.event.OnVideoErrorBtnClickListener;
import com.dongyu.movies.model.movie.VideoSource;
import com.dongyu.movies.utils.DisplayUtilsKt;
import com.dongyu.movies.utils.SpUtils;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.utils.UtilsKt;
import com.dongyu.movies.utils.player.AcFunDanmakuParser;
import com.dongyu.movies.utils.player.MyAcFunDanmakuLoader;
import com.dongyu.movies.view.VideoSourceView;
import com.dongyu.movies.view.player.base.BasePlayer;
import com.dongyu.movies.view.player.base.PlayerStateListener;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
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
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;
import master.flame.danmaku.ui.widget.DanmakuTouchHelper;
import master.flame.danmaku.ui.widget.DanmakuView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class DongYuPlayer extends BasePlayer {

    private static final String TAG = DongYuPlayer.class.getSimpleName();

    private static final String SP_NAME = "movie";

    private LayoutControlHeaderBinding headerBinding;

    private LayoutStatusBarBinding statusBarBinding;

    private LayoutControlBottomBinding bottomBinding;

    private LayoutControlMiddleBinding middleBinding;

    private LayoutVideoErrorBinding errorBinding;

    private LayoutScreenCastBinding screenCastBinding;

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

    private OnVideoErrorBtnClickListener errorBtnClickListener;

    private OnSourceItemChangeListener sourceItemChangeListener;

    private DanmakuView mDanmakuView;

    private DanmakuContext mDanmakuContext;

    private BaseDanmakuParser mBaseDanmakuParser;

    private final List<String> mDanmakuUrlList = new ArrayList<>();

    /**
     * 视频源列表
     */
    private List<VideoSource> videoSources;

    /**
     * 当前选择的播放源项
     */
    private VideoSource.Item currentSourceItem;

    /**
     * 是否开启投屏
     */
    private boolean isScreencast = false;

    private ScreenProjectionDialog screenProjectionDialog;

    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public void setErrorBtnClickListener(OnVideoErrorBtnClickListener errorBtnClickListener) {
        this.errorBtnClickListener = errorBtnClickListener;
    }

    /**
     * 监听选集切换事件
     *
     * @param sourceItemChangeListener 回调接口
     */
    public void setSourceItemChangeListener(OnSourceItemChangeListener sourceItemChangeListener) {
        this.sourceItemChangeListener = sourceItemChangeListener;
    }

    public void setCurrentSourceItem(VideoSource.Item currentSourceItem) {
        this.currentSourceItem = currentSourceItem;
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

        if (!isFullScreen && isPortrait()) {
            paddingBottom += getNavigationBarHeight();
        }

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
        int line = Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_DANMAKU_LINE, 3));
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, line);

        // 设置是否禁止重叠
        Map<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        // 由于参数在PreferenceFragment中进行操作，所以保存在默认的 DEFAULT_KEY中
        float alpha = Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_DANMAKU_ALPHA, 10)) / 10f;

        float size = Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_DANMAKU_SIZE, 10)) / 10f;

        int margin = Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_DANMAKU_MARGIN, 20));

        float speed = Objects.requireNonNull(SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_DANMAKU_SPEED, 12)) / 10f;

        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(speed)
                .setScaleTextSize(size)
                .setDanmakuTransparency(alpha)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuMargin(margin);
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
        bottomBinding.danmakuVisible.setText(Boolean.TRUE.equals(isShowDanmaku) ? "弹幕开" : "弹幕关");
    }

    public void setDanmakuLine(int line) {
        // 设置最大显示行数
        Map<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, line);
        mDanmakuContext.setMaximumLines(maxLinesPair);
    }

    public void setDanmakuAlpha(float alpha) {
        mDanmakuContext.setDanmakuTransparency(alpha);
    }

    public void setDanmakuSize(float size) {
        mDanmakuContext.setScaleTextSize(size);
    }

    public void setDanmakuMargin(int margin) {
        mDanmakuContext.setDanmakuMargin(margin);
    }

    public void setDanmakuSpeed(float speed) {
        mDanmakuContext.setScrollSpeedFactor(speed);
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

    public void startDanmaku(int position) {
        Log.d(TAG, "startDanmaku, position: " + position + ", urls: " + mDanmakuUrlList);
        if (position < 0 || position >= mDanmakuUrlList.size()) {
            Log.e(TAG, "弹幕装填异常");
            return;
        }
        startDanmaku(mDanmakuUrlList.get(position));
    }

    public boolean isShowDanmaku() {
        return mDanmakuView.isShown();
    }

    public boolean isPreparedDanmaku() {
        return mDanmakuView.isPrepared();
    }

    public boolean showDanmaku() {
        SpUtils.INSTANCE.put(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, true);
        bottomBinding.danmakuVisible.setText("弹幕开");
        if (mDanmakuUrlList.isEmpty()) {
            Log.e(TAG, "danmakuUrls is empty");
            return false;
        }
        if (!isShowDanmaku()) {
            mDanmakuView.show();
            checkDanmakuCurrentTime();
        }
        return true;
    }

    public void hideDanmaku() {
        if (isShowDanmaku()) {
            mDanmakuView.hide();
        }
        SpUtils.INSTANCE.put(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, false);
        bottomBinding.danmakuVisible.setText("弹幕关");
    }

    public void startDanmaku(String url) {
        Boolean isShowDanmaku = SpUtils.INSTANCE.getOrDefault(SP_NAME, SPConfig.PLAYER_SHOW_DANMAKU, true);
        if (Boolean.FALSE.equals(isShowDanmaku)) {
            return;
        }
        Log.d(TAG, "startDanmaku: " + url);
        ILoader loader = MyAcFunDanmakuLoader.instance();

        new Thread(() -> {
            InputStream inputStream = null;
            try {
                inputStream = new URL(url).openStream();
                String json = IOUtils.getString(inputStream);
                json = new JSONObject(json).getJSONArray("danmuku").toString();
                loader.load(json);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            IDataSource<?> dataSource = loader.getDataSource();

            if (mBaseDanmakuParser != null) {
                mBaseDanmakuParser.release();
            }

            mBaseDanmakuParser = new AcFunDanmakuParser();

            mBaseDanmakuParser.load(dataSource);

            mDanmakuView.prepare(mBaseDanmakuParser, mDanmakuContext);
        }).start();
    }

    private void checkDanmakuCurrentTime() {
        if (mDanmakuView.isPrepared() && mDanmakuView.getCurrentTime() != getCurrentProgress()) {
            Log.i(TAG, "DanMuKuCurrentTime: " + mDanmakuView.getCurrentTime() + ", currentProgress: " + getCurrentProgress());
            mDanmakuView.seekTo(getCurrentProgress());
        }
    }

    /**
     * 设置视频源，用于切换选集功能
     *
     * @param videoSources 视频源列表
     */
    public void setVideoSources(List<VideoSource> videoSources) {
        this.videoSources = videoSources;
    }

    /**
     * 是否开启投屏
     *
     * @return 投屏状态
     */
    public boolean isScreencast() {
        return isScreencast;
    }

    @Override
    public void resume() {
        if (isScreencast) {
            return;
        }
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
        if (isScreencast) {
            screenProjectionDialog.stop();
        }
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
                if (errorBtnClickListener == null) {
                    errorBinding.btnRefresh.setOnClickListener(v -> {
                        refresh();
                        hideMaskView();
                    });
                } else {
                    errorBinding.btnReload.setVisibility(VISIBLE);
                    errorBinding.btnSwitchSource.setVisibility(VISIBLE);
                    errorBinding.btnReload.setOnClickListener(v -> {
                        if (currentSourceItem == null) {
                            return;
                        }
                        errorBtnClickListener.onReloadClick(currentSourceItem);
                    });
                    errorBinding.btnSwitchSource.setOnClickListener(v -> errorBtnClickListener.onSwitchSourceClick());
                    errorBinding.btnRefresh.setOnClickListener(v -> errorBtnClickListener.onRefreshClick());
                }
            }
            return errorBinding.getRoot();
        } else if (isScreencast) {
            if (screenCastBinding == null) {
                screenCastBinding = LayoutScreenCastBinding
                        .inflate(LayoutInflater.from(getContext()), maskLayout, false);
                screenCastBinding.exit.setOnClickListener(v -> {
                    screenProjectionDialog.stop();
                    isScreencast = false;
                    hideMaskView();
                    resume();
                });
            }
            return screenCastBinding.getRoot();
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
                Arrays.asList(header, middle, bottom.getRoot(), middleBinding.btnLock, middleBinding.btnLock2));
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

        bottomBinding.scale.setOnClickListener(v -> {
            PopupMenu menu = createVideoScaleMenu(v);
            menu.setOnMenuItemClickListener(item -> {
                VideoScaleMode mode = VideoScaleMode.getModeForTitle((String) item.getTitle());
                setScaleMode(mode);
                return true;
            });
            menu.show();
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
                canTouch.set(canTouch(event.getX(), event.getY()));
            }
            if (!canTouch.get()) {
                return false;
            }
            onTouchEvent(event);
            // boolean isEventConsumed = false;
            if (finalMTouchHelper != null) {
                finalMTouchHelper.onTouchEvent(event);
            }
            return true;
        });

        headerBinding.videoProjection.setOnClickListener(v -> {
            if (screenProjectionDialog == null) {
                IjkMediaPlayer ijkMediaPlayer = getIjkMediaPlayer();
                if (ijkMediaPlayer == null) {
                    Log.d(TAG, "player is null");
                    return;
                }
                String url = ijkMediaPlayer.getDataSource();
                if (TextUtils.isEmpty(url)) {
                    UtilsKt.showToast("当前视频未加载成功", 2000);
                    return;
                }
                screenProjectionDialog = new ScreenProjectionDialog(v.getContext())
                        .setTitle(getTitle().toString())
                        .setUrl(url);
                screenProjectionDialog.setScreencastListener(new OnScreencastListener() {
                    @Override
                    public void onConnected() {
                        pause();
                        isScreencast = true;
                        showMaskView();
                    }

                    @Override
                    public void onProgress(Long current, Long duration) {

                    }

                    @Override
                    public void onDisconnect() {
                        isScreencast = false;
                        hideMaskView();
                    }
                });
            }
            screenProjectionDialog.show();
        });

        // 选集切换功能
        bottomBinding.selections.setOnClickListener(v -> {
            if (videoSources == null || currentSourceItem == null) {
                return;
            }
            VideoSourceView videoSourceView = new VideoSourceView(getContext());
            videoSourceView.setPagerAutoHeight(false);
            videoSourceView.submitList(videoSources, (item, position) -> {
                if (sourceItemChangeListener != null) {
                    sourceItemChangeListener.onSourceItemChanged(item, position);
                }
            }).setSelection(currentSourceItem);
            // 选集对话框
            BaseAppCompatDialog dialog = new BaseAppCompatDialog(getContext());
            dialog.setContentView(videoSourceView);
            dialog.show();
        });

        // 播放下一集
        bottomBinding.playNext.setOnClickListener(v -> {
            if (currentSourceItem == null || sourceItemChangeListener == null) {
                return;
            }
            String sourceId = currentSourceItem.getParam().getSourceId();
            for (int i = 0; i < videoSources.size(); i++) {
                VideoSource source = videoSources.get(i);
                if (sourceId.equals(source.getId())) {
                    int index = source.getItems().indexOf(currentSourceItem);
                    if (index == -1) {
                        return;
                    }
                    if (++index >= source.getItems().size()) {
                        UtilsKt.showToast("已经是最后一集了", 1000);
                        // 最后一集
                        return;
                    }
                    VideoSource.Item item = source.getItems().get(index);
                    setCurrentSourceItem(item);
                    sourceItemChangeListener.onSourceItemChanged(item, index);
                    break;
                }
            }
        });

        bottomBinding.portrait.setOnClickListener(v -> {
            switchPortrait();
            ImageButton btn = (ImageButton) v;
            if (isPortrait()) {
                btn.setImageResource(R.drawable.baseline_unfold_less_24);
            } else {
                btn.setImageResource(R.drawable.baseline_unfold_more_24);
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
    protected void onInsetChanged(int statusBarHeight, int navigationBarHeight) {
        super.onInsetChanged(statusBarHeight, navigationBarHeight);
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

    /**
     * 重写播放方法
     *
     * @param url 播放地址
     * @return 状态
     */
    @Override
    public boolean play(String url) {
        // 如果正在投屏
        if (isScreencast) {
            screenProjectionDialog.play(url, getTitle().toString());
            showMaskView();
            return false;
        }
        return super.play(url);
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

    @Override
    public void setPortrait(boolean portrait) {
        super.setPortrait(portrait);
        int navigationBarHeight = getNavigationBarHeight();
        if (isFullScreen() || navigationBarHeight <= 0) {
            return;
        }
        ViewGroup bottom = bottomBinding.getRoot();
        int paddingBottom = isFullScreen() ? FULLSCREEN_PADDING_BOTTOM : DEFAULT_PADDING_BOTTOM;
        if (portrait) {
            bottom.setPadding(bottom.getPaddingLeft(), bottom.getPaddingTop(),
                    bottom.getPaddingRight(), navigationBarHeight + paddingBottom);
        } else {
            bottom.setPadding(bottom.getPaddingLeft(), bottom.getPaddingTop(),
                    bottom.getPaddingRight(), paddingBottom);
        }
    }
}
