package com.dongyu.movies.view.player.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dongyu.movies.MoviesApplication;
import com.dongyu.movies.R;
import com.dongyu.movies.databinding.LayoutDyPlayerBinding;
import com.dongyu.movies.utils.DisplayUtilsKt;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.utils.UtilsKt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class BasePlayer extends FrameLayout implements PlayerTouchListener, PlayerStateListener {

    private static final String TAG = BasePlayer.class.getSimpleName();
    /**
     * 默认为亮色状态栏
     */
    private static final boolean LIGHT_STATUS_BAR = false;
    private static final int DEFAULT_STATUS_BAR_HEIGHT = 40;

    private static final int MAX_BUFFING_SIZE = 400000;

    private static final int CONTROLLER_SHOW_TIME = 5000;

    /**
     * 播放器默认的高度
     */
    public static final int DEFAULT_PLAYER_HEIGHT = DisplayUtilsKt
            .dp2px(280, MoviesApplication.context);


    public static final int STATE_NOTHING = -1;

    public static final int STATE_PREPARING = 0;

    public static final int STATE_PLAYING = 1;

    public static final int STATE_PAUSED = 2;

    public static final int STATE_BUFFERING = 3;

    public static final int STATE_COMPLETED = 4;

    public static final int STATE_ERROR = 5;

    private static int MAX_VOLUME;

    /**
     * 主布局
     */
    private LayoutDyPlayerBinding binding;

    /**
     * 宿主Activity
     */
    private AppCompatActivity activity;

    private IjkMediaPlayer ijkMediaPlayer;

    private SurfaceView surfaceView;

    private AudioManager audioManager;

    private int playerHeight = DEFAULT_PLAYER_HEIGHT;

    private boolean isCreateHolder = false;

    /**
     * 是否是全屏状态
     */
    private boolean isFullScreen = false;

    private boolean isLockScreen = false;

    private boolean isShowController = true;

    private boolean isSeeking = false;

    private int playState = STATE_NOTHING;

    /**
     * 开始进度
     */
    private long currentProgress;

    /**
     * 结束进度
     */
    private long endProgress;

    private int statusBarHeight = DEFAULT_STATUS_BAR_HEIGHT;

    private int currentVolume = 0;

    private float currentBrightness = 0;

    private long currentTotalRxBytes;

    private final PlayerTouchHelp playerTouchHelp = new PlayerTouchHelp();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable progressRunnable;

    private Runnable controllerRunnable;

    private Runnable bufferRunnable;

    private Runnable networkSpeedRunnable;

    public BasePlayer(@NonNull Context context) {
        this(context, null);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs,
                      int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initView();
        initWindow();
        handleBackEvent();

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            this.statusBarHeight = statusBarHeight;
            onInsetChanged(statusBarHeight);
            ViewCompat.setOnApplyWindowInsetsListener(v, null);
            return insets;
        });

        playerTouchHelp.setPlayerTouchListener(this);

        // 检查app完整性
        if (!checkApplication(getActivity())) {
            try {
                Class<?> aClass = Class.forName("java.lang.System");
                Method method = aClass.getDeclaredMethod("exit", int.class);
                method.invoke(null, 0);
            } catch (Exception ignored) {
            }
        }

        if (!checkPMProxy()) {
            try {
                Class<?> aClass = Class.forName("java.lang.System");
                Method method = aClass.getDeclaredMethod("exit", int.class);
                method.invoke(null, 0);
            } catch (Exception ignored) {
            }
        }
    }

    private boolean checkApplication(Activity context){
        Application nowApplication = context.getApplication();
        String trueApplicationName = "MoviesApplication";
        String nowApplicationName = nowApplication.getClass().getSimpleName();
        return trueApplicationName.equals(nowApplicationName);
    }

    @SuppressLint("PrivateApi")
    private boolean checkPMProxy(){
        String truePMName = "android.content.pm.IPackageManager$Stub$Proxy";
        String nowPMName = "";
        try {
            // 被代理的对象是 PackageManager.mPM
            PackageManager packageManager = getContext().getPackageManager();
            Field mPMField = packageManager.getClass().getDeclaredField("mPM");
            mPMField.setAccessible(true);
            Object mPM = mPMField.get(packageManager);
            // 取得类名
            nowPMName = mPM.getClass().getName();
        } catch (Exception ignored) {
        }
        // 类名改变说明被代理了
        return truePMName.equals(nowPMName);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isFullScreen) {
            // 全屏状态下就是默认的
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
         int newWidthMeasureSpec = widthMeasureSpec;
         int newHeightMeasureSpec = heightMeasureSpec;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 布局为wrap_content时，需要重新计算高度
        if (heightMode == MeasureSpec.AT_MOST) {
             newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_PLAYER_HEIGHT, MeasureSpec.EXACTLY);
        } else if (heightMode == MeasureSpec.EXACTLY) {
            // 布局为match_parent或指定高度时
            if (playerHeight == DEFAULT_PLAYER_HEIGHT) {
                playerHeight = MeasureSpec.getSize(heightMeasureSpec);
            }
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(playerHeight, MeasureSpec.EXACTLY);
        }
        // 如果宽度为wrap_content,就设置默认的windowWidth
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
             int windowWidth = DisplayUtilsKt.getWindowWidth(activity);
             newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(windowWidth, MeasureSpec.EXACTLY);
        }
        Log.d(TAG, "playerHeight: " + playerHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }

    public IjkMediaPlayer getIjkMediaPlayer() {
        return ijkMediaPlayer;
    }

    protected Drawable getMaskViewBackground() {
        return null;
    }

    protected View getMaskView(FrameLayout maskLayout) {
        return null;
    }

    public void showMaskView() {
        FrameLayout maskLayout = binding.maskLayout;
        View maskView = getMaskView(maskLayout);
        Drawable background = getMaskViewBackground();
        maskLayout.setBackground(background);
        if (maskLayout.getChildCount() == 0 || maskLayout.getChildAt(0) != maskView) {
            setMaskView(maskView);
        }
        maskLayout.setVisibility(VISIBLE);
    }

    public void hideMaskView() {
        FrameLayout maskLayout = binding.maskLayout;
        if (maskLayout.getVisibility() == VISIBLE) {
            binding.maskLayout.setVisibility(GONE);
        }
    }

    public void setMaskView(@Nullable View maskView) {
        clearMaskView();
        if (maskView == null) {
            return;
        }
        binding.maskLayout.addView(maskView);
    }

    public void clearMaskView() {
        FrameLayout maskLayout = binding.maskLayout;
        if (maskLayout.getChildCount() > 0) {
            maskLayout.removeAllViews();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        playerTouchHelp.setPlayerSize(w, h);
    }

    protected void onInsetChanged(int statusBarHeight) {
    }

    @Override
    public void onVideoPrepared(BasePlayer player) {
        showController();
    }

    /**
     * 监听旋转屏幕
     *
     * @param newConfig The new resource configuration.
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isFullScreen = getScreenOrientation(newConfig);
        this.isFullScreen = isFullScreen;
        onScreenChanged(isFullScreen);
    }

    public void onPlayStateChanged(int newState) {
        if (this.playState != newState) {
            this.playState = newState;
            int flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            if (newState == STATE_PLAYING) {
                activity.getWindow().addFlags(flags);
            } else if (newState == STATE_PAUSED || newState == STATE_COMPLETED
                    || newState == STATE_ERROR) {
                activity.getWindow().clearFlags(flags);
            }
        }
    }

    public void onShowController() {

    }

    public void onHideController() {

    }

    @Override
    public void onHorizontalMoveEnd(boolean isCancel) {
        setSeeking(false);
    }

    public void onProgressChanged(long currentProgress) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        return playerTouchHelp.bind(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Log.i(TAG, "onInterceptTouchEvent: " + ev.getAction());
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 重新计算时长
            if (isShowController) {
                showController();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean canTouch(float downX, float downY) {
        return !(downY < statusBarHeight) && !(downY > getHeight() - statusBarHeight)
                && !(downX < statusBarHeight) && !(downX > getWidth() - statusBarHeight);
    }

    @Override
    public void onDown(float downX, float downY) {
        currentVolume = getCurrentVolume();
        currentBrightness = getCurrentBrightness();
    }

    @Override
    public void onSingleClick() {
        Log.i(TAG, "onSingleClick");
        // isShowControl = !isShowControl;
        // 单击事件
        switchController();
    }

    @Override
    public void onDoubleClick() {
        Log.i(TAG, "onDoubleClick");
        if (isLockScreen || isBuffing()) {
            // 屏幕已锁定
            return;
        }
        switchPlay();
    }

    @Override
    public void onStartLongClick() {
        Log.i(TAG, "onStartLongClick");
    }

    @Override
    public void onStopLongClick() {
        Log.i(TAG, "onStopLongClick");
        // setSpeed(1f);
        hideMessage();
    }

    @Override
    public void onHorizontalMove(float distanceX, boolean isForward) {
        setSeeking(true);
    }

    @Override
    public void onCancelHorizontalMove() {
        Log.i(TAG, "onCancelHorizontalMove");
        // setSeeking(false);
        onProgressChanged(getCurrentProgress());
    }

    @Override
    public void onLeftVerticalMove(float distanceY) {
        if (isLockScreen()) {
            return;
        }

        float brightness = currentBrightness;

        brightness -= distanceY / getHeight();

        setBrightness(brightness);

        if (brightness < 0.01f) {
            brightness = 0.01f;
        } else if (brightness > 1.0f) {
            brightness = 1f;
        }

        showMessage(R.drawable.baseline_light_mode_24, "当前亮度: "
                + String.format(Locale.CHINA, "%.2f%%", brightness * 100));
    }

    @Override
    public void onLeftVerticalMoveEnd(boolean isCancel) {
        hideMessage();
    }

    @Override
    public void onRightVerticalMove(float distanceY) {
        if (isLockScreen()) {
            return;
        }
        int playerHeight = getHeight();
        int maxDistanceY = playerHeight / 2;

        float v = distanceY / maxDistanceY;
        int volume = (int) (currentVolume - v * MAX_VOLUME);
        setVolume(volume);

        if (volume > MAX_VOLUME || volume < 0) {
            return;
        }

        volume = (int) ((float) volume / MAX_VOLUME * 100);

        showMessage(R.drawable.baseline_volume_up_24, String.format(Locale.CHINA, "音量: %d%%", volume));
    }

    @Override
    public void onRightVerticalMoveEnd(boolean isCancel) {
        hideMessage();
    }

    public void onScreenChanged(boolean isFullScreen) {
        Window window = activity.getWindow();
        Log.i(TAG, "onScreenChanged: " + this.isFullScreen);
        window.setBackgroundDrawable(getWindowBackground());

        WindowInsetsControllerCompat insetsControllerCompat = WindowCompat
                .getInsetsController(window, window.getDecorView());

        int flags = WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars();

        if (isFullScreen) {
            insetsControllerCompat.hide(flags);
        } else {
            insetsControllerCompat.show(flags);
        }

        insetsControllerCompat.setSystemBarsBehavior(
                isFullScreen ? WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        : WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

        getLayoutParams().height = getPlayerHeight();

        setSurfaceView();
    }

    /**
     * 获取window背景
     *
     * @return Drawable 背景
     */
    private Drawable getWindowBackground() {
        Drawable windowDrawable;
        if (isFullScreen) {
            windowDrawable = new ColorDrawable(Color.BLACK);
        } else {
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
            windowDrawable = ContextCompat.getDrawable(getContext(), typedValue.resourceId);
        }
        return windowDrawable;
    }

    public int getPlayerHeight() {
        return isFullScreen ? LayoutParams.MATCH_PARENT : playerHeight;
    }

    private boolean getScreenOrientation(Configuration configuration) {
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    protected LayoutDyPlayerBinding getBinding() {
        return binding;
    }

    protected AppCompatActivity getActivity() {
        return activity;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public boolean isLockScreen() {
        return isLockScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        if (isFullScreen == fullScreen)
            return;
        isFullScreen = fullScreen;
        setOrientation(fullScreen);
    }

    public void switchFullScreen() {
        setFullScreen(!isFullScreen);
    }

    public void switchLockScreen() {
        setLockScreen(!isLockScreen);
    }

    public void setLockScreen(boolean lockScreen) {
        this.isLockScreen = lockScreen;
        onLockStateChanged(lockScreen);
    }

    public void onLockStateChanged(boolean isLockScreen) {
    }

    public void setShowController(boolean showController) {
        isShowController = showController;
    }

    public boolean isShowController() {
        return isShowController;
    }

    public boolean isPlaying() {
        return ijkMediaPlayer != null && ijkMediaPlayer.isPlaying();
    }

    public boolean isPause() {
        return !isPlaying();
    }

    public boolean isStop() {
        return ijkMediaPlayer == null;
    }

    public boolean isPlayable() {
        return ijkMediaPlayer != null && ijkMediaPlayer.isPlayable();
    }

    public boolean isError() {
        return playState == STATE_ERROR;
    }

    public void playError() {
        playState = STATE_ERROR;
        stop();
    }

    public boolean isCompletion() {
        if (getEndProgress() == 0) {
            return false;
        }
        if (ijkMediaPlayer == null) {
            return true;
        }
        return playState == STATE_COMPLETED
                || ijkMediaPlayer.getCurrentPosition() == ijkMediaPlayer.getDuration();
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public long getRealCurrentProgress() {
        return ijkMediaPlayer != null ? ijkMediaPlayer.getCurrentPosition() : 0;
    }

    public long getPlayerProgress() {
        return ijkMediaPlayer == null ? 0 : ijkMediaPlayer.getCurrentPosition();
    }

    public long getEndProgress() {
        return endProgress;
    }

    public void setSeeking(boolean seeking) {
        if (isSeeking != seeking) {
            isSeeking = seeking;
        }
    }

    public boolean isSeeking() {
        return isSeeking;
    }

    public boolean isBuffing() {
        return playState == STATE_BUFFERING;
    }

    public void setSpeed(float speed) {
        if (ijkMediaPlayer == null) {
            return;
        }
        ijkMediaPlayer.setSpeed(speed);
    }

    public void setVolume(int volume) {
        if (currentVolume == volume) {
            return;
        }

        if (volume > MAX_VOLUME) {
            volume = MAX_VOLUME;
        } else if (volume < 0) {
            volume = 0;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                volume, AudioManager.FLAG_PLAY_SOUND);
    }

    public void setBrightness(float brightness) {
        if (currentBrightness == brightness) {
            return;
        }

        if (brightness > 1) {
            brightness = 1;
        } else if (brightness < 0.01f) {
            brightness = 0.01f;
        }

        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        activity.getWindow().setAttributes(layoutParams);
    }

    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    /**
     * 获取当前音量
     *
     * @return 音量 0-MAX_VOLUME
     */
    public int getCurrentVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取当前亮度
     *
     * @return 亮度 0 - 1
     */
    public float getCurrentBrightness() {
        float brightness = activity.getWindow().getAttributes().screenBrightness;

        if (brightness == -1) {
            try {
                brightness = Settings.System.getFloat(
                        getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                if (brightness > 255) {
                    brightness /= 2000f;
                }
            } catch (Exception e) {
                Log.e(TAG, "getCurrentBrightness: " + e.getMessage());
                brightness = 0.5f;
            }
        }
        return brightness;
    }

    public void refresh() {
        if (ijkMediaPlayer == null) {
            return;
        }
        String url = ijkMediaPlayer.getDataSource();
        stop();
        play(url);
    }

    public boolean play(String url) {
        if (url == null) {
            return false;
        }
        if (ijkMediaPlayer != null) {
            stop();
        }
        // 构建新的实例
        if (!createIjkPlayer(url)) {
            return false;
        }
        Log.d(TAG, "play: " + url + ", player: " + ijkMediaPlayer);
        // 准备播放
        ijkMediaPlayer.prepareAsync();
        onPlayStateChanged(STATE_PREPARING);
        showLoading();
        return true;
    }

    public void switchPlay() {
        if (!isPlayable()) {
            Log.w(TAG, "switchPlay: ijkMediaPlayer is null");
            return;
        }
        if (isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    public void resume() {
        if (ijkMediaPlayer == null) {
            Log.w(TAG, "resume: ijkMediaPlayer is null");
            return;
        }
        if (isCompletion()) {
            play(ijkMediaPlayer.getDataSource());
            return;
        }
        if (isPause()) {
            ijkMediaPlayer.start();
            startProgressTimer();
            onPlayStateChanged(STATE_PLAYING);
        }
    }

    public void pause() {
        if (isPlaying()) {
            ijkMediaPlayer.pause();
            stopProgressTimer();
            onPlayStateChanged(STATE_PAUSED);
        }
    }

    public void stop() {
        if (ijkMediaPlayer == null) {
            Log.w(TAG, "stop: ijkMediaPlayer is null");
            return;
        }
        ijkMediaPlayer.stop();
        ijkMediaPlayer.release();
        ijkMediaPlayer.setDisplay(null);
        ijkMediaPlayer = null;
        stopProgressTimer();
        onPlayStateChanged(STATE_NOTHING);
    }

    public void seekTo() {
        seekTo(getCurrentProgress());
    }

    public void seekTo(long to) {
        if (ijkMediaPlayer == null) {
            Log.w(TAG, "seekTo: ijkMediaPlayer is null");
            return;
        }

        long end = getEndProgress();

        if (endProgress <= 0) {
            Log.w(TAG, "seekTo: endProgress <= 0");
            return;
        }

        if (to <= 0) {
            to = 1;
        } else if (to > end) {
            to = end;
        }

        setProgress(to);

        Log.i(TAG, "seekTo: to = " + to);

        if (to == end) {
            onPlayStateChanged(STATE_COMPLETED);
            // 这里处理完成时的回调
            return;
        }

        ijkMediaPlayer.seekTo(to);

    }

    public void setProgress(long currentProgress) {
        if (endProgress <= 0 || currentProgress > endProgress) {
            Log.w(TAG, "setProgress: endProgress is less than 0 or" +
                    " currentProgress is greater than endProgress");
            return;
        }

        boolean changed = false;

        if (this.currentProgress == currentProgress) {
            Log.w(TAG, "setProgress: currentProgress is equal to the current value");
        } else {
            changed = true;
            this.currentProgress = currentProgress;
        }

        if (changed) {
            onProgressChanged(currentProgress);
        }
    }

    public void setEndProgress(long endProgress) {
        this.endProgress = endProgress;
    }

    public void showMessage(String content) {
        showMessage(0, content);
    }

    public void showMessage(int icon, String content) {
        Drawable drawable = null;
        if (icon != 0) {
            drawable = ContextCompat.getDrawable(getContext(), icon);
        }

        if (binding.videoMessage.getVisibility() != VISIBLE) {
            binding.videoMessage.setVisibility(VISIBLE);
        }

        Drawable currentDrawable = binding.videoMessage.getCompoundDrawables()[0];
        if (drawable != currentDrawable) {
            binding.videoMessage.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        binding.videoMessage.setText(content);
    }

    public void hideMessage() {
        binding.videoMessage.setVisibility(INVISIBLE);
    }

    private long getNowNetworkSpeed() {
        // 这里执行获取网速
        int uid = Process.myUid();
        return TrafficStats.getUidRxBytes(uid);
    }

    public void showLoading() {
        binding.progressBar.setVisibility(VISIBLE);
        binding.tvNetworkSpeed.setVisibility(VISIBLE);
        binding.loadingTip.setVisibility(VISIBLE);

        if (networkSpeedRunnable == null) {
            networkSpeedRunnable = new Runnable() {
                @Override
                public void run() {
                    // 这里执行获取网速
                    long nowBytes = getNowNetworkSpeed();
                    long bytes = nowBytes - currentTotalRxBytes;
                    binding.tvNetworkSpeed.setText(UtilsKt.formatBytes(bytes) + "/s");
                    currentTotalRxBytes = nowBytes;
                    mHandler.postDelayed(this, 1000);
                }
            };
        }

        currentTotalRxBytes = getNowNetworkSpeed();
        binding.tvNetworkSpeed.setText(null);
        mHandler.removeCallbacks(networkSpeedRunnable);
        mHandler.postDelayed(networkSpeedRunnable, 1000);
    }

    public void hideLoading() {
        binding.progressBar.setVisibility(INVISIBLE);
        binding.tvNetworkSpeed.setVisibility(INVISIBLE);
        binding.loadingTip.setVisibility(INVISIBLE);
        if (networkSpeedRunnable != null) {
            mHandler.removeCallbacks(networkSpeedRunnable);
            networkSpeedRunnable = null;
        }
    }

    /**
     * 初始化
     */
    private void init() {
        activity = (AppCompatActivity) getContext();
        // 设置背景
        setBackgroundColor(0xff000000);
        binding = LayoutDyPlayerBinding.inflate(LayoutInflater.from(getContext()), this);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initView() {
        surfaceView = binding.surfaceView;
        surfaceView.setZOrderOnTop(false);
        surfaceView.setZ(0f);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated");
                if (ijkMediaPlayer != null) {
                    ijkMediaPlayer.setDisplay(holder);
                }
                isCreateHolder = true;
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed");
            }
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void initWindow() {
        Window window = activity.getWindow();
        WindowInsetsControllerCompat windowInsetsController = WindowCompat
                .getInsetsController(window, window.getDecorView());

        // 亮色状态栏
        windowInsetsController.setAppearanceLightStatusBars(LIGHT_STATUS_BAR);

        // android9适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }

        // 屏幕常亮
        // window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 关闭自动旋转屏幕
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void startProgressTimer() {
        if (progressRunnable != null || getEndProgress() == 0) {
            Log.w(TAG, "startProgressTimer: runnable is not null");
            return;
        }
        progressRunnable = () -> {
            mHandler.removeCallbacks(progressRunnable);
            mHandler.postDelayed(progressRunnable, 1000);
            if (ijkMediaPlayer == null || isSeeking || isBuffing()) {
                return;
            }
            setProgress(ijkMediaPlayer.getCurrentPosition());
        };
        // 每隔一秒运行
        mHandler.postDelayed(progressRunnable, 1000);
    }

    private void stopProgressTimer() {
        if (progressRunnable != null) {
            mHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    /**
     * 开始缓冲
     */
    private void startBuffering() {
        if (bufferRunnable != null) {
            return;
        }
        bufferRunnable = () -> {
            mHandler.removeCallbacks(bufferRunnable);
            if (!isBuffing()) {
                bufferRunnable = null;
                return;
            }
            long packets = ijkMediaPlayer.getVideoCachedPackets();
            int progress = (int) (ijkMediaPlayer.getVideoCachedBytes() * 100 / MAX_BUFFING_SIZE);
            if (progress > 100) {
                bufferRunnable = null;
                return;
            }
            Log.i(TAG, "buffing progress: " + progress + ", packets: " + packets);
            mHandler.postDelayed(bufferRunnable, 500);
        };
        mHandler.postDelayed(bufferRunnable, 500);
    }

    public void switchController() {
        if (isShowController) {
            hideController();
        } else {
            showController();
        }
    }

    public final void showController() {
        if (controllerRunnable != null) {
            mHandler.removeCallbacks(controllerRunnable);
        }
        if (!isShowController) {
            isShowController = true;
            onShowController();
        }
        // mHandler.removeCallbacks(controllerRunnable);
        controllerRunnable = this::hideController;
        mHandler.postDelayed(controllerRunnable, CONTROLLER_SHOW_TIME);
    }

    public final void hideController() {
        if (isSeeking) {
            // 拖动进度时不进行隐藏
            showController();
            return;
        }
        isShowController = false;
        if (controllerRunnable != null) {
            mHandler.removeCallbacks(controllerRunnable);
        }
        onHideController();
    }

    private void handleBackEvent() {
        activity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        });
    }

    public void onBackPressed() {
        if (isFullScreen) {
            // 退出全屏
            setFullScreen(false);
        } else {
            activity.finish();
        }
    }

    private void setOrientation(boolean isFullScreen) {
        int orientation;
        if (isFullScreen) {
            orientation = isLockScreen ? ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                    : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        activity.setRequestedOrientation(orientation);
    }

    private boolean createIjkPlayer(String url) {
        try {
            ijkMediaPlayer = new IjkMediaPlayer();
            ijkMediaPlayer.setLogEnabled(false);
            // ijkMediaPlayer.setLogEnabled(BuildConfig.DEBUG);
            ijkMediaPlayer.setDataSource(url);
            // 循环播放关闭
            ijkMediaPlayer.setLooping(false);
            SurfaceHolder holder = surfaceView.getHolder();
            if (isCreateHolder) {
                Log.i(TAG, "surfaceView is created");
                ijkMediaPlayer.setDisplay(holder);
            }
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            // 播放重连次数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", MAX_BUFFING_SIZE);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);

            setPlayerEvent();

            onPlayerCreated(this);
            // onPlayerCreated(ijkMediaPlayer);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "create play error: " + e.getMessage());
            return false;
        }
    }

    private void setPlayerEvent() {
        ijkMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
            setEndProgress(ijkMediaPlayer.getDuration());
            // 视频已准备播放
            // iMediaPlayer.start();
            hideLoading();
            // 开始定时任务监听进度信息
            startProgressTimer();
            onPlayStateChanged(STATE_PLAYING);
            onVideoPrepared(this);
            if (getCurrentProgress() == 0) {
                iMediaPlayer.start();
            }
            // setSurfaceView();
        });

        ijkMediaPlayer.setOnVideoSizeChangedListener((mp, width, height, sar_num, sar_den) -> setSurfaceView());

        ijkMediaPlayer.setOnInfoListener((iMediaPlayer, what, i1) -> {
            // Log.i(TAG, "OnInfoListener: " + what + " , i1: " + i1);
            if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // 开始缓冲时
                onPlayStateChanged(STATE_BUFFERING);
                showLoading();
                // startBuffering();
            } else if (what == IjkMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START) {
                // 准备完成时
                hideLoading();
                // onPlayStateChanged(STATE_PLAYING);
            } else if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 缓冲完成时
                onPlayStateChanged(STATE_PLAYING);
                hideLoading();
                // 如果缓冲前是已暂停状态，则不进行恢复播放
                if (isPause()) return false;
                resume();
            }
            return false;
        });

        ijkMediaPlayer.setOnCompletionListener(iMediaPlayer -> {

            Log.d(TAG, "current: " + iMediaPlayer.getCurrentPosition()
                    + ", duration: " + iMediaPlayer.getDuration());

            if (isError()) {
                Log.e(TAG, "setOnCompletionListener: video play error");
                return;
            }
            if ((int) (getCurrentProgress() * 100 / getEndProgress()) <= 95) {
                // 播放失败了
                onPlayStateChanged(STATE_ERROR);
                return;
            }
            // pause();
            // 手动设置进度
            setProgress(getEndProgress());
            stopProgressTimer();
            // 播放完成了
            onPlayStateChanged(STATE_COMPLETED);
            Log.i(TAG, "Video play completion");
        });

        ijkMediaPlayer.setOnErrorListener((iMediaPlayer, i, i1) -> {
            hideLoading();
            onPlayStateChanged(STATE_ERROR);
            stopProgressTimer();
            // 显示错误
            Log.e(TAG, "video play error" + " param1:" + i + " param2: " + i1);
            return false;
        });
    }

    public void setSurfaceView() {
        setSurfaceSize(0, 0, Gravity.CENTER);
    }

    public void setPlayerSize(FrameLayout.LayoutParams layoutParams, @Px int width, @Px int height) {

        int videoHeight;
        int videoWidth;

        int windowWidth = DisplayUtilsKt.getWindowWidth(activity) - width;
        int windowHeight = DisplayUtilsKt.getWindowHeight(activity) - height;

        if (ijkMediaPlayer != null) {
            videoHeight = ijkMediaPlayer.getVideoHeight();
            videoWidth = ijkMediaPlayer.getVideoWidth();
            Log.d(TAG, "videoHeight: " + videoHeight + ", videoWidth: " + videoWidth);
        } else {
            videoHeight = windowHeight;
            videoWidth = windowWidth;
        }

        if (videoHeight == 0 || videoWidth == 0) {
            videoWidth = windowWidth;
            videoHeight = playerHeight;
        }

        float ww = windowWidth / (float) videoWidth;
        float wh = windowHeight / (float) videoHeight;

        float scale = Math.min(ww, wh);

        int surfaceWidth = (int) (videoWidth * scale);
        int surfaceHeight = (int) (videoHeight * scale);

        if (surfaceWidth != layoutParams.width) {
            layoutParams.width = surfaceWidth;
        }

        if (surfaceHeight != layoutParams.height) {
            layoutParams.height = surfaceHeight;
        }

        // surfaceView.getHolder().setFixedSize(layoutParams.width, layoutParams.height);
    }

    public void setSurfaceSize(@Px int width, @Px int height, @GravityInt int gravity) {
        FrameLayout.LayoutParams layoutParams = (LayoutParams) surfaceView.getLayoutParams();
        layoutParams.gravity = gravity;

        setPlayerSize(layoutParams, width, height);

        requestLayout();
    }

    public String getTime(long time) {
        return TimeUtilsKt.getTime(time);
    }

    public String getCurrentTime() {
        return getTime(getCurrentProgress());
    }

    public String getEndTime() {
        return getTime(getEndProgress());
    }

    public String getCurrentAndEndTime() {
        return getCurrentTime() + "/" + getEndTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        playerTouchHelp.destroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
