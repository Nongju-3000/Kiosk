package com.wook.web.lighten.nanumAED.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Report {

    @PrimaryKey(autoGenerate = true)
    public int report_id;

    @ColumnInfo(name = "report_name")
    public String report_name;

    @ColumnInfo(name = "report_end_time")
    public String report_end_time;

    @ColumnInfo(name = "report_interval_sec")
    public String report_interval_sec;

    @ColumnInfo(name = "report_cycle")
    public String report_cycle;

    @ColumnInfo(name = "report_depth_correct")
    public String report_depth_correct;

    @ColumnInfo(name = "report_up_depth")
    public String report_up_depth;

    @ColumnInfo(name = "report_down_depth")
    public String report_down_depth;

    @ColumnInfo(name = "report_bpm")
    public String report_bpm;

    @ColumnInfo(name = "report_angle")
    public String report_angle;

    @ColumnInfo(name = "report_depth_list")
    public String report_depth_list;

    @ColumnInfo(name = "report_presstimeList")
    public String report_presstimeList;

    @ColumnInfo(name = "report_breathtime")
    public String report_breathtime;

    @ColumnInfo(name = "report_breathval")
    public String report_breathval;

    @ColumnInfo(name = "report_ventil_volume")
    public String report_ventil_volume;

    @ColumnInfo(name = "to_day")
    public String to_day;

    @ColumnInfo(name = "min")
    public String min;

    @ColumnInfo(name = "max")
    public String max;

    @ColumnInfo(name = "depth_num")
    public String depth_num;

    @ColumnInfo(name = "depth_correct")
    public String depth_correct;

    @ColumnInfo(name = "position_num")
    public String position_num;

    @ColumnInfo(name = "position_correct")
    public String position_correct;

    @ColumnInfo(name = "lung_num")
    public String lung_num;

    @ColumnInfo(name = "lung_correct")
    public String lung_correct;

    @ColumnInfo(name = "stop_time_list")
    public String stop_time_list;

    @ColumnInfo(name = "report_bluetoothtime")
    public String report_bluetoothtime;
}
