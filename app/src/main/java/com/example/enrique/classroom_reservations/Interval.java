package com.example.enrique.classroom_reservations;

/**
 * Created by enrique on 22/02/16.
 */
public class Interval {

    private int _interval;
    private String hour;

    private String[] arrayIntervals = new String[]{"8:15 - 9:15","9:15 - 10:15","10:15 - 11:15","11:45 - 12:45","12:45 - 13:45","13:45 - 14:45"};

    public int get_interval() {
        return _interval;
    }

    public void set_interval(int _interval) {
        this._interval = _interval;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Interval(int interval){
        this._interval = interval;
        this.hour = arrayIntervals[interval - 1];
    }

    @Override
    public String toString() {
        return this.hour;
    }
}
