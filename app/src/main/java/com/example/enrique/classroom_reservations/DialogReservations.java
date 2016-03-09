package com.example.enrique.classroom_reservations;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by enrique on 23/02/16.
 */
public class DialogReservations extends DialogFragment {

    private String[] arrayIntervals = new String[]{"8:15 - 9:15","9:15 - 10:15","10:15 - 11:15","11:45 - 12:45","12:45 - 13:45","13:45 - 14:45"};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Tomamos los argumentos que se nos envían
        Bundle extras = getArguments();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // Use the Builder class for convenient dialog construction
        final LayoutInflater inflater = getActivity().getLayoutInflater();      // Para 'inflar' el layout del Diálogo
        final View layout = inflater.inflate(R.layout.dialog_reservations, null);    // Layout que se va a mostrar en el Diálogo

        ArrayList<Reservation> reservationsList = (ArrayList<Reservation>)extras.getSerializable("reservationsList");
        ArrayList<String> itemsList = new ArrayList<>();

        for (Reservation reservation:reservationsList) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String dateFormatted = formatter.format(reservation.getDate());
            String message = reservation.getName()+" reservada por "+reservation.getFirst_Name()+" a las "+arrayIntervals[reservation.getInterval()-1];
            itemsList.add(message);
        }

        // Mostrar la lista de reservar en el ListView
        ArrayAdapter<String> adapterReservations = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, itemsList);
        ListView listView = (ListView)layout.findViewById(android.R.id.list);
        listView.setAdapter(adapterReservations);

        builder.setView(layout)
                .setTitle("Reservas el día "+extras.getString("date"))
                .setPositiveButton("Ok", null);
        // Crea el objeto AlertDialog y lo devuelve
        return builder.create();
    }
}
