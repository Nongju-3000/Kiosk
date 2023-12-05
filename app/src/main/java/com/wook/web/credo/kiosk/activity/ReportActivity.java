package com.wook.web.credo.kiosk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.google.gson.Gson;
import com.wook.web.credo.kiosk.R;

import java.util.ArrayList;

import soup.neumorphism.NeumorphCardView;

public class ReportActivity extends AppCompatActivity {
    ReportItem reportItem;
    private BackPressCloseHandler backPressCloseHandler;
    private LineChart chart;
    private Button report_up_depth, report_down_depth;
    private ArrayList<Float> chart_item, pressTime_list;
    private TextView all_accuracy, depth_accuracy;
    private String min, max;
    private int div_time, depth_accuracy_;
    private float prev_val = 0f, prev_xval = 0f, prev_bval = 0f, prev_bxval = 0f;
    private TextView date, time, report_end_time, report_interval_sec, report_cycle, report_depth_correct,
            correctCount, report_bpm, report_total, ave_depth;
    private ArrayList<Float> stop_list = new ArrayList<>(), ble_list = new ArrayList<>();
    final float breath_limit = 10.0f, breath_threshold = 50.0f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        backPressCloseHandler = new BackPressCloseHandler();

        Gson gson = new Gson();
        String json = getIntent().getStringExtra("report");
        reportItem = gson.fromJson(json, ReportItem.class);

        chart = findViewById(R.id.chart_img);
        report_end_time = findViewById(R.id.report_end_time);
        report_interval_sec = findViewById(R.id.report_interval_sec);
        report_cycle = findViewById(R.id.report_cycle);
        report_depth_correct = findViewById(R.id.report_depth_correct);
        report_up_depth = findViewById(R.id.report_up_depth);
        report_down_depth = findViewById(R.id.report_down_depth);
        report_bpm = findViewById(R.id.report_bpm);
        report_total = findViewById(R.id.report_total);
        all_accuracy = findViewById(R.id.all_accuracy);
        depth_accuracy = findViewById(R.id.depth_accuracy);
        correctCount = findViewById(R.id.correctCount);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        ave_depth = findViewById(R.id.ave_depth);

        String today = getIntent().getStringExtra("today");
        String[] split = today.split("/");
        date.setText(setDateTextView(split[0]));
        String[] timeSplit = split[1].split(":");
        int time_sec = Integer.parseInt(timeSplit[0]) * 3600 + Integer.parseInt(timeSplit[1]) * 60 + Integer.parseInt(timeSplit[2]);
        time_sec = time_sec + 60;
        String hour = String.format("%02d", time_sec / 3600);
        String miniute = String.format("%02d", (time_sec % 3600) / 60);
        String sec = String.format("%02d", time_sec % 60);
        time.setText(split[1] + " - " + hour + ":" + miniute + ":" + sec);;

        report_end_time.setText(reportItem.getReport_end_time() + " sec");
        report_interval_sec.setText(reportItem.getReport_interval_sec() + " sec");
        report_cycle.setText(reportItem.getReport_cycle());

        report_depth_correct.setVisibility(View.VISIBLE);
        report_depth_correct.setText(reportItem.getReport_depth_correct() + " %");
        report_up_depth.setText(reportItem.getReport_up_depth() + " %");
        if (Integer.parseInt(reportItem.getReport_up_depth()) >= 70) {
            report_up_depth.setTextColor(Color.GREEN);
        } else if (Integer.parseInt(reportItem.getReport_up_depth()) >= 30) {
            report_up_depth.setTextColor(Color.rgb(255, 100, 0));
        } else if (Integer.parseInt(reportItem.getReport_up_depth()) >= 0) {
            report_up_depth.setTextColor(Color.RED);
        }

        report_down_depth.setText(reportItem.getReport_down_depth() + " %");
        if (Integer.parseInt(reportItem.getReport_down_depth()) >= 70) {
            report_down_depth.setTextColor(Color.GREEN);
        } else if (Integer.parseInt(reportItem.getReport_down_depth()) >= 30) {
            report_down_depth.setTextColor(Color.rgb(255, 100, 0));
        } else if (Integer.parseInt(reportItem.getReport_down_depth()) >= 0) {
            report_down_depth.setTextColor(Color.RED);
        }
        report_bpm.setText(reportItem.getReport_bpm() + " BPM");

        chart_item = reportItem.getReport_depth_list();
        pressTime_list = reportItem.getReport_presstime_list();
        min = reportItem.getReport_Min();
        max = reportItem.getReport_Max();
        int sum_depth = Integer.parseInt(reportItem.getReport_depth_correct())
                + Integer.parseInt(reportItem.getReport_up_depth())
                + Integer.parseInt(reportItem.getReport_down_depth());

