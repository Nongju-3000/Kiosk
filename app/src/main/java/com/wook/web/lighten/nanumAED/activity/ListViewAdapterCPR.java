package com.wook.web.lighten.nanumAED.activity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wook.web.lighten.nanumAED.R;

import java.util.ArrayList;

public class ListViewAdapterCPR extends BaseAdapter {

    private ArrayList<UserItem> list;
    private Activity activity;

    // 생성할 클래스
    ListViewAdapterCPR(Activity activity, ArrayList<UserItem> list ){
        this.activity = activity;
        this.list = new ArrayList<UserItem>();
        this.list = list;
    }


    @Override
    public int getCount() {
        // 리스트뷰 갯수 리턴
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // 리스트 값 리턴
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewAdapterCPR.ListViewHolder holder  = null;
        final int pos = position;
        TextView depth;
        TextView angle;
        TextView battery;
        TextView position_txt;
        TextView breath_txt;

        // 최초 뷰 생성
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.listitem, parent, false);
            depth = (TextView) convertView.findViewById(R.id.depth_txt);
            angle = (TextView) convertView.findViewById(R.id.angle_txt);
            battery = (TextView) convertView.findViewById(R.id.battery_txt);
            position_txt = (TextView) convertView.findViewById(R.id.position_txt);
            breath_txt = (TextView) convertView.findViewById(R.id.breath_txt);

            holder = new ListViewHolder();
            holder.depth = depth;
            holder.angle = angle;
            holder.battery = battery;
            holder.position = position_txt;
            holder.breath = breath_txt;

            // list values save
            convertView.setTag(holder);
            // 텍스트 보이기
            depth.setVisibility(View.VISIBLE);
            angle.setVisibility(View.VISIBLE);
            battery.setVisibility(View.VISIBLE);
            position_txt.setVisibility(View.VISIBLE);
            breath_txt.setVisibility(View.VISIBLE);
        }
        else
        {
            // list values get
            holder = (ListViewAdapterCPR.ListViewHolder) convertView.getTag();
            depth = holder.depth;
            angle = holder.angle;
            battery = holder.battery;
            position_txt = holder.position;
            breath_txt = holder.breath;
        }

        // 리스트 이름 보이기
        depth.setText(list.get(pos).getDepth());
        angle.setText(list.get(pos).getAngle());
        position_txt.setText(list.get(pos).getPosition());
        breath_txt.setText(list.get(pos).getBreath());

        // 리스트 아이템을 터치 했을 때 이벤트 발생
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity.getApplicationContext(), "선택한 이름:" + list.get(pos), Toast.LENGTH_SHORT).show();
            }
        });

        // 리스트 아이템을 길게 터치 했을 떄 이벤트 발생
        convertView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(activity.getApplicationContext(), list.get(pos) + " 삭제합니다.", Toast.LENGTH_SHORT).show();
                // list choice remove
                list.remove(pos);
                // listview update
                CPRActivity.listviewCPR.clearChoices();
                CPRActivity.listViewAdapterCPR.notifyDataSetChanged();
                return false;
            }
        });

        return convertView;
    }

    // list values class
    private class ListViewHolder {
        TextView depth;
        TextView angle;
        TextView battery;
        TextView position;
        TextView breath;
    }
}


