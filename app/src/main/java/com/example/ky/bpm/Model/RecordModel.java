package com.example.ky.bpm.Model;

/**
 * Created by RockStar0116 on 2016.08.18.
 */
public class RecordModel {
    private String date_history;
    private int bpm_history;
    private int week_no;

    public int getWeek_no() {
        return week_no;
    }

    public void setWeek_no(int week_no) {
        this.week_no = week_no;
    }

    public String getDate_history() {
        return date_history;
    }

    public void setDate_history(String date_history) {
        this.date_history = date_history;
    }

    public int getBpm_history() {
        return bpm_history;
    }

    public void setBpm_history(int bpm_history) {
        this.bpm_history = bpm_history;
    }
}
