package com.example.ky.bpm.comps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.ky.bpm.Model.RecordModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by RockStar-0116 on 2016.07.10.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "bpmhistory.db";
    private static String DB_PATH;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase m_database;
    private Context m_context;
    public DBHelper(Context context)  {
        super(context, DB_NAME, null, DATABASE_VERSION);
        try{
        m_context = context;
        String packageName = context.getPackageName();
        DB_PATH = "/data/data/" + packageName + "/databases/";
        this.m_database = openDatabase();
        }catch (IOException e){}

    }
    public SQLiteDatabase openDatabase() throws IOException {
        String path = DB_PATH + DB_NAME;
        if (m_database == null) {
            createDataBase();
        }
        m_database = SQLiteDatabase.openDatabase(path, null,
                SQLiteDatabase.OPEN_READWRITE);
        return m_database;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void createDataBase() throws IOException {
        boolean dbExists = checkDataBase();
        if (!dbExists) {
            this.getReadableDatabase();
            copyDataBase();
        } else {
            Log.e(getClass().getName(), "Database already exists");
        }
    }
    public boolean checkDataBase(){
        File databaseFile = new File(DB_PATH + DB_NAME);
        return databaseFile.exists();
    }
    private void copyDataBase() throws IOException {
        InputStream myInput = m_context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.close();
    }
    private SQLiteDatabase getDataBase() {
        return this.m_database;
    }
    public synchronized void closeDataBase() {
        if(m_database!= null)
            m_database.close();
    }
    public boolean insertRecordIntoTable(String history_date, int bpm_count,int weekno) {
        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        String sql = String.format("INSERT INTO history (Id, his_date, bpm,week) VALUES(?,'%s','%d','%d')",history_date,bpm_count,weekno) ;
        m_database.execSQL(sql);
        closeDataBase();
        return true;
    }
    public ArrayList<RecordModel> getDatasbetweendays(String start_date, String end_date){
        start_date += " 00:00";
        end_date += " 23:59";
        ArrayList<RecordModel> models = new ArrayList<>();
        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        String condition_format = "'%Y-%m-%d %H:%M'";
        String query = String.format("select * from history where strftime(%s,his_date) >= strftime(%s,'%s') AND strftime(%s, his_date) <= strftime(%s, '%s') order by his_date asc",condition_format, condition_format, start_date,condition_format, condition_format, end_date);
        //String query = "select * from history";
        Cursor res =  m_database.rawQuery( query, null );
        if(res.moveToFirst()) {
            do {
                RecordModel model = new RecordModel();
                model.setDate_history(res.getString(1));
                model.setBpm_history(res.getInt(2));
                model.setWeek_no(res.getInt(3));
                models.add(model);
            } while (res.moveToNext());
        }
        res.close();
        closeDataBase();
        return models;
    }
    public RecordModel getRecentData(){
        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        RecordModel model = new RecordModel();
        model.setBpm_history(0);
        model.setDate_history("");
        Cursor res = m_database.rawQuery("SELECT * FROM history ORDER BY Id DESC LIMIT 1",null);
        if(res.getCount() != 0)
        {
            res.moveToFirst();
            model.setDate_history(res.getString(1));
            model.setBpm_history(res.getInt(2));
        }
        res.close();
        closeDataBase();
        return model;
    }
    public ArrayList<RecordModel> getHeaderDataList(int mode){
        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        ArrayList<RecordModel> header_list = new ArrayList<>();
        String query = new String();
        if(mode == CommonFunctions.DATA_VIEW_DAY){
            query = "select strftime('%Y-%m-%d', his_date) yr_day, sum(bpm)/count(bpm) bpm_avg, week from history group by yr_day;";
        }
        else if(mode == CommonFunctions.DATA_VIEW_WEEK){
            query = "select strftime('%Y-%m-%d', his_date) yr_day, sum(bpm)/count(bpm) bpm_avg, week from history group by week order by yr_day asc;";
        }
        else if(mode == CommonFunctions.DATA_VIEW_MONTH){
            query = "select strftime('%Y-%m',his_date) yr_day, sum(bpm)/count(bpm) bpm_avg, week from history group by yr_day";
        }
        else if(mode == CommonFunctions.DATA_VIEW_YEAR){
            query = "select strftime('%Y',his_date) yr_day, sum(bpm)/count(bpm) bpm_avg, week from history group by yr_day";
        }
        Cursor cursor = m_database.rawQuery(query,null);
        if(cursor != null && cursor.getCount() != 0)
        {
            cursor.moveToFirst();
            do{
                RecordModel model = new RecordModel();
                if(mode == CommonFunctions.DATA_VIEW_MONTH)
                {
                    model.setDate_history(CommonFunctions.convertStringForHeaderItemMonth(cursor.getString(0)));
                }
                else {
                    model.setDate_history(cursor.getString(0));
                }
                model.setBpm_history(cursor.getInt(1));
                model.setWeek_no(cursor.getInt(2));
                header_list.add(model);
            } while(cursor.moveToNext());
        }
        cursor.close();
        closeDataBase();
        return header_list;
    }
    public ArrayList<RecordModel> getDatasInWeek(int week){
        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        ArrayList<RecordModel> recordModels = new ArrayList<>();
        String query = String.format("SELECT * FROM history WHERE week = '%d' ORDER BY his_date ASC",week);
        Cursor cursor = m_database.rawQuery(query, null);
        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do{
                RecordModel model = new RecordModel();
                model.setDate_history(cursor.getString(1));
                model.setBpm_history(cursor.getInt(2));
                model.setWeek_no(cursor.getInt(3));
                recordModels.add(model);
            }while(cursor.moveToNext());
            cursor.close();
            closeDataBase();
        }
        return recordModels;
    }
    public ArrayList<RecordModel> getDatasInMonth(String month){
        String search_month = CommonFunctions.convertStringFromMonthDate(month);

        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        ArrayList<RecordModel> recordModels = new ArrayList<>();
        String query = String.format("SELECT * FROM history WHERE his_date LIKE '%s' ORDER BY his_date ASC",search_month + "%");
        Cursor cursor = m_database.rawQuery(query, null);
        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do{
                RecordModel model = new RecordModel();
                model.setDate_history(cursor.getString(1));
                model.setBpm_history(cursor.getInt(2));
                model.setWeek_no(cursor.getInt(3));
                recordModels.add(model);
            }while(cursor.moveToNext());
            cursor.close();
            closeDataBase();
        }
        return recordModels;
    }
    public ArrayList<RecordModel> getDatasInYear(String year){
        //String search_month = CommonFunctions.convertStringFromMonthDate(month);

        if (!m_database.isOpen()) {
            try {
                openDatabase();
            }catch (Exception e){}
        }
        ArrayList<RecordModel> recordModels = new ArrayList<>();
        String query = String.format("SELECT * FROM history WHERE his_date LIKE '%s' ORDER BY his_date ASC",year + "%");
        Cursor cursor = m_database.rawQuery(query, null);
        if(cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            do{
                RecordModel model = new RecordModel();
                model.setDate_history(cursor.getString(1));
                model.setBpm_history(cursor.getInt(2));
                model.setWeek_no(cursor.getInt(3));
                recordModels.add(model);
            }while(cursor.moveToNext());
            cursor.close();
            closeDataBase();
        }
        return recordModels;
    }
}
