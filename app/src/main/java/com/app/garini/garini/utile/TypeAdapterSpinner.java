package com.app.garini.garini.utile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

public class TypeAdapterSpinner extends ArrayAdapter<Types> {

    ArrayList<Types> liste;
    LayoutInflater inflater;
    int groupeId,id;
    Activity context;

    public TypeAdapterSpinner(Activity context, int groupeId, int id, ArrayList<Types> liste) {

        super(context,groupeId,id,liste);
        this.context = context;
        this.liste = liste;
        this.groupeId = groupeId;
        this.id = id;

    }

    public View getView(int position,View convertView,ViewGroup parent){
        View view = convertView;
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_types, parent, false);
        }

        TextView type = (TextView) view.findViewById(R.id.typetxt);
        type.setText(liste.get(position).getType());

        return view;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public void setError(View v, CharSequence s) {
        TextView name = (TextView) v.findViewById(R.id.typetxt);
        name.setError(s);
    }
}
