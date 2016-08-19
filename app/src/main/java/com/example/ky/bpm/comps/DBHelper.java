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
            m_database = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
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
    public boolean insertRecordIntoTable(String history_date, int bpm_count) {
        String sql = String.format("INSERT INTO history (Id, his_date, bpm) VALUES(?,'%s','%d')",history_date,bpm_count) ;
        m_database.execSQL(sql);

        closeDataBase();
        return true;
    }
    public ArrayList<RecordModel> getDatas(String start_date, String end_date){
        ArrayList<RecordModel> models = new ArrayList<>();
        Cursor res =  m_database.rawQuery( "select * from history where his_date >= '"+start_date+"' AND his_date <= '" +end_date +"'", null );
        if(res.moveToFirst()) {
            while (res.moveToNext()) {
                RecordModel model = new RecordModel();
                model.setDate_history(res.getString(1));
                model.setBpm_history(res.getInt(2));
                models.add(model);
            }
        }
        res.close();
        closeDataBase();
        return models;
    }
    public RecordModel getRecentData(){
        RecordModel model = new RecordModel();
        model.setBpm_history(0);
        model.setDate_history("");
        Cursor res = m_database.rawQuery("SELECT * FROM history ORDER BY Id ASC LIMIT 1",null);
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
}
