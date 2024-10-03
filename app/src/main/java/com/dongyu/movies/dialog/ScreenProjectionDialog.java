package com.dongyu.movies.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dongyu.movies.R;
import com.dongyu.movies.databinding.DialogScrrenProjectionBinding;
import com.dongyu.movies.utils.TimeUtilsKt;
import com.dongyu.movies.utils.UtilsKt;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wanban.screencast.ScreenCastUtils;
import com.wanban.screencast.dlna.DLANUtils;
import com.wanban.screencast.listener.OnDeviceConnectListener;
import com.wanban.screencast.listener.OnSearchedDevicesListener;
import com.wanban.screencast.listener.OnVideoProgressUpdateListener;
import com.wanban.screencast.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenProjectionDialog extends BottomSheetDialog implements OnSearchedDevicesListener, OnVideoProgressUpdateListener {

    private static final String TAG = "ScreenProjectionDialog";

    private DialogScrrenProjectionBinding binding;

    private boolean isConnected = false;
    private View.OnClickListener nextSelectionClickListener;
    private final List<DeviceModel> deviceInfoList = new ArrayList<>();

    private String url;
    private String title;
    private long duration;

    public ScreenProjectionDialog setUrl(String url) {
        this.url = url;
        return this;
    }

    public ScreenProjectionDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ScreenProjectionDialog setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public ScreenProjectionDialog(@NonNull Context context) {
        super(context);
    }

    public void setNextSelectionClickListener(View.OnClickListener nextSelectionClickListener) {
        this.nextSelectionClickListener = nextSelectionClickListener;
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
                            isConnected = true;
                            ScreenCastUtils.INSTANCE.play(url, title, ScreenProjectionDialog.this);
                            UtilsKt.showToast(name + "已连接", 2000);
                        }

                        @Override
                        public void onDeviceDisConnect() {
                            isConnected = false;
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

        binding.resume.setOnClickListener(v -> ScreenCastUtils.INSTANCE.resume());

        binding.exit.setOnClickListener(v -> {
            ScreenCastUtils.INSTANCE.stop();
            ScreenCastUtils.INSTANCE.stopBrowser();
        });

        binding.next.setOnClickListener(nextSelectionClickListener);
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
        ScreenCastUtils.INSTANCE.stopBrowser();
        ScreenCastUtils.INSTANCE.disconnect();
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
    public ScreenProjectionDialog play(String url, String title, long duration) {
        if (TextUtils.isEmpty(url)) {
            UtilsKt.showToast("视频未加载完成", 1000);
            return this;
        }
        this.url = url;
        this.title = title;
        // binding.duration.setText(TimeUtilsKt.getTime(duration));
        // binding.progress.setMax((int) duration);
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
    public void onVideoProgressUpdate(long time) {
        // binding.currentTime.setText(TimeUtilsKt.getTime(time));
        // binding.progress.setProgress((int) time);
    }
}
