package com.example.ky.bpm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ky.bpm.ImageProcessing;
import com.example.ky.bpm.Model.RecordModel;
import com.example.ky.bpm.R;
import com.example.ky.bpm.comps.CircleProgressBar;
import com.example.ky.bpm.comps.CommonFunctions;
import com.example.ky.bpm.comps.DBHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MeasureFragment extends Fragment {
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private TextView text = null;
    private TextView text_avg_rest = null, text_avg_active = null;
    private CircleProgressBar progressBar = null;
    private LineChart chart =null;
    private static PowerManager.WakeLock wakeLock = null;
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    public static enum TYPE {
        GREEN, RED
    };
    private static TYPE currentType = TYPE.GREEN;
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private static float current_progress = 0;
    private int current_recent_bpm = 0;
    public MeasureFragment() {}
    public static MeasureFragment _this;
    public static MeasureFragment newInstance() {
        MeasureFragment fragment = new MeasureFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        _this = fragment;
        return fragment;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View rootview = inflater.inflate(R.layout.fragment_measure, container, false);
        preview = (SurfaceView) rootview.findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        text = (TextView) rootview.findViewById(R.id.txt_bpm);
        text_avg_rest = (TextView)rootview.findViewById(R.id.txt_graph_value);
        text_avg_active = (TextView)rootview.findViewById(R.id.txt_graph_avg);
        progressBar = (CircleProgressBar)rootview.findViewById(R.id.circle_progress);
        progressBar.setProgress(0);
        chart = (LineChart)rootview.findViewById(R.id.line_chart);
        chart.setDescription("");
        chart.setViewPortOffsets(0, 0, 0, 0);
        chart.setNoDataTextDescription("You need to provide data for the chart.");
        chart.setTouchEnabled(false);chart.setDragEnabled(false);chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        LineData data = new LineData();
        chart.setData(data);
        XAxis x = chart.getXAxis();
        x.setEnabled(false);
        YAxis y = chart.getAxisLeft();
        y.setLabelCount(6, false);
        y.setTextColor(Color.TRANSPARENT);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisMaxValue(120);
        y.setAxisMinValue(-50);
        y.setAxisLineColor(Color.TRANSPARENT);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        loadAVGRestActivityToManifest();
        return rootview;
    }
    public void setCurrentBeat(int beats){
        String value_disp = String.format("%03d",beats);
        text.setText(value_disp);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            wakeLock.release();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            saveAVGRestActivityToManifest();
        } catch(Exception e){}
    }

    public void saveAVGRestActivityToManifest(){
        DBHelper m_db = new DBHelper(getActivity().getApplicationContext());
        Date date = new Date(System.currentTimeMillis());
        String date_now = CommonFunctions.getStringFromDateTime(date);
        int week_no = getWeekNumberFromDate(date);
        if(current_recent_bpm > 0)
            m_db.insertRecordIntoTable(date_now,current_recent_bpm,week_no);
    }
    public int getWeekNumberFromDate(Date date){
        Calendar now = Calendar.getInstance();
        now.set(Calendar.YEAR,date.getYear());
        now.set(Calendar.MONTH,date.getMonth());//0- january ..4-May
        now.set(Calendar.DATE, date.getDate());
        return now.get(Calendar.WEEK_OF_YEAR);
    }
    public void loadAVGRestActivityToManifest(){
        DBHelper m_db = new DBHelper(getActivity().getApplicationContext());
        RecordModel m_md = m_db.getRecentData();
        int value_rest = 0;
        int value_activity = 0;
        if(m_md.getBpm_history() != 0 && m_md.getDate_history() != "")
        {
            value_rest = m_md.getBpm_history();
        }
        setAvgRestText(value_rest);
        setAvgActivityText(value_activity);
    }
    public void setAvgActivityText(int value){
        text_avg_active.setText(String.format("%02d",value));
    }
    public void setAvgRestText(int value){
        text_avg_rest.setText(String.format("%02d",value));
    }
   @Override
    public void onResume() {
        super.onResume();
        try {
            wakeLock.acquire();
            camera = Camera.open();
            startTime = System.currentTimeMillis();
            loadAVGRestActivityToManifest();
        }catch (Exception e){

        }
    }
    private static Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null){
                current_progress = 0;
                _this.progressBar.setProgress(current_progress);
                _this.chart.clearValues();
                _this.addEntry(30);
                throw new NullPointerException();
            }
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;
            int width = size.width;
            int height = size.height;
            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            if (imgAvg == 0 || imgAvg == 255) {
                current_progress = 0;
                _this.setCurrentBeat(0);
                _this.progressBar.setProgress(current_progress);
                _this.chart.clearValues();
                _this.addEntry(30);
                processing.set(false);
                return;
            }
            int averageArrayAvg = 0, averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }
            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            TYPE newType = currentType;
            current_progress += 0.5;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;///////////////sometimes beats comes to me as zero, that makes progress and graph makes so busy....
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }
            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            currentType = newType;
            if(current_progress >= 100){
                current_progress = 100;
            }
            _this.progressBar.setProgress(current_progress);
            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000;
            if (totalTimeInSecs >= 1) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);//
                Log.d("test",dpm+"");
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;
                    current_progress = 0;
                    _this.progressBar.setProgress(current_progress);
                    _this.setCurrentBeat(0);
                    _this.chart.clearValues();
                    _this.addEntry(30);
                    processing.set(false);
                    return;
                }
                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;
                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                if(current_progress >= 100) {
                    current_progress = 100;
                    _this.current_recent_bpm = beatsAvg;
                }
                    _this.setCurrentBeat(beatsAvg);
                    _this.addEntry(beatsAvg);
                    startTime = System.currentTimeMillis();
                    beats = 0;
            }
            processing.set(false);
        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e(TAG, "PreviewDemo-surfaceCallback - Exception in setPreviewDisplay()", t);
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }

    private void addEntry(int value_add) {
        LineData data = chart.getData();
        data.setDrawValues(false);

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = _this.createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), value_add), 0);
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(15);
            chart.moveViewToX(data.getEntryCount());
        }
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dataset");
        set.setDrawHorizontalHighlightIndicator(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawCircles(false);
        set.setLineWidth(2);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(getResources().getColor(R.color.colorAccent));
        set.setCircleRadius(4f);
        set.setValueTextSize(0f);
        set.setDrawValues(false);
        set.setFillColor(getResources().getColor(R.color.colorAccent));
        set.addEntry(new Entry(0,30));
        return set;
    }
}
