package com.app.garini.garini.login;

/**
 * Created by m.lagha on 07/02/2017.
 */

public class User {

    private String name,email,image;
    private int id_user;

    public User(String name, String email, String image, int id_user) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.id_user = id_user;
    }

    public int getIdUser() {
        return id_user;
    }

    public String getImage() {
        return image;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

}
