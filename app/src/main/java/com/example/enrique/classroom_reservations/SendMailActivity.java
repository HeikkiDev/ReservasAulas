package com.example.enrique.classroom_reservations;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SendMailActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressDialog progressDialog;
    ArrayList<Teacher> itemsList = null;
    static AsyncHttpClient client = null;
    static Context context;

    private ListView listView;
    private EditText etxSubject;
    private EditText etxMessage;
    private Button btnSendMail;
    private Button btnCancel;

    private final String FROM = "correo@enrique.portadaalta.info";
    private final String PASSW = "malaga2015";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        //
        this.context = this;
        listView = (ListView)findViewById(R.id.listView);
        etxSubject = (EditText)findViewById(R.id.etxSubject);
        etxMessage = (EditText)findViewById(R.id.etxMessage);
        btnSendMail = (Button)findViewById(R.id.btnSendMail);
        btnCancel = (Button)findViewById(R.id.btnCancel);

        // Color de la Action Bar
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        // Mensaje de espera
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        View v = View.inflate(this, R.layout.custom_progressdialog, null);
        ((TextView)v.findViewById(R.id.txtMessageDialog)).setText("Cargando profesores");
        progressDialog.setContentView(v);

        // Carga los profesores en el ListView
        getTeachers();

        btnSendMail.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSendMail){
            String subject = etxSubject.getText().toString();
            String message = etxMessage.getText().toString();
            if(subject.equals("")){
                showToast("Indica el asunto del email");
                return;
            }
            if(message.equals("")){
                showToast("Escriba el contenido del email");
                return;
            }

            // Enviar email a cada profesor seleccionado
            SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
            for(int i = 0; i < listView.getCount(); i++)
            {
                if(sparseBooleanArray.get(i) == true)
                {
                    Teacher teacher = (Teacher)listView.getItemAtPosition(i);
                    try {
                        sendMail(teacher);
                    }
                    catch (JSONException e) {e.printStackTrace();}
                }
            }
        }
        else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void getTeachers() {
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/teachers/" + MainActivity.TEACHER_ME.getApi_Key(), new JsonHttpResponseHandler() {
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
                    itemsList = AnalyzeJSON.analyzeAllTeachers(response);
                    ArrayAdapter<Teacher> adapterTeachers = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, itemsList);
                    listView.setAdapter(adapterTeachers);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void sendMail(Teacher teacher) throws JSONException {
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("from", FROM);
        jsonParams.put("passwd", PASSW);
        jsonParams.put("to", teacher.getEmail());
        jsonParams.put("subject", etxSubject.getText().toString());
        jsonParams.put("message", etxMessage.getText().toString());
        RequestParams params = new RequestParams("email", jsonParams.toString());

        client = new AsyncHttpClient(true,80,443);
        client.setTimeout(6000);
        client.post(MainActivity.HOST + "/reservations/api/email/" + MainActivity.TEACHER_ME.getApi_Key(), params, new JsonHttpResponseHandler() {
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
                        setResult(RESULT_OK);
                        finish();
                    } else
                        showToast("No se ha podido enviar el email");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
