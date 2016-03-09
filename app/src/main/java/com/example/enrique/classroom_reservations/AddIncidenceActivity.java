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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class AddIncidenceActivity extends AppCompatActivity implements View.OnClickListener {

    static Context context;
    static AsyncHttpClient client;
    ProgressDialog progressDialog;

    private Spinner spinnerClassrooms;
    private EditText etxDescription;
    private Button btnSave;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incidence);
        //
        this.context = this;
        etxDescription = (EditText)findViewById(R.id.etxDescription);
        spinnerClassrooms = (Spinner)findViewById(R.id.spinnerClass);
        btnSave = (Button)findViewById(R.id.btnSaveIncidence);
        btnCancel = (Button)findViewById(R.id.btnCancel);

        // Color de la Action Bar
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        // Mensaje de espera
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(this, R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando aulas");
        progressDialog.setContentView(v);

        // Cargar Aulas en el Spinner
        getClassrooms();

        // Click de los botones
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSaveIncidence){
            Classroom classroom = (Classroom)spinnerClassrooms.getSelectedItem();
            try {
                if(etxDescription.getText().toString().equals("")){
                    showToast("Describa la incidencia antes de guardarla");
                    return;
                }
                insertNewIncidence(classroom);
            }
            catch (JSONException e) {e.printStackTrace();}
        }
        else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void getClassrooms(){
        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/classrooms/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
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
                    if(response.getString("code").equals("true")){
                        ArrayList<Classroom> classroomsList = AnalyzeJSON.analyzeClassroomsArray(response);
                        ArrayAdapter<Classroom> adapterClassrooms = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, classroomsList);
                        spinnerClassrooms.setAdapter(adapterClassrooms);
                    }
                    progressDialog.dismiss();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void insertNewIncidence(Classroom classroom) throws JSONException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("Classroom_id", classroom.getClassroom_id());
        jsonParams.put("Description", etxDescription.getText().toString());
        RequestParams params = new RequestParams("incidence", jsonParams.toString());

        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.post(MainActivity.HOST + "/reservations/api/incidences/" + MainActivity.TEACHER_ME.getApi_Key(), params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("ERROR", "Error!!!!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.e("ERROR", "Error!!!!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("code").equals("true")) {
                        showToast("Incidencia añadida correctamente");
                        setResult(RESULT_OK);
                        finish();
                    } else
                        showToast("No se ha podido añadir la incidencia");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
