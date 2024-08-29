package com.dongyu.movies.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dongyu.movies.databinding.DialogScrrenProjectionBinding;
import com.dongyu.movies.utils.UtilsKt;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wanban.screencast.ScreenCastUtils;
import com.wanban.screencast.listener.OnDeviceConnectListener;
import com.wanban.screencast.listener.OnSearchedDevicesListener;
import com.wanban.screencast.listener.OnVideoProgressUpdateListener;
import com.wanban.screencast.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenProjectionDialog extends BottomSheetDialogFragment {

    private static final String TAG = "ScreenProjectionDialog";

    private DialogScrrenProjectionBinding binding;

    private final List<DeviceModel> deviceInfoList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogScrrenProjectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String url = requireArguments().getString("url");
        String title = requireArguments().getString("title");

        binding.scanResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.scanResult.setAdapter(new RecyclerView.Adapter<ViewHolder>() {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView textView = new TextView(parent.getContext());
                ViewHolder viewHolder = new ViewHolder(textView);
                textView.setPadding(20, 20, 20, 20);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = deviceInfoList.get(viewHolder.getAbsoluteAdapterPosition()).getName();
                        assert name != null;
                        ScreenCastUtils.INSTANCE.connectDevice(name, new OnDeviceConnectListener() {
                            @Override
                            public void onDeviceConnect() {
                                UtilsKt.showToast("已连接", 2000);
                            }

                            @Override
                            public void onDeviceDisConnect() {
                                UtilsKt.showToast("已断开连接", 2000);
                            }
                        });
                    }
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

        OnSearchedDevicesListener searchedDevicesListener = new OnSearchedDevicesListener() {
            @Override
            public void onSearchedDevices(@NonNull List<DeviceModel> devices) {
                // 扫描设备
                deviceInfoList.clear();
                deviceInfoList.addAll(devices);
            }
        };

        ScreenCastUtils.INSTANCE.startBrowser(searchedDevicesListener);

        binding.scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenCastUtils.INSTANCE.startBrowser(searchedDevicesListener);
            }
        });

        binding.pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenCastUtils.INSTANCE.pause();
            }
        });

        binding.resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenCastUtils.INSTANCE.play(url, title, new OnVideoProgressUpdateListener() {
                    @Override
                    public void onVideoProgressUpdate(long time) {

                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        ScreenCastUtils.INSTANCE.stop();
    }
}