        depth_accuracy_ = (int) ((double) sum_depth / (double) 3);

        depth_accuracy.setText(depth_accuracy_ + " %");

        correctCount.setText(reportItem.getDepth_correct());
        stop_list = reportItem.getStop_time_list();
        report_total.setText(reportItem.getDepth_num());
        div_time = Integer.parseInt(reportItem.getReport_end_time());
        div_time = (int) (Math.ceil(div_time / 30.0d) * 30);

        setChart();
    }

    private void setChart(){
        ArrayList<Float> stop_xval = new ArrayList<Float>();

        chart.clear();
        chart.fitScreen();
        chart.setDescription(null);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setDrawBorders(false);
        chart.setAutoScaleMinMaxEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        BarLineChartTouchListener barLineChartTouchListener = (BarLineChartTouchListener) chart.getOnTouchListener();
        barLineChartTouchListener.stopDeceleration();
        float scale = div_time / 15f;
        chart.zoomToCenter(scale / 2, 0f);

        LineData data = new LineData();
        ILineDataSet set = createSet();
        ILineDataSet set1 = createSet1();

        data.addDataSet(set);
        data.addDataSet(set1);

        chart.animateX(500);
        chart.setData(data);

        LimitLine ll0 = new LimitLine(0f, " ");
        ll0.setLineWidth(1f);
        ll0.enableDashedLine(10f, 0f, 0f);
        ll0.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll0.setLineColor(Color.WHITE);
        ll0.setTextSize(10f);

        LimitLine ll1 = new LimitLine(Float.parseFloat(max), " ");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 0f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setLineColor(ContextCompat.getColor(ReportActivity.this, R.color.lineColor));
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(Float.parseFloat(min), " ");
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(10f, 0f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        ll2.setLineColor(ContextCompat.getColor(ReportActivity.this, R.color.lineColor));
        ll2.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setEnabled(true);
        leftAxis.setGridColor(Color.TRANSPARENT);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setShowSpecificLabelPositions(true);
        leftAxis.setSpecificLabelPositions(new float[]{0f, Float.parseFloat(min), Float.parseFloat(max)});
        leftAxis.addLimitLine(ll0);
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setZeroLineWidth(1f);
        leftAxis.setZeroLineColor(Color.WHITE);
        leftAxis.setAxisMaximum(70f);
        leftAxis.setAxisMinimum(-20f);  // << should control may be
        leftAxis.setInverted(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.invalidate();

        int depth = 0;
        int depth_sum = 0;
        int depth_count = 0;
        int index = 0;
        int chart_size = chart_item.size();
        float pre_item = 0f;
        float pre_xval = 0f;

        int j = 0;

        for (float item : chart_item) {
            if (item >= Integer.parseInt(min) && item <= Integer.parseInt(max)) {
                try {
                    if (pre_item != 0) {
                        addEntry_chart(0, (pressTime_list.get(index) - pressTime_list.get(index - 1)) / 2 + pressTime_list.get(index - 1), 0);
                        addEntry_chart(item, pressTime_list.get(index), 0);
                    } else {
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(item, pressTime_list.get(index), 0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(item, pressTime_list.get(index), 0);
                }
                depth++;
                depth_sum += item;
                depth_count++;
            } else if ((5 < item && item < Integer.parseInt(min))) {
                try {
                    if (pre_item != 0) {
                        addEntry_chart(0, (pressTime_list.get(index) - pressTime_list.get(index - 1)) / 2 + pressTime_list.get(index - 1), 0);
                        addEntry_chart(item, pressTime_list.get(index), 0);
                    } else {
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(item, pressTime_list.get(index), 0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(item, pressTime_list.get(index), 0);
                }
                depth++;
                depth_sum += item;
                depth_count++;
            } else if ((Integer.parseInt(max) < item && item <= 100)) {
                try {
                    if (pre_item != 0) {
                        addEntry_chart(0, (pressTime_list.get(index) - pressTime_list.get(index - 1)) / 2 + pressTime_list.get(index - 1), 0);
                        addEntry_chart(70, pressTime_list.get(index), 0);
                    } else {
                        addEntry_chart(0, pre_xval, 0);
                        addEntry_chart(70, pressTime_list.get(index), 0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    addEntry_chart(0, 0, 0);
                    addEntry_chart(70, pressTime_list.get(index), 0);
                }
                depth++;
                depth_sum += item;
                depth_count++;
            } else if (100 < item) {
                try {
                    if (pre_item != 0) {
                        addEntry_chart(10, (pressTime_list.get(index) - pressTime_list.get(index - 1)) / 2 + pressTime_list.get(index - 1), 0);
                        addEntry_chart((item - 100), pressTime_list.get(index), 0);
                    } else {
                        addEntry_chart(10, pre_xval, 0);
                        addEntry_chart((item - 100), pressTime_list.get(index), 0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    addEntry_chart(10, 0, 0);
                    addEntry_chart((item - 100), pressTime_list.get(index), 0);
                }
                depth++;
                depth_count++;
                depth_sum += (item - 100);
            } else if (0 == item) {
                try {
                    addEntry_chart(0, (pressTime_list.get(index - 1) - pressTime_list.get(index - 2)) / 2 + pressTime_list.get(index - 1), 0);
                    addEntry_chart(0, pressTime_list.get(index - 1) + (float) stop_list.get(j), 0);
                    pre_xval = pressTime_list.get(index - 1) + (float) stop_list.get(j);
                    stop_xval.add((pressTime_list.get(index - 1) - pressTime_list.get(index - 2)) / 2 + pressTime_list.get(index - 1));
                    stop_xval.add(pressTime_list.get(index - 1) + (float) stop_list.get(j));
                } catch (IndexOutOfBoundsException e) {
                    try {
                        addEntry_chart(0, pressTime_list.get(index - 1) + pressTime_list.get(index - 1), 0);
                        addEntry_chart(0, pressTime_list.get(index - 1) + (float) stop_list.get(j), 0);
                        pre_xval = pressTime_list.get(index - 1) + (float) stop_list.get(j);
                        stop_xval.add(pressTime_list.get(index - 1) + pressTime_list.get(index - 1));
                        stop_xval.add(pressTime_list.get(index - 1) + (float) stop_list.get(j));
                    } catch (IndexOutOfBoundsException e_) {
                        addEntry_chart(0, 0, 0);
                        addEntry_chart(0, 0f + stop_list.get(j), 0);
                        pre_xval = 0f + stop_list.get(j);
                        stop_xval.add(0f);
                        stop_xval.add(0f + stop_list.get(j));
                    }
                }
                depth++;
                index--;
                j++;
            } else if (item < 5) {
                switch ((int) item) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        depth++;
                        index--;
                        break;
                }
            }
            index++;
            if (index == chart_size) {
                if (item < 5) {
                    data = chart.getData();
                    if (data != null) {
                        ILineDataSet breath = data.getDataSetByIndex(1);
                        stop_array.add(breath.getEntryCount() - 1);
                    }
                }
            }
            pre_item = item;
        }

        for (int i = 0; i < stop_xval.size(); i++) {
            if (i % 2 == 1) {
                if (i + 1 == stop_xval.size()) {
                    addll_chart(stop_xval.get(i), div_time, 1);
                }
            } else {
                if (i == 0) {
                    addll_chart(0, stop_xval.get(i), 1);
                } else {
                    addll_chart(stop_xval.get(i - 1), stop_xval.get(i), 1);
                }
            }
        }

        if (stop_xval.size() == 0) {
            addll_chart(0, div_time, 1);
        }
        double avg_depth = 0.0;
        if (depth_count != 0) {
            avg_depth = Float.parseFloat(String.valueOf(depth_sum)) / depth_count;
        }
        String avg_depth_s = String.format("%.1f", avg_depth);

        ave_depth.setText(avg_depth_s + "mm");

        int total_score = depth_accuracy_;
        all_accuracy.setText(total_score + "%");

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.removeAllLimitLines();
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisMaximum((float) div_time);
        xAxis.setAxisMinimum(0.0f);
        xAxis.setGridColor(Color.TRANSPARENT);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setShowSpecificLabelPositions(true);

        float[] label_position = new float[(int) scale + 1];
        for (int i = 0; i < scale + 1; i++) {
            label_position[i] = i * 15.0f;
        }
        xAxis.setSpecificLabelPositions(label_position);
        chart.getXAxis().setTextColor(Color.WHITE);
        for (int i = 0; i < stop_xval.size(); i++) {
            LimitLine ll;
            if (i % 2 == 0)
                ll = new LimitLine(stop_xval.get(i), " ");
            else {
                try {
                    ll = new LimitLine(stop_xval.get(i), (String.format("%.1f", stop_list.get(i / 2)) + " Secs  "));
                } catch (IndexOutOfBoundsException e) {
                    ll = new LimitLine(stop_xval.get(i), "2.0 Secs  ");
                }
            }
            ll.setLineWidth(1f);
            ll.enableDashedLine(15f, 2f, 0f);
            ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            ll.setLineColor(Color.RED);
            ll.setTextSize(10f);
            ll.setTextColor(Color.WHITE);
            xAxis.addLimitLine(ll);
        }
        for (int i = 0; i < ble_list.size(); i++) {
            LimitLine ll;
            if (i % 2 == 0)
                ll = new LimitLine(ble_list.get(i), " ");
            else {
                try {
                    ll = new LimitLine(ble_list.get(i), " ");
                } catch (IndexOutOfBoundsException e) {
                    ll = new LimitLine(ble_list.get(i), " ");
                }
            }
            ll.setLineWidth(1f);
            ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll.setLineColor(Color.BLUE);
            ll.setTextSize(10f);
            ll.setTextColor(Color.WHITE);
            xAxis.addLimitLine(ll);
        }

        chart.moveViewToX(-0.5f);
        chart.setNoDataText(" ");
        chart.invalidate();
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255, 255, 255));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255, 255, 255));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255, 0, 0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet3() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255, 210, 0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet4() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255, 100, 0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet5() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(72, 214, 214));
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet6() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(73, 198, 68));
        set.setLineWidth(2.5f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    ArrayList<Integer> stop_array = new ArrayList<>();
    boolean isOver = false;

    private void addEntry_chart(float val, float xval, int label) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet depth = data.getDataSetByIndex(0);
            ILineDataSet breath = data.getDataSetByIndex(1);
            if (depth == null) {
                depth = createSet();
                breath = createSet1();
                data.addDataSet(depth);
                data.addDataSet(breath);
            }

            if (label == 0)
                data.addEntry(new Entry(xval, val, "Label1"), 0);

            if (label == 1 || label == 2)
                data.addEntry(new Entry(xval, val), 1);

            if (val > 0f && (val > Integer.parseInt(max) || val < Integer.parseInt(min))) {
                if (val > prev_val && label == 0) {
                    ILineDataSet set = createSet2();
                    data.addDataSet(set);
                    set.addEntry(new Entry(prev_xval, prev_val));
                    set.addEntry(new Entry(xval, val));
                }
            }

            if (val > 0f && val < Integer.parseInt(min)) {
                if (prev_val > val && label == 0) {
                    ILineDataSet set = createSet3();
                    data.addDataSet(set);
                    set.addEntry(new Entry(prev_xval, prev_val));
                    set.addEntry(new Entry(xval, val));
                }
            }

            if (isOver) {
                ILineDataSet set = createSet4();
                data.addDataSet(set);
                set.addEntry(new Entry(prev_bxval, prev_bval));
                set.addEntry(new Entry(xval, val));
                isOver = false;
            }

            if (label == 2) {
                ILineDataSet set = createSet4();
                data.addDataSet(set);
                set.addEntry(new Entry(prev_bxval, prev_bval));
                set.addEntry(new Entry(xval, val));
                isOver = true;
            }

            if (label == 3) {
                ILineDataSet set = createSet5();
                data.addDataSet(set);
                set.addEntry(new Entry(xval, val));
            }

            if (label == 0) {
                prev_xval = xval;
                prev_val = val;
            } else if (label == 1 || label == 2) {
                prev_bxval = xval;
                prev_bval = val;
            }

            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void addll_chart(float pre_xval, float xval, int label) {
        LineData data = chart.getData();
        if (data != null) {
            if (label == 0) {
                ILineDataSet set = createSet5();
                ILineDataSet mset = createSet5();
                data.addDataSet(set);
                data.addDataSet(mset);
                set.addEntry(new Entry(pre_xval, breath_limit));
                set.addEntry(new Entry(xval, breath_limit));
                mset.addEntry(new Entry(pre_xval, breath_threshold));
                mset.addEntry(new Entry(xval, breath_threshold));
            }
            if (label == 1) {
                ILineDataSet set = createSet6();
                ILineDataSet mset = createSet6();
                data.addDataSet(set);
                data.addDataSet(mset);
                set.addEntry(new Entry(pre_xval, Float.parseFloat(min)));
                set.addEntry(new Entry(xval, Float.parseFloat(min)));
                mset.addEntry(new Entry(pre_xval, Float.parseFloat(max)));
                mset.addEntry(new Entry(xval, Float.parseFloat(max)));
            }

            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void resetChart() {
        chart.clear();
        chart.invalidate();
    }

    private String setDateTextView(String rawDate) {
        String year, month, day;

        year = String.valueOf(rawDate.charAt(0)) + rawDate.charAt(1) + rawDate.charAt(2) + rawDate.charAt(3);
        month = String.valueOf(rawDate.charAt(4)) + rawDate.charAt(5);
        day = String.valueOf(rawDate.charAt(6)) + rawDate.charAt(7);

        return year + "." + month + "." + day;
    }

    @Override
    public void onBackPressed(){
        this.backPressCloseHandler.onBackPressed();
    }

    class BackPressCloseHandler {

        public BackPressCloseHandler() {
        }

        public void onBackPressed() {
            Intent main = new Intent(ReportActivity.this, CPRActivity.class);
            startActivity(main);
            finish();
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        }


    }
}
