package com.example.enrique.classroom_reservations;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    TextView txtUsername;
    TextView txtFirstName;
    TextView txtLastName;
    TextView txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //
        txtUsername = (TextView)findViewById(R.id.txtUserName);
        txtFirstName = (TextView)findViewById(R.id.txtName);
        txtLastName = (TextView)findViewById(R.id.txtLastName);
        txtEmail = (TextView)findViewById(R.id.txtEmail);
        //
        Bundle bundle = getIntent().getExtras();
        txtUsername.setText(bundle.getString("username"));
        txtFirstName.setText(bundle.getString("firstname"));
        txtLastName.setText(bundle.getString("lastname"));
        txtEmail.setText(bundle.getString("email"));

        // Color de la Action Bar
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
    }
}
