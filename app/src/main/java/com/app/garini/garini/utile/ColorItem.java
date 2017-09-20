package com.app.garini.garini.utile;

/**
 * Created by m.lagha on 27/06/2017.
 */

public class ColorItem {

    int hex;
    String color;

    public ColorItem(int hex, String color) {
        this.hex = hex;
        this.color = color;
    }

    public int getHex() {
        return hex;
    }

    public String getColor() {
        return color;
    }

    public void setHex(int hex) {
        this.hex = hex;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
