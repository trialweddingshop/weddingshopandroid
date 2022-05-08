package com.example.weddingshop;

import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public interface ServiceCallbacks {

    void retrieveUserCallback(FirebaseUser user);
    void retrieveUserDataCallback(Map<String, Object> values);
    void modifyUserDataCallback();
    void isAdminCallback(boolean isAdmin);

}
