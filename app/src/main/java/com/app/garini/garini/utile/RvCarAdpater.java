package com.app.garini.garini.utile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.garini.garini.MapsFrag;
import com.app.garini.garini.R;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.IOException;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by m.lagha on 23/03/2017.
 */

public class RvCarAdpater extends RecyclerView.Adapter<RvCarAdpater.CarViewHolder> {

    int id_voiture;
    Context context;
    private String URL = StaticValue.URL;

    public static class CarViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView marque;
        TextView couleur;
        ImageButton btnsupp;
        RadioButton cbselect;

        CarViewHolder(View itemView){
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            marque = (TextView) itemView.findViewById(R.id.txtmarque);
            couleur = (TextView) itemView.findViewById(R.id.txtcouleur);
            btnsupp = (ImageButton) itemView.findViewById(R.id.btnsupp);
            cbselect = (RadioButton) itemView.findViewById(R.id.cbselect);
        }

    }

    List<Voiture> voitures;

    public RvCarAdpater(List<Voiture> voitures, Context context){
        this.voitures = voitures;
        this.context = context;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_car,viewGroup,false);
        CarViewHolder cvh = new CarViewHolder(v);
        return cvh;
    }

    @Override
    public int getItemCount() {
        return voitures.size();
    }

    @Override
    public void onBindViewHolder(final CarViewHolder carViewHolder, final int position) {

        carViewHolder.marque.setText(voitures.get(position).marque+" "+voitures.get(position).modele);
        carViewHolder.couleur.setText(voitures.get(position).couleur);
        if(voitures.get(position).selected == 0){
            carViewHolder.cbselect.setChecked(false);
        }else{
            carViewHolder.cbselect.setChecked(true);
        }

        carViewHolder.cbselect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for (int i = 0; i < voitures.size(); i++) {
                    if (voitures.get(i).selected == 1){
                        voitures.get(i).setSelected(0);
                    }
                }
                voitures.get(position).setSelected(1);
                id_voiture = voitures.get(position).id;
                new SelectCarTask().execute(id_voiture);

            }
        });

        carViewHolder.btnsupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(voitures.get(position).selected == 0){
                    id_voiture = voitures.get(position).id;
                    new LovelyStandardDialog(context)
                            .setTopColorRes(R.color.colorPrimary)
                            .setButtonsColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_directions_car_white_24dp)
                            .setTitle("Supperimer véhicule")
                            .setMessage("vous êtes certain de vouloir supprimer ce véhicule ?")
                            .setPositiveButton("Oui", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new DeleteCarTask().execute(id_voiture);
                                    voitures.remove(position);
                                }
                            })
                            .setNegativeButton("Non", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                }
                            }).show();
                }else{
                    Toast.makeText(context,"Vous devez sélectionner un autre véhicule avant de supprimer !",Toast.LENGTH_SHORT).show();
                }

            }
        });




    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class SelectCarTask extends AsyncTask<Integer, Void, Void> {

        ProgressDialog dialog;

        protected Void doInBackground(Integer... params) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("id_voiture",params[0]+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(URL+"carSelected.php")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    response.body().string();

                } catch (@NonNull IOException e) {
                    Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
                }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Patientez un instant SVP...");
            dialog.setMessage("traitement en cours ");
            dialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            notifyDataSetChanged();
        }
    }

    class DeleteCarTask extends AsyncTask<Integer, Void, Void> {

        ProgressDialog dialog;

        protected Void doInBackground(Integer... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("id_voiture",params[0]+"")
                        .build();
                Request request = new Request.Builder()
                        .url(URL+"deleteCar.php")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                response.body().string();

            } catch (@NonNull IOException e) {
                Log.e("Json ErrorMarque", "" + e.getLocalizedMessage());
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Patientez un instant SVP...");
            dialog.setMessage("traitement en cours ");
            dialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            notifyDataSetChanged();
        }
    }
}
