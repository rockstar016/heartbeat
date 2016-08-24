package com.example.ky.bpm.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ky.bpm.ExpandListAdapter;
import com.example.ky.bpm.Model.RecordModel;
import com.example.ky.bpm.R;
import com.example.ky.bpm.comps.CommonFunctions;
import com.example.ky.bpm.comps.DBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HistoryViewFragment extends Fragment{

    private final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for(int j = 0; j < group.getChildCount(); j++){
                final ToggleButton view = (ToggleButton)group.getChildAt(j);
                view.setChecked(view.getId() == checkedId);
            }
        }
    };
    private RadioGroup m_group;
    private ToggleButton toggle_day, toggle_week, toggle_month, toggle_year;
    private BarChart chart;
    private ExpandableListView listview;
    private int current_selected_item = -1;
    private ExpandListAdapter listAdapter;
    private List<RecordModel> headerList;
    private HashMap<RecordModel,List<RecordModel>> itemList;
    DBHelper database;
    private TextView txt_min, txt_max, txt_average;
    public HistoryViewFragment() {
        // Required empty public constructor
    }

    public static HistoryViewFragment newInstance() {
        HistoryViewFragment fragment = new HistoryViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_history_view, container, false);
        m_group  =(RadioGroup)rootview.findViewById(R.id.toggle_group);
        m_group.setOnCheckedChangeListener(ToggleListener);
        toggle_day = (ToggleButton)rootview.findViewById(R.id.toggle_day);
        toggle_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_week = (ToggleButton)rootview.findViewById(R.id.toggle_week);
        toggle_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_month = (ToggleButton)rootview.findViewById(R.id.toggle_month);
        toggle_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_year = (ToggleButton)rootview.findViewById(R.id.toggle_year);
        toggle_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        txt_min = (TextView)rootview.findViewById(R.id.txt_min_bpm);
        txt_max = (TextView)rootview.findViewById(R.id.txt_max_bpm);
        txt_average = (TextView)rootview.findViewById(R.id.txt_avg_bpm);


        chart = (BarChart)rootview.findViewById(R.id.barchart);
        chart.setDrawGridBackground(false);
        chart.setDescription("");
        chart.setNoDataTextDescription("Select the header.");
        chart.setDragEnabled(true);
        chart.setTouchEnabled(false);
        chart.setMaxVisibleValueCount(30);
        chart.setDragEnabled(true);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinValue(0);
        xAxis.setAxisMaxValue(30);
        xAxis.setDrawLabels(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1);
        xAxis.setDrawAxisLine(false);


        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);

        chart.getAxisRight().setEnabled(false);
        chart.animateY(2500);
        chart.getLegend().setEnabled(false);


        listview = (ExpandableListView)rootview.findViewById(R.id.listview);
        headerList = new ArrayList<>();
        itemList = new HashMap<>();
        database = new DBHelper(getActivity().getApplicationContext());
        SetTextMin_Max_Avg(0,0,0);

        initListAdapter();
