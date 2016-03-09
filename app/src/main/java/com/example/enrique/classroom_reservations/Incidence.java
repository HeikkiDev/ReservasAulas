package com.example.enrique.classroom_reservations;

/**
 * Created by enrique on 22/02/16.
 */
public class Incidence {

    private int Id;
    private int Classroom_id;
    private String Description;
    private boolean Solved;
    private String Name;

    public int getId() { return Id; }

    public void setId(int id) { Id = id; }

    public int getClassroom_id() {
        return Classroom_id;
    }

    public void setClassroom_id(int classroom_id) {
        Classroom_id = classroom_id;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isSolved() {
        return Solved;
    }

    public void setSolved(boolean solved) {
        Solved = solved;
    }

    public String getName() { return Name; }

    public void setName(String name) { Name = name; }

    public Incidence(int id, int idClassroom, String desc, boolean solved, String name){
        this.Id = id;
        this.Classroom_id = idClassroom;
        this.Description = desc;
        this.Solved = solved;
        this.Name = name;
    }

    @Override
    public String toString() {
        return this.Name+"\n"+this.Description;
    }
}
