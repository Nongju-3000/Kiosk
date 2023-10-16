package com.wook.web.lighten.nanumAED.activity;

import java.io.Serializable;
import java.util.ArrayList;

public class ReportItem implements Serializable {

    public String getReport_end_time() {
        return report_end_time;
    }

    public String getReport_interval_sec() {
        return report_interval_sec;
    }

    public String getReport_cycle() {
        return report_cycle;
    }

    public String getReport_depth_correct() {
        return report_depth_correct;
    }

    public String getReport_up_depth() {
        return report_up_depth;
    }

    public String getReport_down_depth() {
        return report_down_depth;
    }

    public String getReport_bpm() {
        return report_bpm;
    }

    public String getReport_angle() {
        return report_angle;
    }

    public ArrayList<Float> getReport_depth_list() {
        return report_depth_list;
    }
    public ArrayList<Float> getReport_presstime_list() {
        return report_presstimeList;
    }
    public ArrayList<Float> getReport_breathtime() {
        return report_breathtime;
    }
    public ArrayList<Float> getReport_breathval() {
        return report_breathval;
    }

    public String getReport_ventil_volume() {
        return report_ventil_volume;
    }
    public String getReport_name() {
        return report_name;
    }

    public String getReport_Min(){ return min; }
    public String getReport_Max(){ return max; }

    public String getDepth_num(){ return depth_num; }
    public String getDepth_correct(){ return depth_correct; }

    public String getReport_position_num(){return position_num;}
    public String getReport_position_correct(){return position_correct;}

    public String getReport_lung_num(){return lung_num;}
    public String getReport_lung_correct(){return lung_correct;}

    public ArrayList<Float> getStop_time_list(){ return stop_time_list; }
    public ArrayList<Float> getReport_bletime_list(){ return report_bletime_list; }


    private String report_name;
    private String report_end_time;
    private String report_interval_sec;
    private String report_cycle;
    private String report_depth_correct;
    private String report_up_depth;     
    private String report_down_depth;
    private String report_bpm;
    private String report_angle;
    private ArrayList<Float> report_depth_list;
    private ArrayList<Float> report_presstimeList;
    private ArrayList<Float> report_breathtime;
    private ArrayList<Float> report_breathval;
    private String report_ventil_volume;
    private String min;
    private String max;
    private String depth_correct;
    private String depth_num;
    private String position_num;
    private String position_correct;
    private String lung_num;
    private String lung_correct;
    private ArrayList<Float> stop_time_list;
    private ArrayList<Float> report_bletime_list;

    public ReportItem(String report_name,
                      String report_end_time,
                      String report_interval_sec,
                      String report_cycle,
                      String report_depth_correct,
                      String report_up_depth,
                      String report_down_depth,
                      String report_bpm,
                      String report_angle,
                      ArrayList<Float> report_depth_list,
                      ArrayList<Float> report_presstimeList,
                      ArrayList<Float> report_breathtime,
                      ArrayList<Float> report_breathval,
                      String report_ventil_volume,
                      String min,
                      String max,
                      String depth_num,
                      String depth_correct,
                      String position_num,
                      String position_correct,
                      String lung_num,
                      String lung_correct,
                      ArrayList<Float> stop_time_list,
                      ArrayList<Float> report_ble_time_list) {

        this.report_name = report_name;
        this.report_end_time = report_end_time;
        this.report_interval_sec = report_interval_sec;
        this.report_cycle = report_cycle;
        this.report_depth_correct = report_depth_correct;
        this.report_up_depth = report_up_depth;
        this.report_down_depth = report_down_depth;
        this.report_bpm = report_bpm;
        this.report_angle = report_angle;
        this.report_depth_list = report_depth_list;
        this.report_presstimeList = report_presstimeList;
        this.report_breathtime = report_breathtime;
        this.report_breathval = report_breathval;
        this.report_ventil_volume = report_ventil_volume;
        this.min = min;
        this.max = max;
        this.depth_num = depth_num;
        this.depth_correct = depth_correct;
        this.position_num = position_num;
        this.position_correct = position_correct;
        this.lung_num = lung_num;
        this.lung_correct = lung_correct;
        this.stop_time_list = stop_time_list;
        this.report_bletime_list = report_ble_time_list;
    }
}

