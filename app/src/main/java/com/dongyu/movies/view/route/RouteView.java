package com.dongyu.movies.view.route;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dongyu.movies.config.SPConfig;
import com.dongyu.movies.data.movie.PlaySource;
import com.dongyu.movies.databinding.LayoutRouteBinding;
import com.dongyu.movies.event.OnRouteChangeListener;
import com.dongyu.movies.event.OnSelectionChangeListener;
import com.dongyu.movies.fragment.selection.SelectionFragment;
import com.dongyu.movies.utils.SpUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RouteView extends LinearLayout {

    private static final String TAG = RouteView.class.getSimpleName();

    private LayoutRouteBinding binding;

    private final PagerAdapter pagerAdapter;

    private final List<PlaySource> playSources = new ArrayList<>();

    private final List<SelectionFragment> fragments = new ArrayList<>();

    /**
     * 当前线路，默认不选择
     * 范围：（0-count)
     */
    private int currentRoute = -1;

    /**
     * 当前集数，默认不选择
     * 范围：（1-count)
     */
    private int currentSelection = 0;


    /**
     * 失败重试次数
     */
    private int retryCount = 0;

    private boolean isClone = false;

    /**
     * 是否允许重复选择
     */
    private boolean allowRepeatedSelection = false;

    /**
     * 监听选集改变事件
     */
    private OnRouteChangeListener routeChangeListener;

    private final List<OnSelectionChangeListener> selectionChangeListeners = new ArrayList<>();

    public void setRouteChangeListener(OnRouteChangeListener routeChangeListener) {
        this.routeChangeListener = routeChangeListener;
    }

    private void setSelectionChangeListeners(List<OnSelectionChangeListener> listeners) {
        if (listeners.isEmpty())
            return;
        this.selectionChangeListeners.addAll(listeners);
    }

    public List<OnSelectionChangeListener> getSelectionChangeListeners() {
        return selectionChangeListeners;
    }

    public void setAllowRepeatedSelection(boolean allowRepeatedSelection) {
        this.allowRepeatedSelection = allowRepeatedSelection;
    }

    public RouteView(Context context) {
        this(context, null);
    }

    public RouteView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RouteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = LayoutRouteBinding.inflate(inflater, this);

        pagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager(),
                getActivity().getLifecycle(), fragments);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, i) ->
                tab.setText(playSources.get(i).getName())).attach();
    }

    public AppCompatActivity getActivity() {
        return ((AppCompatActivity) getContext());
    }

    public SelectionFragment getFragment() {
        if (fragments.isEmpty() || currentRoute < 0 || currentRoute >= fragments.size()) {
            return null;
        }
        return fragments.get(currentRoute);
    }

    public RouteView cloneView(OnRouteChangeListener listener) {
        RouteView routeView = new RouteView(getContext());
        routeView.setRouteChangeListener(listener);
        routeView.isClone = true;
        routeView.setSelectionChangeListeners(selectionChangeListeners);
        routeView.setPlaySources(playSources);
        routeView.setRoute(currentRoute);
        routeView.setSelection(currentSelection);
        return routeView;
    }

    public int getSelection() {
        return currentSelection;
    }

    public int getRoute() {
        return currentRoute;
    }

    public String getSelectionName() {
        SelectionFragment fragment = getFragment();
        if (fragment == null) {
            return "";
        }
        return fragment.getSelectionName();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPlaySources(List<PlaySource> sources) {
        if (sources == null || sources.isEmpty())
            return;
        this.playSources.clear();
        this.playSources.addAll(sources);

        this.fragments.clear();

        for (int i = 0; i < playSources.size(); i++) {
            SelectionFragment selectionFragment = createSelectionFragment(i);
            fragments.add(selectionFragment);
        }

        pagerAdapter.notifyDataSetChanged();
    }

    public void setRoute(int route) {
        if (route < 0 || route >= playSources.size()) {
            return;
        }
        this.currentRoute = route;

        if (binding == null)
            return;
        TabLayout tabLayout = binding.tabLayout;
        if (route == tabLayout.getSelectedTabPosition()) {
            return;
        }
        TabLayout.Tab tab = tabLayout.getTabAt(route);
        if (tab != null)
            tab.select();
    }

    /**
     * 通过routeId改变选中的线路
     *
     * @param routeId 线路id
     */
    public void setRouteId(int routeId) {
        for (int i = 0; i < playSources.size(); i++) {
            PlaySource source = playSources.get(i);
            if (source.getRouteId() == routeId) {
                setRoute(i);
                break;
            }
        }
    }

    public void setSelection(int selection) {
        if (selection < 1)
            return;
        this.currentSelection = selection;
        if (fragments.isEmpty())
            return;
        // 清除选择
        clearSelection();
        SelectionFragment fragment = getFragment();
        if (fragment == null)
            return;
        fragment.setSelection(selection);
    }

    public void nextSelection() {
        int nextSelectionPosition = getCurrentSelectionPosition() + 1;
        if (nextSelectionPosition >= getSourcesCount()) {
            return;
        }
        PlaySource source = getSource();
        if (source == null) {
            return;
        }
        int next = source.getData().get(nextSelectionPosition).getSelection();
        setSelection(next);
        int routeId = getRouteId();
        if (routeId == -1)
            return;
        routeChangeListener.onNextSelection(routeId, currentRoute, currentSelection);
    }

    public void clearSelection() {
        if (fragments.isEmpty())
            return;
        fragments.forEach(SelectionFragment::clearSelection);
    }

    @Nullable
    public PlaySource getSource() {
        if (playSources.isEmpty() || currentRoute < 0 || currentRoute >= playSources.size())
            return null;
        return playSources.get(currentRoute);
    }

    public int getSourcesCount() {
        PlaySource source = getSource();
        if (source == null)
            return 0;
        return source.getData().size();
    }

    public List<PlaySource> getPlaySources() {
        return playSources;
    }

    /**
     * 获取routeId
     *
     * @return 获取失败则返回-1
     */
    public int getRouteId() {
        PlaySource source = getSource();
        if (source == null)
            return -1;
        return source.getRouteId();
    }

    public void restRetryCount() {
        retryCount = 0;
    }

    private SelectionFragment createSelectionFragment(int position) {
        List<PlaySource.Item> selectionList = playSources.get(position).getData();
        SelectionFragment selectionFragment = new SelectionFragment();
        selectionFragment.setSelectionList(selectionList, currentRoute == position ? currentSelection : 0);

        OnSelectionChangeListener listener = (selection, index) -> {

            if (isClone) {
                selectionChangeListeners.get(position).onChangedSelection(selection, index);
            }
            // 判断选择的路线是否发生了改变
            boolean changeRoute = position != currentRoute;

            if (!changeRoute && !allowRepeatedSelection && selection == currentSelection) {
                // 重复选择了
                return;
            }

            // 之前的选集
            int beforeIndex = selectionFragment.getSelectionPosition();

            setRoute(position);

            currentSelection = selection;

            int routeId = getRouteId();

            // Log.d(TAG, "routeId: " + routeId);

            if (routeId == -1)
                return;

            clearSelection();
            // 设置选择
            selectionFragment.setSelection(selection);
            if (!changeRoute) {
                int currentIndex = selectionFragment.getSelectionPosition();
                boolean nextSelection = (currentIndex - beforeIndex == 1) && beforeIndex != 0;
                if (nextSelection) {
                    routeChangeListener.onNextSelection(routeId, currentRoute, selection);
                } else {
                    routeChangeListener.onSelectionChanged(routeId, currentRoute, selection);
                }
            } else {
                routeChangeListener.onSelectionChanged(routeId, currentRoute, selection);
            }
        };
        selectionFragment.setSelectionChangeListener(listener);
        if (!isClone) {
            selectionChangeListeners.add(listener);
        }
        return selectionFragment;
    }

    public void switchRoute() {
        if (playSources.isEmpty()) {
            return;
        }
        int size = playSources.size();

        if (retryCount >= size) {
            return;
        }

        Boolean autoSwitch = SpUtils.INSTANCE
                .getOrDefault(SpUtils.DEFAULT_KEY, SPConfig.PLAYER_AUTO_SWITCH_ROUTE, true);
        if (autoSwitch != null && !autoSwitch) {
            return;
        }

        int beforeSelection = currentSelection;
        if (++currentRoute >= size) {
            currentRoute = 0;
        }
        retryCount++;
        binding.viewPager.setCurrentItem(currentRoute);
        setSelection(beforeSelection);
        if (routeChangeListener != null) {
            routeChangeListener.onSelectionChanged(getRouteId(), currentRoute, currentSelection);
        }

    }

    public int getCurrentSelectionPosition() {
        SelectionFragment fragment = getFragment();
        if (fragment == null) {
            return -1;
        }
        return fragment.getSelectionPosition();
    }

    public boolean hasNextSelection() {
        return getCurrentSelectionPosition() < getSourcesCount() - 1;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        binding = null;
    }

    static class PagerAdapter extends FragmentStateAdapter {

        private final List<SelectionFragment> fragments;

        public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                            List<SelectionFragment> fragments) {
            super(fragmentManager, lifecycle);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
