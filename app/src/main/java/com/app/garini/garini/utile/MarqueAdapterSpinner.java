package com.app.garini.garini.utile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.garini.garini.R;

import java.util.ArrayList;

/**
 * Created by m.lagha on 12/02/2017.
 */

public class MarqueAdapterSpinner extends ArrayAdapter<Marque> {

    ArrayList<Marque> liste;
    LayoutInflater inflater;
    int groupeId;
    Activity context;

    public MarqueAdapterSpinner(Activity context, int groupeId, int id, ArrayList<Marque> liste) {

        super(context,id,liste);
        this.liste = liste;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groupeId = groupeId;

    }

    public View getView(int position,View convertView,ViewGroup parent){
        View marqueView = inflater.inflate(groupeId,parent,false);

        TextView marque_name = (TextView) marqueView.findViewById(R.id.name_marque);
        marque_name.setText(liste.get(position).getName_marque());

        //TextView marque_id = (TextView) marqueView.findViewById(R.id.id_marque);
        //marque_id.setText(liste.get(position).getId_marque()+"");

        return marqueView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
