package com.example.enrique.classroom_reservations;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    ListView myDrawerList;
    TextView txtUsername;
    TextView txtViewProfile;
    private ActionBarDrawerToggle myDrawerToggle;
    private DrawerLayout myDrawerLayout;

    ArrayList<NavItem> myNavItemList = new ArrayList<>();

    private int currentPosition = -1;
    private final int LOGIN_CODE = 1;
    public static Teacher TEACHER_ME = null;
    public static final String HOST = "https://enriqueramos.info/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        txtUsername = (TextView)findViewById(R.id.userName);
        txtViewProfile = (TextView)findViewById(R.id.viewProfile);
        myDrawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        myDrawerList = (ListView)findViewById(R.id.navList);
        txtViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                Bundle args = new Bundle();
                args.putString("username", TEACHER_ME.getUsername());
                args.putString("firstname", TEACHER_ME.getFirst_Name());
                args.putString("lastname", TEACHER_ME.getLast_Name());
                args.putString("email", TEACHER_ME.getEmail());
                intent.putExtras(args);
                startActivity(intent);// IR A LA ACTIVITY DEL PERFIL DE USUARIO
            }
        });

        // Comprobar la sesión de usuario
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", 0); // 0 para modo privado
        String userSesion = sharedPreferences.getString("username", null);
        if(userSesion == null){ // Si no hay sesión guardada
            // Lanzo la Activity del Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_CODE);
        }
        else{
            // Asigno el usuario que tenía abierta la sesión
            int id = sharedPreferences.getInt("id", 0);
            String username = sharedPreferences.getString("username", null);
            String firstname = sharedPreferences.getString("firstname", null);
            String lastname = sharedPreferences.getString("lastname", null);
            String email = sharedPreferences.getString("email", null);
            String apikey = sharedPreferences.getString("apikey",null);
            boolean admin = sharedPreferences.getBoolean("admin", false);
            this.TEACHER_ME = new Teacher(String.valueOf(id),username,firstname,lastname,email,apikey, admin);
            txtUsername.setText(this.TEACHER_ME.getFirst_Name());
        }

        // Navigations Drawer options
        myNavItemList.add(new NavItem("Aulas y reservas","Consulta las aulas y haz reservas", R.drawable.home_icon));
        myNavItemList.add(new NavItem("Mis reservas", "Detalles sobre mis reservas", R.drawable.calendar));
        myNavItemList.add(new NavItem("Incidencias", "Info sobre las incidencias", R.drawable.alert));
        myNavItemList.add(new NavItem("", "Cerrar sesión", R.drawable.logout));

        // Relleno el Naavigation Drawer con las opciones
        DrawerListAdapter adapter = new DrawerListAdapter(this, myNavItemList);
        myDrawerList.setAdapter(adapter);

        // Evento Click de cada Drawer Item
        myDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Vamos a la página del Drawer Item pulsado
                selectItemFromDrawer(position);
            }
        });

        // Inicializo myDrawerToggle y lo enlazo a myDrawerLayout
        myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                //
                // Al ABRIRSE el Navigation Drawer Panel
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //
                // Al CERRARSE el Navigation Drawer Panel
                super.onDrawerClosed(drawerView);
            }
        };
        myDrawerLayout.setDrawerListener(myDrawerToggle);

        // Para que se muestre el icono del Hamburger Menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#ef6c00"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        if(this.TEACHER_ME != null)
            selectItemFromDrawer(0); // Muestro por defecto la página HOME
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        myDrawerToggle.syncState(); // This method syncs the state of the icon indicator with the Navigation Drawer (so hamburger or arrow)
    }

    // When Click Hamburger Menu (or Arrow Icon)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        Teacher teacher = (Teacher)bundle.getSerializable("teacher");
        this.TEACHER_ME = teacher;
        txtUsername.setText(teacher.getFirst_Name());

        // Muestro por defecto la página HOME
        selectItemFromDrawer(0);

        // Guardo la sesión para este usuario
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id",teacher.getTeacher_id());
        editor.putString("username", teacher.getUsername());
        editor.putString("firstname", teacher.getFirst_Name());
        editor.putString("lastname", teacher.getLast_Name());
        editor.putString("email", teacher.getEmail());
        editor.putString("apikey", teacher.getApi_Key());
        editor.putBoolean("admin", teacher.isAdmin());
        editor.commit(); // commit changes
    }

    /** Called when a particular item from the navigation drawer
        * is selected.
        * */
    private void selectItemFromDrawer(int position) {
        if(currentPosition == position){
            myDrawerLayout.closeDrawers();
            return;
        }
        this.currentPosition = position;

        Fragment fragment = null;

        switch (position){ // Cargar el Fragment adecuado
            case 0:
                fragment = new ClassroomsAndReservationsFragment();
                break;
            case 1:
                fragment = new MyReservationsFragment();
                break;
            case 2:
                fragment = new IncidencesFragment();
                break;
            case 3:
                closeSession(); // Cerrar sesión del usuario
                return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();

        myDrawerList.setItemChecked(position, true);
        setTitle(myNavItemList.get(position).myTitle);

        // Close the drawer
        myDrawerLayout.closeDrawers();
    }

    private void closeSession(){
        // Borro los datos del usuario logeado
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit(); // commit changes

        // Relanza la MainActivity de la aplicación
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
