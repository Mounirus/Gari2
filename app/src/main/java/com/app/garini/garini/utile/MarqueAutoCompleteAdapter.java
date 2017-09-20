package com.app.garini.garini.utile;

/**
 * Created by m.lagha on 14/02/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.app.garini.garini.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akshay on 1/2/15.
 */
public class MarqueAutoCompleteAdapter extends ArrayAdapter<Marque> {

    Context context;
    int resource, textViewResourceId;
    List<Marque> items, tempItems, suggestions;

    public MarqueAutoCompleteAdapter(Context context, int resource, int textViewResourceId, List<Marque> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        tempItems = new ArrayList<Marque>(items); // this makes the difference.
        suggestions = new ArrayList<Marque>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row_marque_layout, parent, false);
        }
        Marque people = items.get(position);
        if (people != null) {
            TextView lblName = (TextView) view.findViewById(R.id.name_marque);
            if (lblName != null)
                lblName.setText(people.getName_marque());
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((Marque) resultValue).getName_marque();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Marque people : tempItems) {
                    if (people.getName_marque().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(people);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Marque> filterList = (ArrayList<Marque>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Marque people : filterList) {
                    add(people);
                    notifyDataSetChanged();
                }
            }
        }
    };
}