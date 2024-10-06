package com.dongyu.movies.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dongyu.movies.R;
import com.dongyu.movies.databinding.DialogScrrenProjectionBinding;
import com.dongyu.movies.event.OnScreencastListener;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.utils.UtilsKt;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.wanban.screencast.ScreenCastUtils;
import com.wanban.screencast.listener.OnDeviceConnectListener;
import com.wanban.screencast.listener.OnSearchedDevicesListener;
import com.wanban.screencast.listener.OnVideoProgressUpdateListener;
import com.wanban.screencast.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenProjectionDialog extends BottomSheetDialog implements OnSearchedDevicesListener, OnVideoProgressUpdateListener {

    private static final String TAG = "ScreenProjectionDialog";

    private DialogScrrenProjectionBinding binding;

    private OnScreencastListener screencastListener;
    private final List<DeviceModel> deviceInfoList = new ArrayList<>();

    private String url;
    private String title;

    private boolean isSeeking = false;

    public ScreenProjectionDialog setUrl(String url) {
        this.url = url;
        return this;
    }

    public ScreenProjectionDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ScreenProjectionDialog(@NonNull Context context) {
        super(context);
    }

    public void setScreencastListener(OnScreencastListener screencastListener) {
        this.screencastListener = screencastListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogScrrenProjectionBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

        // binding.duration.setText(TimeUtilsKt.getTime(duration));
        // binding.progress.setMax((int) duration);

        binding.scanResult.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.scanResult.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.scanResult.setAdapter(new RecyclerView.Adapter<ViewHolder>() {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView textView = new TextView(parent.getContext());
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -2);
                textView.setLayoutParams(layoutParams);
                textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(parent.getContext(), R.drawable.baseline_tv_24), null, null, null);
                textView.setCompoundDrawablePadding(10);
                ViewHolder viewHolder = new ViewHolder(textView);
                textView.setPadding(20, 20, 20, 20);
                textView.setOnClickListener(v -> {
                    String name = deviceInfoList.get(viewHolder.getAbsoluteAdapterPosition()).getName();
                    assert name != null;
                    UtilsKt.showToast("开始连接: " + name, 1000);
                    ScreenCastUtils.INSTANCE.connectDevice(name, new OnDeviceConnectListener() {
                        @Override
                        public void onDeviceConnect() {
                            ScreenCastUtils.INSTANCE.play(url, title, ScreenProjectionDialog.this);
                            UtilsKt.showToast(name + "已连接", 2000);
                            if (screencastListener != null) {
                                screencastListener.onConnected();
                            }
                            dismiss();
                        }

                        @Override
                        public void onDeviceDisConnect() {
                            if (screencastListener != null) {
                                screencastListener.onDisconnect();
                            }
                            UtilsKt.showToast(name + "已断开连接", 2000);
                        }
                    });
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(deviceInfoList.get(position).getName());
            }

            @Override
            public int getItemCount() {
                return deviceInfoList.size();
            }
        });

        binding.scan.setOnClickListener(v -> ScreenCastUtils.INSTANCE.startBrowser(this));

        binding.pause.setOnClickListener(v -> ScreenCastUtils.INSTANCE.pause());

        binding.resume.setOnClickListener(v -> resume());

        binding.exit.setOnClickListener(v -> {
            stop();
            if (screencastListener != null) {
                screencastListener.onDisconnect();
            }
            dismiss();
        });

        binding.progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.currentTime.setText(TimeUtilsKt.getTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                seekTo(seekBar.getProgress());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: startBrowser");
        ScreenCastUtils.INSTANCE.startBrowser(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void resume() {
        ScreenCastUtils.INSTANCE.resume();
    }

    /**
     * 非正常停止，不会停止电视播放，只是停止连接
     */
    public void stop() {
        ScreenCastUtils.INSTANCE.stopBrowser();
        ScreenCastUtils.INSTANCE.disconnect();
    }

    public void seekTo(long to) {
        ScreenCastUtils.INSTANCE.seekTo((int) to);
    }

    public void pause() {
        ScreenCastUtils.INSTANCE.pause();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * 外部调用播放
     * @param url 地址
     * @param title 标题
     */
    public ScreenProjectionDialog play(String url, String title) {
        if (TextUtils.isEmpty(url)) {
            UtilsKt.showToast("视频未加载完成", 1000);
            return this;
        }
        this.url = url;
        this.title = title;
        binding.progress.setProgress(0);
        binding.progress.setMax(0);
        ScreenCastUtils.INSTANCE.play(url, title, this);
        return this;
    }

    /**
     * 已扫描设备添加
     * @param devices 机型列表
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSearchedDevices(@NonNull List<DeviceModel> devices) {
        // 扫描设备
        deviceInfoList.clear();
        deviceInfoList.addAll(devices);
        binding.scanResult.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onVideoProgressUpdate(long time, long duration) {
        if (isSeeking) {
            return;
        }
        if (screencastListener != null) {
            screencastListener.onProgress(time, duration);
        }
         binding.currentTime.setText(TimeUtilsKt.getTime(time));
         binding.progress.setProgress((int) time);
         binding.progress.setMax((int) duration);
         binding.duration.setText(TimeUtilsKt.getTime(duration));
    }
}
