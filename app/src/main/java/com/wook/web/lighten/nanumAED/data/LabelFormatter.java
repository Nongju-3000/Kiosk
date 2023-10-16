package com.wook.web.lighten.nanumAED.data;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class LabelFormatter extends IndexAxisValueFormatter {
    int depth;
    int scale;
    int count = 1;
    ArrayList<String> arrayList = new ArrayList<String>();
    public LabelFormatter(int depth, int scale){
        this.depth = depth*2;
        this.scale = scale;
        init();
    }
    void init(){
        for(int i=0; i<=scale; i++){
            arrayList.add(String.valueOf(i*30));
        }
    }
    //scale == time/30
    //depth == total_depth
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        for(int i=0; i<=scale; i++){
            if((int)value >= (depth/scale)*i - 5 && (int)value <= (depth/scale)*i + 5){
                return arrayList.get(i);
            }
        }
        return "-";
    }
}