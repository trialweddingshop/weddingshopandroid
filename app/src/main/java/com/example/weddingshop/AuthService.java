package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AuthService extends Service {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ServiceCallbacks serviceCallbacks;
    public AuthService() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AuthService getServerInstance() {
            return AuthService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void logInUser(String email, String password){
        new LoginOperation().execute(email,password);
    }

    public void registerUser(HashMap<String, Object> regInfo){
        new RegisterOperation().execute(regInfo.get("email").toString(), regInfo.get("password").toString(),
                regInfo.get("name").toString(), regInfo.get("address").toString(), regInfo.get("city").toString(),
                regInfo.get("country").toString());
    }

    public void getCurrentUserData(){
        new GetDataOperation().execute();
    }

    public void signUserOut(){
        mAuth.signOut();
    }

    public void modifyUserData(String name, String address, String city, String country){
        new ModifyUserData().execute(name, address, city, country);
    }

    public void getIsCurrentUserAdmin(){
        new FetchIsCurrentUserAdmin().execute();
    }

    private final class FetchIsCurrentUserAdmin extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            db.collection("users").document(getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(serviceCallbacks != null){
                        Log.d(TAG, "onSuccess: ISADMIN CHECK: " + documentSnapshot.getData().get("isAdmin"));
                        serviceCallbacks.isAdminCallback(documentSnapshot.getData().get("isAdmin").equals("true"));
                    }
                }
            });
            return null;
        }
    }

    private final class ModifyUserData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, Object> userParams = new HashMap<>();
            userParams.put("name", params[0]);
            userParams.put("address", params[1]);
            userParams.put("city", params[2]);
            userParams.put("country", params[3]);
            db.collection("users").document(getCurrentUser().getUid()).update(userParams).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        if(serviceCallbacks != null){
                            serviceCallbacks.modifyUserDataCallback();
                        }
                    }
                }
            });
            return null;
        }
    }

    private final class GetDataOperation extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            db.collection("users").document(getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(serviceCallbacks != null){
                            if(task.getResult().exists()) {
                                serviceCallbacks.retrieveUserDataCallback(task.getResult().getData());
                            }
                        }
                    }else{
                        // some kind of error handling
                    }
                }
            });
            return null;
        }
    }

    private final class RegisterOperation extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            mAuth.createUserWithEmailAndPassword(params[0], params[1])
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                                HashMap<String, Object> userData = new HashMap<>();
                                userData.put("name", params[2]);
                                userData.put("address", params[3]);
                                userData.put("city", params[4]);
                                userData.put("country", params[5]);
                                userData.put("email", params[0]);
                                userData.put("isAdmin", "false");
                                db.collection("users").document(user.getUid()).set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Success");
                                        if(serviceCallbacks != null){
                                            serviceCallbacks.retrieveUserCallback(user);
                                        }
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(TAG, task.getException().toString());
                                //Toast.makeText(authActivity.this, task.getException().toString(),Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
            return null;
        }
    }

    private final class LoginOperation extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params){
            mAuth.signInWithEmailAndPassword(params[0], params[1])
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "onComplete: Request Sent");
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Request Success");
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user != null){
                                    if(serviceCallbacks != null){
                                        serviceCallbacks.retrieveUserCallback(user);
                                    }
                                }
                            } else {
                                //some kind of error handling maybe
                                Log.d(TAG, "onComplete: Request Fail");
                            }
                        }
                    });
            return null;
        }
    }
}