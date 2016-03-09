package com.example.enrique.classroom_reservations;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class ClassroomsAndReservationsFragment extends Fragment {

    TextView txtInfoInterval;
    Button btnSave;
    Spinner spinnerClassrooms;
    Spinner spinnerFreeIntervals;
    static boolean boot = true;
    static Date selectedDate = null;
    static AsyncHttpClient client = null;
    ProgressDialog progressDialog;

    // Acaba el curso
    private final int maxDay = 24;
    private final int maxMonth = 5;
    private final int year = 2016;
    // Semana Santa
    private final int minDaySSanta = 21;
    private final int maxDaySSanta = 28;
    private final int monthSSanta = 2;
    // Semana blanca
    private final int minDaySBlanca = 22;
    private final int maxDaySBlanca = 29;
    private final int monthSBlanca = 1;

    public ClassroomsAndReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        boot = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_classrooms_and_reservations, container, false);

        // Mensaje de espera
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(getActivity(), R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando aulas y sus datos");
        progressDialog.setContentView(v);

        // Bind views
        txtInfoInterval = (TextView)view.findViewById(R.id.txtInfoInterval);
        btnSave = (Button)view.findViewById(R.id.btnSaveReservation);
        spinnerClassrooms = (Spinner)view.findViewById(R.id.spinnerClassrooms);
        spinnerFreeIntervals = (Spinner)view.findViewById(R.id.spinnerFreeIntervals);

        // Inicializa Calendario Caldroid
        final CaldroidFragment caldroidFragment = new CaldroidFragment();

        // Lista de Aulas
        getClassrooms(caldroidFragment);

        // Configuración del Caldroid
        Bundle args = new Bundle();
        final Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.MyCaldroidTheme);
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        // Se carga el fragment en el RelativeLayout
        FragmentActivity act = (FragmentActivity) getActivity();
        FragmentTransaction t = act.getSupportFragmentManager().beginTransaction();
        t.replace(R.id.caldroidContent, caldroidFragment);
        t.commit();

        // Fechas mínimas y máximas
        minAndMaxDates(caldroidFragment);

        // Click o Long Click en una fecha del Caldroid
        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                selectedDate = date; // Asigno al campo la fecha pulsada
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Fin de semanas no permitidos
                if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    txtInfoInterval.setVisibility(View.INVISIBLE);
                    spinnerFreeIntervals.setVisibility(View.INVISIBLE);
                    btnSave.setEnabled(false);
                    selectedDate = null;
                    return;
                }
                // Carga las reservas libres para el día y aula elegidos
                getReservationsByDate(date, ((Classroom) spinnerClassrooms.getSelectedItem()).getClassroom_id(), caldroidFragment);
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                super.onLongClickDate(date, view);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Fin de semanas no permitidos
                if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                    return;

                // MOSTRAR UN DIÁLOGO CON INFO DE LAS RESERVAS HECHAS PARA ESE DÍA
                getReservationsAndShowDialog(date);
            }
        });

        // Click en un Aula del Spinner
        spinnerClassrooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedDate == null)
                    return;
                Classroom classroom = (Classroom) spinnerClassrooms.getItemAtPosition(position);
                // Carga las reservas libres para el día y aula elegidos
                getReservationsByDate(selectedDate, classroom.getClassroom_id(), caldroidFragment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Click en el botón Guardar Reserva
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardo la reserva, previas comprobaciones
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                final String dateFormatted = formatter.format(selectedDate);

                final int idClassrom = ((Classroom)spinnerClassrooms.getSelectedItem()).getClassroom_id();
                final int interval = ((Interval)spinnerFreeIntervals.getSelectedItem()).get_interval();

                client = new AsyncHttpClient(true, 80, 443);
                client.setTimeout(6000);
                client.get(MainActivity.HOST + "/reservations/api/reservations/week/" + dateFormatted + "/"+idClassrom+"/"+MainActivity.TEACHER_ME.getTeacher_id()+"/"+ MainActivity.TEACHER_ME.getApi_Key(),
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
                                Log.e("ERROR", "Error!");
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                Log.e("ERROR", "Error!");
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    if (response.getString("code").equals("true") && response.getString("message").equals("ALLOWED")) {
                                        insertNewReservation(idClassrom, interval, dateFormatted, MainActivity.TEACHER_ME.getTeacher_id()); // INSERTAR LA NUEVA RESERVA
                                    } else
                                        showToast("No puede reservar más de dos horas a la semana para este aula");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });

        return view;
    }

    private void getClassrooms(final CaldroidFragment caldroigFragment){
        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/classrooms/"+MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("ERROR", "Error!!!!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Classroom> classroomsList = AnalyzeJSON.analyzeClassroomsArray(response);
                    // Cargar las reservas libres para hoy en el Spinner de reservas
                    getReservationsByDate(new Date(), classroomsList.get(0).getClassroom_id(), caldroigFragment);
                    // Cargar las aulas en el Spinner de aulas
                    ArrayAdapter<Classroom> adapterClassrooms = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, classroomsList);
                    spinnerClassrooms.setAdapter(adapterClassrooms);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getReservationsByDate(final Date date, int idClassroom, final CaldroidFragment caldroidFragment){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final String dateFormatted = formatter.format(date);

        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/reservations/" + dateFormatted + "/" + idClassroom + "/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("ERROR", "Error!");
            }

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
                        ArrayAdapter<Interval> adapterReservations = new ArrayAdapter<Interval>(getActivity(), android.R.layout.simple_spinner_dropdown_item, freeIntervals);
                        spinnerFreeIntervals.setAdapter(adapterReservations);

                        // Se muestra el Spinner y su título
                        txtInfoInterval.setVisibility(View.VISIBLE);
                        spinnerFreeIntervals.setVisibility(View.VISIBLE);
                        String[] inverseDate = dateFormatted.split("-");
                        txtInfoInterval.setText("Elige una hora del " + inverseDate[2] + "/" + inverseDate[1] + "/" + inverseDate[0]);
                        btnSave.setEnabled(true);
                    } else {
                        txtInfoInterval.setVisibility(View.INVISIBLE);
                        spinnerFreeIntervals.setVisibility(View.INVISIBLE);
                        btnSave.setEnabled(false);
                    }

                    if (boot) {
                        disableHolidays(caldroidFragment);
                        caldroidFragment.refreshView();
                        boot = false;
                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getReservationsAndShowDialog(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final String dateFormatted = formatter.format(date);

        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/reservations/" + dateFormatted + "/.*/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                //
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Reservation> reservationsList = AnalyzeJSON.analyzeReservations(response);
                    Bundle args = new Bundle();
                    args.putSerializable("reservationsList", reservationsList);
                    String[] reverseDate = dateFormatted.split("-");
                    args.putString("date", reverseDate[2] + "/" + reverseDate[1] + "/" + reverseDate[0]);

                    if (reservationsList.size() > 0) {
                        // Muestro el diálogo con la lista de reservas para ese día
                        DialogReservations dialogoInfo = new DialogReservations();
                        dialogoInfo.setArguments(args); // Se mandan los argumentos
                        dialogoInfo.show(getActivity().getFragmentManager(), "InfoDialogListener");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void insertNewReservation(int idClass, int interval, String date, int idTeacher) throws JSONException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("Classroom_id", idClass);
        jsonParams.put("Interval", interval);
        jsonParams.put("Date", date);
        jsonParams.put("Teacher_id", idTeacher);
        RequestParams params = new RequestParams("reservation", jsonParams.toString());

        client = new AsyncHttpClient(true,80, 443);
        client.setTimeout(6000);
        client.post(MainActivity.HOST + "/reservations/api/reservations/" + MainActivity.TEACHER_ME.getApi_Key(), params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true") && response.getString("status").equals("200"))
                        showToast("Reserva completada");
                    else
                        showToast("Error al guardar la reserva...");
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void minAndMaxDates(CaldroidFragment caldroidFragment){
        // Fecha mínima
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Hoy
        caldroidFragment.setMinDate(new Date(calendar.getTimeInMillis()));
        //Fecha máxima
        calendar.add(Calendar.DAY_OF_YEAR, 30); // Hoy + 30 días
        Calendar calMax = Calendar.getInstance();
        calMax.set(year, maxMonth, maxDay);
        // No se puede pasar del 24 de junio de 2016
        if (calendar.getTime().after(calMax.getTime())){
            calendar = calMax;
        }
        caldroidFragment.setMaxDate(new Date(calendar.getTimeInMillis()));
    }

    private void disableHolidays(CaldroidFragment caldroidFragment){
        ArrayList<Date> dateHolidays = new ArrayList<>();
        Calendar calHolidays = Calendar.getInstance();
        for (int i = minDaySBlanca; i <= maxDaySBlanca; i++) {
            calHolidays.set(year, monthSBlanca, i);
            dateHolidays.add(new Date(calHolidays.getTimeInMillis()));
        }
        for (int i = minDaySSanta; i <= maxDaySSanta; i++) {
            calHolidays.set(year,monthSSanta,i);
            dateHolidays.add(new Date(calHolidays.getTimeInMillis()));
        }
        caldroidFragment.setDisableDates(dateHolidays);

        // Si hoy es vacaciones no muestra info
        Calendar cal = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        int dayOfYear = calToday.get(Calendar.DAY_OF_YEAR);

        for (int i = 0; i < dateHolidays.size(); i++) {
            cal.setTime(dateHolidays.get(i));
            int day = cal.get(Calendar.DAY_OF_YEAR);
            if(day == dayOfYear){
                btnSave.setEnabled(false);
                txtInfoInterval.setVisibility(View.INVISIBLE);
                spinnerFreeIntervals.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showToast(String message){
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
