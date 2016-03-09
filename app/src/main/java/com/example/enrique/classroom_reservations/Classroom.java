package com.example.enrique.classroom_reservations;

/**
 * Created by enrique on 22/02/16.
 */
public class Classroom {

    private int Classroom_id;
    private String Name;

    public int getClassroom_id() {
        return Classroom_id;
    }

    public void setClassroom_id(int classroom_id) {
        Classroom_id = classroom_id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Classroom(String id, String name){
        this.Classroom_id = Integer.parseInt(id);
        this.Name = name;
    }

    @Override
    public String toString() {
        return this.Name;
    }
}
