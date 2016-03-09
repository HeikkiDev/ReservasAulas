package com.example.enrique.classroom_reservations;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class EditReservationActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtClassroomName;
    private Spinner spinnerFreeIntervals;
    private TextView txtDate;
    private Button btnSave;
    private Button btnCancel;

    static ProgressDialog progressDialog;
    static AsyncHttpClient client;
    static Context context;
    static Reservation reservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reservation);
        //
        txtClassroomName= (TextView)findViewById(R.id.txtName);
        spinnerFreeIntervals = (Spinner)findViewById(R.id.spinnerIntervals);
        txtDate = (TextView)findViewById(R.id.txtDate);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        this.context = this;

        // Mensaje de espera
        progressDialog = new ProgressDialog(EditReservationActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(getApplicationContext(), R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando clases libres");
        progressDialog.setContentView(v);

        // Color de la Action Bar
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        // Recupero los datos de la reserva
        Bundle bundle = getIntent().getExtras();
        reservation = (Reservation)bundle.getSerializable("reservation");
        txtClassroomName.setText(reservation.getName());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateFormatted = formatter.format(reservation.getDate());
        txtDate.setText(dateFormatted);

        getReservationsByDate(reservation.getDate(), reservation.getClassroom_id());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSave){
            // GUARDAR CAMBIOS EN EL INTERVAL
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Classroom_id", reservation.getClassroom_id());
                jsonObject.put("Interval",((Interval)spinnerFreeIntervals.getSelectedItem()).get_interval());
                jsonObject.put("OldInterval", reservation.getInterval());
                final String dateFormatted = new SimpleDateFormat("yyyy-MM-dd").format(reservation.getDate());
                jsonObject.put("Date", dateFormatted);
                jsonObject.put("Teacher_id", MainActivity.TEACHER_ME.getTeacher_id());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            RequestParams params = new RequestParams("reservation", jsonObject.toString());
            client = new AsyncHttpClient(true,80,443);
            client.setTimeout(6000);
            client.put(MainActivity.HOST + "/reservations/api/reservations/" + MainActivity.TEACHER_ME.getApi_Key(), params, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                    Log.e("ERROR!!!", "ERROOOOOR");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (response.getString("code").equals("true")) {
                            showToast("Reserva modificada correctamente");
                            setResult(RESULT_OK);
                        }
                        else
                            showToast("No se podido modificar la reserva");
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
            finish();
    }

    private void getReservationsByDate(final Date date, int idClassroom){
        final String dateFormatted = new SimpleDateFormat("yyyy-MM-dd").format(date);

        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/reservations/" + dateFormatted + "/" + idClassroom + "/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Integer> occupiedReservations = AnalyzeJSON.analyzeOccupiedIntervals(response);
                    ArrayList<Interval> freeIntervals = getFreeIntervals(occupiedReservations, date);

                    if (freeIntervals.size() > 0) {
                        // Se cargan los Intervalos de clase libres en el Spinner
                        ArrayAdapter<Interval> adapterReservations = new ArrayAdapter<Interval>(context, android.R.layout.simple_spinner_dropdown_item, freeIntervals);
                        spinnerFreeIntervals.setAdapter(adapterReservations);
                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private ArrayList<Interval> getFreeIntervals(ArrayList<Integer> occupiedIntervals, Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar calToday = Calendar.getInstance();
        boolean today = calendar.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR);

        ArrayList<Interval> freeIntervals = new ArrayList<Interval>();

        for (int i = 1; i <= 6; i++) {
            boolean exists = false;
            for (int j = 0; j < occupiedIntervals.size(); j++) {
                if (occupiedIntervals.get(j) == i) {
                    exists = true;
                }
            }

            if (!exists) {
                // Si es HOY hay que hacer comprobaciones de hora
                if(today){
                    switch (i){
                        case 1:
                            if(checkHourAndMinutes(8,15,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                        case 2:
                            if(checkHourAndMinutes(9,15,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                        case 3:
                            if(checkHourAndMinutes(10,15,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                        case 4:
                            if(checkHourAndMinutes(11,45,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                        case 5:
                            if(checkHourAndMinutes(12,45,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                        case 6:
                            if(checkHourAndMinutes(13,45,calendar)){
                                freeIntervals.add(new Interval(i));
                            }
                            break;
                    }
                }
                else{
                    freeIntervals.add(new Interval(i)); // Se añade el intervalo como hora libre
                }
            }
        }

        return freeIntervals;
    }

    private boolean checkHourAndMinutes(int hour, int minutes, Calendar calendar){
        Calendar calToday = Calendar.getInstance();
        calendar.set(calToday.YEAR, calToday.MONTH, calToday.DAY_OF_MONTH, hour, minutes);
        boolean result = calToday.getTime().before(calendar.getTime());
        return result;
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
