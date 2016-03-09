package com.example.enrique.classroom_reservations;

import android.text.format.DateFormat;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by enrique on 22/02/16.
 */
public class Reservation implements Serializable{

    private int Classroom_id;
    private String Name;
    private int Interval;
    private Date Date;
    private String First_Name;

    private String[] arrayIntervals = new String[]{"8:15 - 9:15","9:15 - 10:15","10:15 - 11:15","11:45 - 12:45","12:45 - 13:45","13:45 - 14:45"};

    public int getClassroom_id() {return Classroom_id;}

    public void setClassroom_id(int classroom_id) {Classroom_id = classroom_id;}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getInterval() {
        return Interval;
    }

    public void setInterval(int interval) {
        Interval = interval;
    }

    public java.util.Date getDate() {
        return Date;
    }

    public void setDate(java.util.Date date) {
        Date = date;
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public void setFirst_Name(String first_Name) {
        First_Name = first_Name;
    }

    public Reservation(String name, int interval, String date, String first){
        String[] arrDate = date.split("-");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(arrDate[0]), Integer.parseInt(arrDate[1])-1, Integer.parseInt(arrDate[2]));
        this.Name = name;
        this.Interval = interval;
        this.Date = new Date(calendar.getTimeInMillis());
        this.First_Name = first;
    }

    @Override
    public String toString() {
        return Name+"\n"+"El "+DateFormat.format("dd/MM/yyyy", this.Date).toString()+" a las "+ arrayIntervals[Interval-1];
    }
}
