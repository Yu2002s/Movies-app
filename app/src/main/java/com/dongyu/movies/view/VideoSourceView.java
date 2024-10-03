package com.dongyu.movies.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.dongyu.movies.databinding.ItemListSourceBinding;
import com.dongyu.movies.databinding.LayoutVideoSourceBinding;
import com.dongyu.movies.event.OnSourceItemChangeListener;
import com.dongyu.movies.model.movie.VideoSource;
import com.dongyu.movies.model.parser.PlayParam;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoSourceView extends LinearLayoutCompat {

    private static final String TAG = VideoSourceView.class.getSimpleName();

    private TabLayout tab;

    private ViewPager vp;

    /**
     * 是否是最后的源项
     */
    private boolean isLastSourceItem = false;

    /**
     * 换源次数
     */
    private int switchSourceCount = 0;

    private int insertBottom = 0;

    /**
     * 当前播放源项
     */
    private VideoSource.Item currentSourceItem;

    private VideoSource.Item beforeSourceItem;

    /**
     * 当前源所在的位置
     */
    private int currentSourcePosition = -1;

    private final List<VideoSource> videoSources = new ArrayList<>();

    private final List<RecyclerView> sourceViews = new ArrayList<>();

    private final SourcePageAdapter sourcePageAdapter = new SourcePageAdapter();

    private OnSourceItemChangeListener sourceItemChangeListener;

    public VideoSourceView(@NonNull Context context) {
        this(context, null);
    }

    public VideoSourceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSourceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setSourceItemChangeListener(OnSourceItemChangeListener sourceItemChangeListener) {
        this.sourceItemChangeListener = sourceItemChangeListener;
    }

    public VideoSourceView submitList(List<VideoSource> list, boolean init, OnSourceItemChangeListener listener) {
        this.sourceItemChangeListener = listener;
        submitList(list, init);
        return this;
    }

    public VideoSourceView submitList(List<VideoSource> list, OnSourceItemChangeListener listener) {
        submitList(list, false, listener);
        return this;
    }

    public VideoSourceView submitList(List<VideoSource> list, boolean init) {
        if (list.isEmpty()) {
            return this;
        }
        setVisibility(VISIBLE);
        this.videoSources.clear();
        this.videoSources.addAll(list);
        this.sourceViews.clear();
        for (VideoSource videoSource : list) {
            RecyclerView rv = getRecyclerView(videoSource);
            sourceViews.add(rv);
        }
        currentSourcePosition = 0;
        sourcePageAdapter.notifyDataSetChanged();
        if (init) {
            vp.post(this::initSelection);
        }
        return this;
    }

    private @NonNull RecyclerView getRecyclerView(VideoSource videoSource) {
        RecyclerView rv = new RecyclerView(getContext());
        rv.setClipToPadding(false);
        rv.setPadding(rv.getPaddingLeft(), rv.getPaddingTop(), rv.getPaddingTop(), insertBottom);
        rv.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rv.setLayoutManager(gridLayoutManager);
        SourceItemAdapter sourceItemAdapter = new SourceItemAdapter(videoSource.getItems());
        sourceItemAdapter.setListener((item, position) -> {
            currentSourcePosition = position;
            setSelection(item);
        });
        rv.setAdapter(sourceItemAdapter);
        return rv;
    }

    /**
     * 设置指定的集数
     *
     * @param item 指定地第几项播放
     */
    public void setSelection(@NonNull VideoSource.Item item) {
        if (item.equals(currentSourceItem)) {
            // 当前选择的源和之前的一样
            Log.i(TAG, "item == currentSourceItem, item: " + item + ", current: " + currentSourceItem);
            return;
        }
        if (currentSourceItem != null) {
            beforeSourceItem = new VideoSource.Item(currentSourceItem);
        }
        Log.i(TAG, "currentSourceItem: " + item);
        currentSourceItem = item;

        setSelectSource();
    }

    /**
     * 选择下一集进行播放
     * @return false代表已是最后一集了
     */
    public boolean setNextSelection() {
        if (currentSourceItem == null) {
            initSelection();
            return true;
        }
        int selectedTabPosition = getSelectSourceIndex();
        if (selectedTabPosition == -1) {
            return false;
        }
        if (videoSources.size() <= selectedTabPosition) {
            return false;
        }
        List<VideoSource.Item> items = videoSources.get(selectedTabPosition).getItems();
        int sourceItemIndex = items.indexOf(currentSourceItem);
        if (sourceItemIndex == -1 || sourceItemIndex >= items.size() - 1) {
            Log.i(TAG, "last sourceItem");
            return false;
        }
        currentSourceItem = items.get(sourceItemIndex + 1);
        setSelectSourceItem(items);
        return true;
    }

    /**
     * 清除选中的集数
     */
    public void clearSelection() {
        int selectSourcePosition = getSelectSourceIndex();
        if (selectSourcePosition != -1) {
            VideoSource source = videoSources.get(selectSourcePosition);
            for (int i = 0; i < source.getItems().size(); i++) {
                VideoSource.Item item = source.getItems().get(i);
                if (item.getSelected()) {
                    item.setSelected(false);
                    Objects.requireNonNull(sourceViews.get(selectSourcePosition)
                            .getAdapter()).notifyItemChanged(i);
                    break;
                }
            }
        }
        beforeSourceItem = null;
        currentSourceItem = null;
    }

    private int getSelectSourceIndex() {
        if (currentSourceItem == null) {
            return -1;
        }
        for (int i = 0; i < videoSources.size(); i++) {
            VideoSource source = videoSources.get(i);
            if (currentSourceItem.getParam().getSourceId().equals(source.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取当前源所在的位置
     * @return 所在的位置
     */
    public int getCurrentSourcePosition() {
        return currentSourcePosition;
    }

    /**
     * 是否是最后一集
     * @return 是否最后一集
     */
    public boolean hasNextSelection() {
        return isLastSourceItem;
    }

    /**
     * 换源
     */
    public boolean switchSource() {
        if (++switchSourceCount == videoSources.size()) {
            return false;
        }
        if (videoSources.size() <= 1) {
            // 没用可以切换的源了
            Log.i(TAG, "没有可以切换的源");
            return false;
        }
        int selectedTabPosition = tab.getSelectedTabPosition();
        if (selectedTabPosition == -1) {
            return false;
        }
        int nextPosition = selectedTabPosition + 1;
        if (nextPosition > videoSources.size() - 1) {
            // 如果是最后一个了就切换会第一个
            nextPosition = 0;
        }
        int beforeSelectionIndex = videoSources.get(selectedTabPosition).getItems().indexOf(currentSourceItem);
        VideoSource nextSource = videoSources.get(nextPosition);
        List<VideoSource.Item> items = nextSource.getItems();
        if (beforeSelectionIndex == -1 || beforeSelectionIndex > items.size() - 1) {
            return false;
        }
        setSelection(nextSource.getItems().get(beforeSelectionIndex));
        return true;
    }

    private void initSelection() {
        if (currentSourceItem == null && !videoSources.isEmpty()) {
            List<VideoSource.Item> items = videoSources.get(0).getItems();
            if (items.isEmpty()) {
                return;
            }
            // 默认选择第一个
            setSelection(items.get(0));
        }
    }

    private void setSelectSource() {
        for (int i = 0; i < videoSources.size(); i++) {
            VideoSource videoSource = videoSources.get(i);
            clearSelectSourceItem(videoSource, i);
            if (videoSource.getId().equals(currentSourceItem.getParam().getSourceId())) {
                TabLayout.Tab tabItem = tab.getTabAt(i);
                if (tabItem == null) {
                    return;
                }
                tabItem.select();
                setSelectSourceItem(videoSource.getItems());
            }
        }
    }

    private void clearSelectSourceItem(VideoSource videoSource, int tabPosition) {
        if (beforeSourceItem == null) {
            return;
        }
        PlayParam before = beforeSourceItem.getParam();
        // PlayParam current = currentSourceItem.getParam();
        if (videoSource.getId().equals(before.getSourceId())) {
            List<VideoSource.Item> items = videoSource.getItems();
            int index = items.indexOf(beforeSourceItem);
            Log.i(TAG, "beforeSelectedPosition: " + index);
            if (index == -1) {
                return;
            }
            Log.i(TAG, "clear select: tabSelectPosition: " + tabPosition + ", selection: " + index);
            beforeSourceItem.setSelected(false);
            items.get(index).setSelected(false);
            SourceItemAdapter adapter = (SourceItemAdapter) sourceViews.get(tabPosition).getAdapter();
            if (adapter == null) {
                return;
            }
            adapter.notifyItemChanged(index);
        }
    }

    private void setSelectSourceItem(List<VideoSource.Item> items) {
        for (int i = 0; i < items.size(); i++) {
            VideoSource.Item item = items.get(i);
            int selectedTabPosition = tab.getSelectedTabPosition();
            if (selectedTabPosition == -1 || selectedTabPosition >= sourceViews.size()) {
                Log.w(TAG, "selectedTabPosition >= sourceViews.size");
                return;
            }
            SourceItemAdapter adapter = (SourceItemAdapter) sourceViews.get(selectedTabPosition).getAdapter();
            if (adapter == null) {
                return;
            }

            if (item.getSelected()) {
                item.setSelected(false);
                adapter.notifyItemChanged(i);
            }
            if (item.getParam().getSelectionId().equals(currentSourceItem.getParam().getSelectionId())) {
                item.setSelected(true);
                adapter.notifyItemChanged(i);
                isLastSourceItem = i == items.size() - 1;
                if (sourceItemChangeListener != null) {
                    sourceItemChangeListener.onSourceItemChanged(item, i);
                }
            }
        }
    }

    private void init() {
        setOrientation(VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LayoutVideoSourceBinding sourceBinding = LayoutVideoSourceBinding.inflate(inflater, this);
        tab = sourceBinding.tab;
        vp = sourceBinding.vp;

        vp.setAdapter(sourcePageAdapter);
        tab.setupWithViewPager(vp);
        setVisibility(INVISIBLE);

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                v.setOnApplyWindowInsetsListener(null);
                insertBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                return insets;
            }
        });
    }

    private class SourcePageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return videoSources.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return videoSources.get(position).getName();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
             container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            RecyclerView rv = sourceViews.get(position);
            container.addView(rv);
            return rv;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    private static class SourceItemAdapter extends RecyclerView.Adapter<SourceItemAdapter.ViewHolder> {

        private final List<VideoSource.Item> items;

        private OnSourceItemChangeListener listener;

        public SourceItemAdapter(List<VideoSource.Item> items) {
            this.items = items;
        }

        public void setListener(OnSourceItemChangeListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            TextView textView = ItemListSourceBinding.inflate(inflater, parent, false).getRoot();
            ViewHolder viewHolder = new ViewHolder(textView);
            textView.setOnClickListener(v -> {
                if (listener == null) {
                    return;
                }
                int position = viewHolder.getAbsoluteAdapterPosition();
                listener.onSourceItemChanged(items.get(position), position);
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VideoSource.Item item = items.get(position);
            holder.itemView.setSelected(item.getSelected());
            ((TextView) holder.itemView).setText(item.getName());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
