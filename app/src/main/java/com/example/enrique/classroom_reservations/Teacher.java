package com.example.enrique.classroom_reservations;

import java.io.Serializable;

/**
 * Created by enrique on 22/02/16.
 */
public class Teacher implements Serializable{

    private int Teacher_id;
    private String Username;
    private String First_Name;
    private String Last_Name;
    private String Email;
    private String Api_Key;
    private boolean Admin;

    public int getTeacher_id() {
        return Teacher_id;
    }

    public void setTeacher_id(int teacher_id) {
        Teacher_id = teacher_id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public void setFirst_Name(String first_Name) {
        First_Name = first_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public void setLast_Name(String last_Name) {
        Last_Name = last_Name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {Email = email;}

    public String getApi_Key() {
        return Api_Key;
    }

    public void setApi_Key(String api_Key) { Api_Key = api_Key; }

    public boolean isAdmin() { return Admin; }

    public void setAdmin(boolean admin) { Admin = admin; }

    public Teacher(String id, String username, String firstname, String lastname, String email, String apiKey, boolean admin){
        this.Teacher_id = Integer.parseInt(id);
        this.Username = username;
        this.First_Name = firstname;
        this.Last_Name = lastname;
        this.Email = email;
        this.Api_Key = apiKey;
        this.Admin = admin;
    }

    @Override
    public String toString() {
        return this.First_Name+" "+this.Last_Name;
    }
}
