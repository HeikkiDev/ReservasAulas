package com.example.enrique.classroom_reservations;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by enrique on 22/02/16.
 */
public class AnalyzeJSON {

    public static Teacher analyzeTeacher(JSONObject jsonObject) throws JSONException {
        final String nombreObjeto = "data";
        String id = jsonObject.getJSONObject(nombreObjeto).getString("Teacher_id");
        String username = jsonObject.getJSONObject(nombreObjeto).getString("Username");
        String firstname = jsonObject.getJSONObject(nombreObjeto).getString("First_Name");
        String lastname = jsonObject.getJSONObject(nombreObjeto).getString("Last_Name");
        String email = jsonObject.getJSONObject(nombreObjeto).getString("Email");
        String apiKey = jsonObject.getJSONObject(nombreObjeto).getString("Api_Key");
        boolean admin = (jsonObject.getJSONObject(nombreObjeto).getInt("Admin")== 0)?false:true;

        Teacher teacher = new Teacher(id,username,firstname,lastname,email,apiKey, admin);
        return teacher;
    }

    public static ArrayList<Teacher> analyzeAllTeachers(JSONObject jsonObject) throws JSONException {
        ArrayList<Teacher> teachersList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String id = jsonObject.getJSONArray("data").getJSONObject(i).getString("Teacher_id");
            String username = jsonObject.getJSONArray("data").getJSONObject(i).getString("Username");
            String firstname = jsonObject.getJSONArray("data").getJSONObject(i).getString("First_Name");
            String lastname = jsonObject.getJSONArray("data").getJSONObject(i).getString("Last_Name");
            String email = jsonObject.getJSONArray("data").getJSONObject(i).getString("Email");

            Teacher teacher = new Teacher(id,username,firstname, lastname, email, "", false);
            teachersList.add(teacher);
        }

        return teachersList;
    }

    public static ArrayList<Classroom> analyzeClassroomsArray(JSONObject jsonObject) throws JSONException {
        ArrayList<Classroom> classroomList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String id = jsonObject.getJSONArray("data").getJSONObject(i).getString("Classroom_id");
            String name = jsonObject.getJSONArray("data").getJSONObject(i).getString("Name");
            Classroom classroom = new Classroom(id, name);
            classroomList.add(classroom);
        }

        return classroomList;
    }

    public static ArrayList<Integer> analyzeOccupiedIntervals(JSONObject jsonObject) throws JSONException {
        ArrayList<Integer> intervalsList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int interval = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Interval");
            intervalsList.add(interval);
        }

        return intervalsList;
    }

    public static ArrayList<Reservation> analyzeReservations(JSONObject jsonObject) throws JSONException {
        ArrayList<Reservation> reservationsList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            String name = jsonObject.getJSONArray("data").getJSONObject(i).getString("Name");
            int interval = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Interval");
            String date = jsonObject.getJSONArray("data").getJSONObject(i).getString("Date");
            String first = jsonObject.getJSONArray("data").getJSONObject(i).getString("First_Name");
            Reservation reservation = new Reservation(name,interval,date,first);
            reservation.setClassroom_id(jsonObject.getJSONArray("data").getJSONObject(i).getInt("Classroom_id"));
            reservationsList.add(reservation);
        }

        return reservationsList;
    }

    public static ArrayList<Incidence> analyzeIncidences(JSONObject jsonObject) throws JSONException {
        ArrayList<Incidence> incidencesList = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("data").length(); i++) {
            int id = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Id");
            int idClassroom = jsonObject.getJSONArray("data").getJSONObject(i).getInt("Classroom_id");
            String description = jsonObject.getJSONArray("data").getJSONObject(i).getString("Description");
            String name = jsonObject.getJSONArray("data").getJSONObject(i).getString("Name");
            boolean solved = (jsonObject.getJSONArray("data").getJSONObject(i).getInt("Solved") == 0)?false:true;
            Incidence incidence = new Incidence(id, idClassroom, description, solved, name);
            incidencesList.add(incidence);
        }

        return incidencesList;
    }
}
