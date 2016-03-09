package com.example.enrique.classroom_reservations;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class IncidencesFragment extends Fragment implements AdapterView.OnItemLongClickListener, View.OnClickListener {

    ProgressDialog progressDialog;
    ArrayList<Incidence> itemsList = null;
    static AsyncHttpClient client = null;

    private ListView listView;
    private LinearLayout linearEmail;
    private LinearLayout linearAddIncidence;
    private RelativeLayout adminContainer;
    private LinearLayout listViewContainer;

    private final int ADD_CODE = 0;
    private final int EMAIL_CODE = 1;

    public IncidencesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_incidences, container, false);
        //
        listView = (ListView)view.findViewById(android.R.id.list);
        linearEmail = (LinearLayout)view.findViewById(R.id.linearEmail);
        linearAddIncidence = (LinearLayout)view.findViewById(R.id.linearAddIncidence);
        adminContainer = (RelativeLayout)view.findViewById(R.id.adminContainer);
        listViewContainer = (LinearLayout)view.findViewById(R.id.linearContainer);
        //
        // Mensaje de espera
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(getActivity(), R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando las incidencias");
        progressDialog.setContentView(v);

        // Descargar los datos de mis reservar y mostrar en ListView
        getIncidences();

        // Suscribo el evento de pulsación larga
        listView.setOnItemLongClickListener(this);

        // Compruebo si soy administrador
        if(MainActivity.TEACHER_ME.isAdmin()){
            linearEmail.setOnClickListener(this);
            linearAddIncidence.setOnClickListener(this);
        }
        else{
            adminContainer.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            listViewContainer.setLayoutParams(lp);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.linearEmail){
            // Abrir Avtivity para mandar email a los profesores elegidos en una lista
            Intent intent = new Intent(getActivity().getApplicationContext(), SendMailActivity.class);
            startActivityForResult(intent, EMAIL_CODE);
        }
        else if(v.getId() == R.id.linearAddIncidence){
            // Abrir Activity para añadir Incidence
            Intent intent = new Intent(getActivity().getApplicationContext(), AddIncidenceActivity.class);
            startActivityForResult(intent, ADD_CODE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // Compruebo si soy administrador
        if(MainActivity.TEACHER_ME.isAdmin()){
            // Abrir simple diálogo de elminar
            showDeleteDialog(position);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == ADD_CODE){
                showToast("Incidencia añadida correctamente");
                getIncidences(); // Actualiza las incidencias
            }
            else if(requestCode == EMAIL_CODE){
                showToast("Email enviado correctamente");
            }
        }
    }

    private void getIncidences() {
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/incidences/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
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
                    itemsList = AnalyzeJSON.analyzeIncidences(response);
                    ArrayAdapter<Incidence> adapterIncidences = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, itemsList);
                    listView.setAdapter(adapterIncidences);
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showDeleteDialog(final int position){
        final Incidence incidence = itemsList.get(position);
        new AlertDialog.Builder(getActivity())
                .setTitle("Eliminar incidencia")
                .setMessage("Se va a eliminar esta incidencia. ¿Está seguro?")
                .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Borrar la Reservation de la base de datos
                        try {
                            deleteIncidence(incidence, position);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();}
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteIncidence(final Incidence incidence, final int position) throws JSONException {
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.delete(MainActivity.HOST + "/reservations/api/incidences/" + incidence.getId() + "/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {

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
                        showToast("Incidencia eliminada");
                        itemsList.remove(position);
                        // Recargo la lista actualizada
                        ArrayAdapter<Incidence> adapterIncidences = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, itemsList);
                        listView.setAdapter(adapterIncidences);
                    } else {
                        showToast("No se ha podido elminar la incidencia");
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
