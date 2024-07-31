package com.dongyu.movies.fragment.selection;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.dongyu.movies.data.movie.PlaySource;
import com.dongyu.movies.databinding.ItemListSelectionBinding;
import com.dongyu.movies.event.OnSelectionChangeListener;

import java.util.ArrayList;
import java.util.List;

public class SelectionFragment extends Fragment {

    private static final String TAG = SelectionFragment.class.getSimpleName();

    private static final int SPAN_COUNT = 3;

    private final List<PlaySource.Item> selectionList = new ArrayList<>();

    private SelectionAdapter selectionAdapter;

    private RecyclerView recyclerView;

    public OnSelectionChangeListener selectionChangeListener;

    private int selection = 0;

    /**
     * 当前选中的位置，默认为未选中
     */
    private int selectionPosition = -1;

    private boolean isFailSelection(int selection) {
        return selection < 1;
    }

    public void setSelectionChangeListener(OnSelectionChangeListener selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }

    public int getSelection() {
        return selection;
    }

    public String getSelectionName() {
        if (isFailSelection(selection))
            return "";
        for (PlaySource.Item item : selectionList) {
            if (item.getSelection() == selection) {
                return item.getName();
            }
        }
        return "";
    }

    public int getSelectionPosition() {
        return selectionPosition;
    }

    public void setSelectionPosition(int selectionPosition) {
        this.selectionPosition = selectionPosition;
    }

    public void setSelection(int selection) {
        this.selection = selection;
        selectionPosition = getPositionForSelection(selection);
        if (selectionAdapter != null && !isFailSelection(selection))
            selectionAdapter.notifyItemChanged(selectionPosition);
    }

    public void clearSelection() {
        if (this.selection == 0) return;
        int tmpSelection = this.selection;
        this.selection = 0;
        if (selectionAdapter != null && !isFailSelection(tmpSelection)) {
            selectionAdapter.notifyItemChanged(getPositionForSelection(tmpSelection));
        }
    }

    private int getPositionForSelection(int selection) {
        for (int i = 0; i < selectionList.size(); i++) {
            if (selectionList.get(i).getSelection() == selection) {
                return i;
            }
        }
        return -1;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectionList(List<PlaySource.Item> selections, int selection) {
        this.selection = selection;
        selectionList.clear();
        selectionList.addAll(selections);
        if (selection == 0) {
            selectionPosition = 0;
        } else {
            selectionPosition = getPositionForSelection(selection);
        }
        if (selectionAdapter != null) {
            selectionAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        recyclerView.setClipToPadding(false);
        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);
        selectionAdapter = new SelectionAdapter();
        recyclerView.setAdapter(selectionAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(view, new androidx.core.view.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                // ViewCompat.setOnApplyWindowInsetsListener(v, null);
                int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                recyclerView.setPadding(0, 0, 0, bottom);
                return insets;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    private class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.SelectionViewHolder> {

        @NonNull
        @Override
        public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SelectionViewHolder viewHolder = new SelectionViewHolder(ItemListSelectionBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
            viewHolder.itemView.setOnClickListener(v -> {
                if (selectionChangeListener != null) {
                    int position = viewHolder.getAbsoluteAdapterPosition();
                    selectionChangeListener.onChangedSelection(selectionList.get(position).getSelection(), position);
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
            holder.bindTo(selectionList.get(position));
        }

        @Override
        public int getItemCount() {
            return selectionList.size();
        }

        private class SelectionViewHolder extends RecyclerView.ViewHolder {

            private final TextView nameTv;

            public SelectionViewHolder(ItemListSelectionBinding binding) {
                super(binding.getRoot());
                nameTv = binding.selectionName;
            }

            public void bindTo(PlaySource.Item item) {
                nameTv.setText(item.getName());
                itemView.setSelected(selection == item.getSelection());
            }
        }

    }
}
