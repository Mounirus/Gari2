package com.app.garini.garini.utile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.app.garini.garini.login.LoginActivity;
import com.app.garini.garini.login.User;

/**
 * Created by m.lagha on 01/02/2017.
 */

public class UserSessionManager {

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREFER_NAME = "PrefUser";

    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    private static final String IS_USER_LOGIN_FB = "IsUserLoggedInFB";

    private static final String KEY_NAME = "name";

    private static final String KEY_EMAIL = "email";

    private static final String KEY_ID = "idUser";

    private static final String KEY_IMAGE = "image";

    private static final String KEY_FIREBASE = "firebase";

    private static final String KEY_IS_DONNER = "is_donner";

    private static final String KEY_ID_DONNER = "id_donner";

    private static final String KEY_IS_TROUVER = "is_trouver";

    private static final String KEY_ID_TROUVER = "ID_trouver";

    private static final String KEY_IS_ATTRIBUER ="is_attribuer";

    private static final String KEY_ID_ATTRIBUER ="id_attribuer";

    public UserSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME,PRIVATE_MODE);
        editor = pref.edit();

    }

    public void createUserLoginSession(int id,String name,String email,String image,boolean is_fb){

        editor.putBoolean(IS_USER_LOGIN,true);
        editor.putBoolean(IS_USER_LOGIN_FB,is_fb);
        editor.putInt(KEY_ID,id);
        editor.putString(KEY_NAME,name);
        editor.putString(KEY_EMAIL,email);
        editor.putString(KEY_IMAGE,image);
        editor.commit();
    }

    public void createDonner(int id_donner){
        editor.putBoolean(KEY_IS_DONNER,true);
        editor.putInt(KEY_ID_DONNER,id_donner);
        editor.commit();
    }
    public void deleteDonner(){
        editor.putBoolean(KEY_IS_DONNER,false);
        editor.commit();
    }

    public int getIdDonner(){
        return pref.getInt(KEY_ID_DONNER, 0);
    }
    public void setIdDonner(int id_donner){
        editor.putInt(KEY_ID_DONNER,id_donner);
        editor.commit();
    }

    public boolean isDonner(){
        return pref.getBoolean(KEY_IS_DONNER, false);
    }
    public boolean isTrouver(){
        return pref.getBoolean(KEY_IS_TROUVER, false);
    }

    public void createTrouver(int id_trouver){
        editor.putBoolean(KEY_IS_TROUVER,true);
        editor.putInt(KEY_ID_TROUVER,id_trouver);
        editor.commit();
    }
    public void deleteTrouver(){
        editor.putBoolean(KEY_IS_TROUVER,false);
        editor.commit();
    }
    public int getIdTrouver(){
        return pref.getInt(KEY_ID_TROUVER, 0);
    }
    public void setIdTrouver(int id_trouver){
        editor.putInt(KEY_ID_TROUVER,id_trouver);
        editor.commit();
    }

    public boolean isAttribuer(){
        return pref.getBoolean(KEY_IS_ATTRIBUER, false);
    }

    public void createAttribuer(int id_attribuer){
        editor.putBoolean(KEY_IS_ATTRIBUER,true);
        editor.putInt(KEY_ID_ATTRIBUER,id_attribuer);
        editor.commit();
    }
    public void deleteAttribuer(){
        editor.putBoolean(KEY_IS_ATTRIBUER,false);
        editor.commit();
    }
    public int getIdAttribuer(){
        return pref.getInt(KEY_ID_ATTRIBUER, 0);
    }

    public void setIdAttribuer(int id_attribuer){
        editor.putInt(KEY_ID_ATTRIBUER,id_attribuer);
        editor.commit();
    }

    public void FirebaseTokenSave(String firebase){
        editor.putString(KEY_FIREBASE,firebase);
        editor.commit();
    }

    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

    public boolean isUserLoggedInFb(){
        return pref.getBoolean(IS_USER_LOGIN_FB, false);
    }

    public String getFirebaseToken(){
        return pref.getString(KEY_FIREBASE, null);
    }

    public boolean checkLogin(){
        if(!this.isUserLoggedIn()){
            Intent i = new Intent(_context,LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
            return true;
        }
        return false;
    }

    public User getUserDetails(){

        String name,email,image;
        int id_user;

        id_user = pref.getInt(KEY_ID, 0);
        name = pref.getString(KEY_NAME,null);
        email = pref.getString(KEY_EMAIL,null);
        image = pref.getString(KEY_IMAGE,null);

        User user = new User(name,email,image,id_user);

        return user;
    }

    public void logoutUser(){

        editor.clear();
        editor.commit();
        Intent i = new Intent(_context,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

}
