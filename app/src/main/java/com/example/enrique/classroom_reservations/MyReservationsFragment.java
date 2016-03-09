package com.example.enrique.classroom_reservations;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.xml.transform.Result;

import cz.msebera.android.httpclient.Header;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReservationsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView listView;
    ProgressDialog progressDialog;

    ArrayList<Reservation> itemsList = null;
    static AsyncHttpClient client = null;

    public MyReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_reservations, container, false);
        //
        listView = (ListView)view.findViewById(android.R.id.list);
        //

        // Mensaje de espera
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(getActivity(), R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando mis reservas");
        progressDialog.setContentView(v);

        // Descargar los datos de mis reservar y mostrar en ListView
        getMyReservations();

        // Suscribo el evento de pulsación corto y larga
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Abrir Activity para editar la Reservation
        final Reservation reservation = itemsList.get(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), EditReservationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("reservation", reservation);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // Abrir simple diálogo de eliminar
        showDeleteDialog(position);
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            getMyReservations();
        }
    }

    private void getMyReservations() {
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/reservations/" + MainActivity.TEACHER_ME.getTeacher_id() + "/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    itemsList = AnalyzeJSON.analyzeReservations(response);
                    ArrayAdapter<Reservation> adapterReservations = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, itemsList);
                    listView.setAdapter(adapterReservations);
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showDeleteDialog(final int position){
        final Reservation reservation = itemsList.get(position);
        new AlertDialog.Builder(getActivity())
                .setTitle("Eliminar reserva")
                .setMessage("Se va a eliminar esta reserva. ¿Está seguro?")
                .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Borrar la Reservation de la base de datos
                        try {
                            deleteReservation(reservation, position);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();}
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteReservation(final Reservation reservation, final int position) throws JSONException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("Classroom_id", reservation.getClassroom_id());
        jsonParams.put("Interval", reservation.getInterval());
        String date = DateFormat.format("yyyy-MM-dd", reservation.getDate()).toString();
        jsonParams.put("Date", date);
        RequestParams params = new RequestParams("reservation", jsonParams.toString());

        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.delete(MainActivity.HOST + "/reservations/api/reservations/" + MainActivity.TEACHER_ME.getApi_Key(), params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        showToast("Reserva eliminada");
                        itemsList.remove(position);
                        // Recargo la lista actualizada
                        ArrayAdapter<Reservation> adapterReservations = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, itemsList);
                        listView.setAdapter(adapterReservations);
                    } else {
                        showToast("No se ha podido elminar la reserva");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