//        listview.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                CollapseAll();
//                openExpand(groupPosition);
//            }
//        });
        listview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                CollapseAll();
                openExpand(groupPosition);
                return true;
            }
        });
        return rootview;
    }
    public void CollapseAll(){
        for(int i = 0 ; i < listAdapter.getGroupCount(); i++) {
            listview.collapseGroup(i);
        }
    }
    public void openExpand(int position){
        listview.expandGroup(position);
        disp_Chart(position);
    }
    private void initListAdapter(){
        listAdapter = new ExpandListAdapter(getActivity(),headerList,itemList);
        listview.setAdapter(listAdapter);
        //CollapseAll();
    }
    public void onToggleClick(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
        if(((ToggleButton)view).isChecked()) {
            current_selected_item = getIndexFromView(view);
            displayProcessData();
        }
    }
    private int getIndexFromView(View view){
        int current = -1;
        switch (view.getId())
        {
            case R.id.toggle_day:
                current = CommonFunctions.DATA_VIEW_DAY;
                break;
            case R.id.toggle_week:
                current = CommonFunctions.DATA_VIEW_WEEK;
                break;
            case R.id.toggle_month:
                current = CommonFunctions.DATA_VIEW_MONTH;
                break;
            case R.id.toggle_year:
                current = CommonFunctions.DATA_VIEW_YEAR;
                break;
        }
        return current;
    }
    private void displayProcessData(){
        if(current_selected_item != -1)
        {
            pull_DataFromDB_ToggleButton(current_selected_item);
        }
    }
    private void pull_DataFromDB_ToggleButton(int current_mode){
        headerList.clear();
        headerList = database.getHeaderDataList(current_mode);
        makeChildList(current_mode);
        initListAdapter();
    }
    private void makeChildList(int current_mode){
        itemList.clear();

        for(int i = 0; i<headerList.size(); i++){
            if(current_mode == CommonFunctions.DATA_VIEW_DAY) {
                ArrayList<RecordModel> tmp_model = new ArrayList<>();
                tmp_model = database.getDatasbetweendays(headerList.get(i).getDate_history(),headerList.get(i).getDate_history());
                itemList.put(headerList.get(i), tmp_model);
            }
            else if(current_mode == CommonFunctions.DATA_VIEW_WEEK){
                ArrayList<RecordModel> tmp_model  = new ArrayList<>();
                tmp_model = database.getDatasInWeek(headerList.get(i).getWeek_no());
                itemList.put(headerList.get(i),tmp_model);
            }
            else if(current_mode == CommonFunctions.DATA_VIEW_MONTH){
                ArrayList<RecordModel> tmp_model = new ArrayList<>();
                tmp_model = database.getDatasInMonth(headerList.get(i).getDate_history());
                itemList.put(headerList.get(i),tmp_model);
            }
            else if(current_mode == CommonFunctions.DATA_VIEW_YEAR){
                ArrayList<RecordModel> tmp_model = new ArrayList<>();
                tmp_model = database.getDatasInYear(headerList.get(i).getDate_history());
                itemList.put(headerList.get(i),tmp_model);
            }
        }
    }
    private int getMinValue(int idx_header){
        int size_array = itemList.get(headerList.get(idx_header)).size();
        List<RecordModel> arrayList = itemList.get(headerList.get(idx_header));
        int min_value = 300;
        for (int i = 0; i < size_array; i++) {
            if(min_value > arrayList.get(i).getBpm_history())
                min_value = arrayList.get(i).getBpm_history();
        }
        return min_value;
    }
    private int getMaxValue(int idx_header){
        int size_array = itemList.get(headerList.get(idx_header)).size();
        List<RecordModel> arrayList = itemList.get(headerList.get(idx_header));
        int max_value = 0;
        for (int i = 0; i < size_array; i++) {
            if(max_value < arrayList.get(i).getBpm_history())
                max_value = arrayList.get(i).getBpm_history();
        }
        return max_value;
    }
    private void disp_Chart(int idx_header){
        chart.getAxisLeft().removeAllLimitLines();
        chart.invalidate();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int size_array = itemList.get(headerList.get(idx_header)).size();
        for (int i = 0; i < size_array; i++) {
            float val = (float) (itemList.get(headerList.get(idx_header)).get(i).getBpm_history());
            yVals1.add(new BarEntry(i + 1, val));
        }

        int avg_bpm = headerList.get(idx_header).getBpm_history();
        int min_bpm = getMinValue(idx_header);
        int max_bpm = getMaxValue(idx_header);
        if(min_bpm == max_bpm) {
            min_bpm = 30;
        }
        LimitLine limit_avg = new LimitLine(avg_bpm, "");
        limit_avg.setLineColor(getResources().getColor(R.color.average_color));
        limit_avg.setLineWidth(1f);
        limit_avg.disableDashedLine();
        limit_avg.setTextColor(Color.TRANSPARENT);


        LimitLine limit_min = new LimitLine(min_bpm - 5,"");
        limit_min.setLineColor(getResources().getColor(R.color.max_min_color));
        limit_min.setTextColor(Color.TRANSPARENT);
        limit_min.disableDashedLine();
        limit_min.setLineWidth(1f);


        LimitLine limit_max = new LimitLine(max_bpm,"");
        limit_max.setLineColor(getResources().getColor(R.color.max_min_color));
        limit_max.setTextColor(Color.TRANSPARENT);
        limit_max.disableDashedLine();
        limit_max.setLineWidth(1f);
        chart.getAxisLeft().setAxisMinValue(min_bpm - 5);
        chart.getAxisLeft().setAxisMaxValue(max_bpm);
        BarDataSet set1;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet)chart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            chart.notifyDataSetChanged();
            chart.invalidate();
        } else {
            set1 = new BarDataSet(yVals1, "Data Set");
            set1.setColors(new int[]{getResources().getColor(R.color.colorAccent)});
            set1.setDrawValues(false);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            chart.setData(data);
        }
        chart.getData().setBarWidth(0.8f);

        SetTextMin_Max_Avg(min_bpm, max_bpm,avg_bpm);
        chart.getAxisLeft().setDrawLimitLinesBehindData(false);
        chart.getAxisLeft().addLimitLine(limit_avg);
        chart.getAxisLeft().addLimitLine(limit_min);
        chart.getAxisLeft().addLimitLine(limit_max);
        chart.invalidate();
        chart.animateY(1000);


    }
    private void SetTextMin_Max_Avg(int min, int max, int avg){
        if(min == 0) { txt_min.setText(""); } else {txt_min.setText(min + "");}
        if(max == 0) { txt_max.setText(""); } else { txt_max.setText(max + ""); }
        if(avg == 0) { txt_average.setText(""); } else { txt_average.setText("avg\n\r" + avg); }

    }
}
