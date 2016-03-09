package com.example.enrique.classroom_reservations;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    TextView txtInfoUser;
    TextView txtInfoPassword;
    EditText etxUser;
    EditText etxPassword;
    Button btnLogin;

    static AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        //
        txtInfoUser = (TextView)findViewById(R.id.txtUser);
        txtInfoPassword = (TextView)findViewById(R.id.txtPassword);
        etxUser = (EditText)findViewById(R.id.etxUser);
        etxPassword = (EditText)findViewById(R.id.etxPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        //

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        etxUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    txtInfoUser.setVisibility(View.VISIBLE);
                else
                    txtInfoUser.setVisibility(View.INVISIBLE);
            }
        });

        etxPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    txtInfoPassword.setVisibility(View.VISIBLE);
                else
                    txtInfoPassword.setVisibility(View.INVISIBLE);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }

    private void Login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        final String username = etxUser.getText().toString();
        String password = etxPassword.getText().toString();

        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(6000);
        client.get(MainActivity.HOST + "/reservations/api/teachers/login", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.dismiss();
                onLoginFailed();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(response.getString("code").equals("true") && response.getString("message").equals("Login completed")){
                        Teacher teacher = AnalyzeJSON.analyzeTeacher(response);
                        onLoginSuccess(teacher);
                    }
                    else
                        onLoginFailed();
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String email = etxUser.getText().toString();
        String password = etxPassword.getText().toString();

        // OJO: Muy interesante esta forma de validar email. A mí también me vale con un nombre de usuario cualquiera...
        //if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        if(email.isEmpty()) {
            etxUser.setError("Enter a valid user or email address");
            valid = false;
        } else {
            etxUser.setError(null);
        }

        if (password.isEmpty() || password.length() < 8) {
            etxPassword.setError("More than 8 alphanumeric characters are required");
            valid = false;
        } else {
            etxPassword.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess(Teacher teacher) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("teacher", teacher);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }
}
