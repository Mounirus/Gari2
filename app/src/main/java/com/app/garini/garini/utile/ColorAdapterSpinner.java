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

public class ColorAdapterSpinner extends ArrayAdapter<ColorItem> {

    ArrayList<ColorItem> liste;
    LayoutInflater inflater;
    int groupeId,id;
    Activity context;

    public ColorAdapterSpinner(Activity context, int groupeId, int id, ArrayList<ColorItem> liste) {

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
            view = inflater.inflate(R.layout.item_colors, parent, false);
        }

        TextView couleur = (TextView) view.findViewById(R.id.colortxt);

        if(position==0){
            couleur.setTag("");
            couleur.setText("");
        }else{
            couleur.setText("");
            couleur.setTag(liste.get(position).getColor());
            couleur.setBackgroundColor(liste.get(position).getHex());
        }

        return view;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public void setError(View v, CharSequence s) {
        TextView name = (TextView) v.findViewById(R.id.colortxt);
        name.setError(s);
    }
}
