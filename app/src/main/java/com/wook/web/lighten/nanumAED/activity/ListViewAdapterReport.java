package com.wook.web.lighten.nanumAED.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.wook.web.lighten.nanumAED.R;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListViewAdapterReport  extends RecyclerView.Adapter<ListViewAdapterReport.ViewHolder> {

    private ArrayList<String> mData = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;

        ViewHolder(View itemView) {

            super(itemView);

            textView1 = itemView.findViewById(R.id.text1);

        }
    }


    public ListViewAdapterReport(ArrayList<String> list) {
        mData = list;
    }


    @Override
    public ListViewAdapterReport.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recyclerview_item, parent, false);
        ListViewAdapterReport.ViewHolder vh = new ListViewAdapterReport.ViewHolder(view);

        return vh;
    }


    @Override
    public void onBindViewHolder(ListViewAdapterReport.ViewHolder holder, int position) {
        String text = mData.get(position);
        holder.textView1.setText(text);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }
}

