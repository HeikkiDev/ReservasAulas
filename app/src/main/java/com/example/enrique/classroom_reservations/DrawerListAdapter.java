package com.example.enrique.classroom_reservations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by enrique on 21/02/16.
 */
public class DrawerListAdapter extends BaseAdapter {

    Context myContext;
    ArrayList<NavItem> myNavItems;

    public DrawerListAdapter(Context context, ArrayList<NavItem> list){
        myContext = context;
        myNavItems = list;
    }

    @Override
    public int getCount() {
        return myNavItems.size();
    }

    @Override
    public Object getItem(int position) {
        return myNavItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.drawer_item, null);
        }
        else{
            view = convertView;
        }

        TextView txtTitle = (TextView)view.findViewById(R.id.title);
        TextView txtSubtitle = (TextView)view.findViewById(R.id.subTitle);
        ImageView iconView = (ImageView)view.findViewById(R.id.icon);

        txtTitle.setText(myNavItems.get(position).myTitle);
        txtSubtitle.setText(myNavItems.get(position).mySubtitle);
        iconView.setImageResource(myNavItems.get(position).myIcon);

        return view;
    }
}
