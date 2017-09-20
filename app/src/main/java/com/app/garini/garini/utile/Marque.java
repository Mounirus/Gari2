package com.app.garini.garini.utile;

/**
 * Created by m.lagha on 12/02/2017.
 */

public class Marque {

    private int id_marque;
    private String name_marque;

    public Marque(int id_marque, String name_marque) {
        this.id_marque = id_marque;
        this.name_marque = name_marque;
    }

    public int getId_marque() {
        return id_marque;
    }

    public void setId_marque(int id_marque) {
        this.id_marque = id_marque;
    }

    public String getName_marque() {
        return name_marque;
    }

    public void setName_marque(String name_marque) {
        this.name_marque = name_marque;
    }
}
