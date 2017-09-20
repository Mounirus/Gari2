package com.app.garini.garini.utile;

/**
 * Created by m.lagha on 23/03/2017.
 */

public class Voiture {

    String marque;
    String modele;
    String couleur;
    int selected;
    int id;

    public Voiture(String marque, String modele, String couleur, int selected, int id) {
        this.marque = marque;
        this.modele = modele;
        this.couleur = couleur;
        this.selected = selected;
        this.id = id;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
