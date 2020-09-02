package io.bidmachine.test.app;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NativeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<String> infoList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InfoViewHolder(new TextView(viewGroup.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((InfoViewHolder) viewHolder).setInfo(infoList.get(position));
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    void addInfo(List<String> infoList) {
        this.infoList.addAll(infoList);
        notifyItemRangeInserted(getItemCount(), infoList.size() - 1);
    }

    void clearContent() {
        infoList.clear();
        notifyDataSetChanged();
    }

    static final class InfoViewHolder extends RecyclerView.ViewHolder {

        InfoViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setInfo(String info) {
            TextView textView = (TextView) itemView;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
            textView.setText(info);
        }

    }

}