package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class authActivity extends AppCompatActivity implements ServiceCallbacks{

    private boolean mIsSelectedModeLogin = true;
    private boolean isAuthenticated = false;
    private final String mStringLogin = "Login";
    private final String mStringRegister = "Register";
    private final String mStringModify = "Modify";
    private final String mStringLogout = "Logout";
    private Switch modeSwitch;
    private Button b_authRequest;
    private TextView tv_Email;
    private TextView tv_Password;
    private TextView tv_Name;
    private TextView tv_Address;
    private TextView tv_City;
    private TextView tv_Country;
    private EditText i_Name;
    private EditText i_Address;
    private EditText i_City;
    private EditText i_Country;
    private EditText i_Email;
    private EditText i_Password;

    FirebaseUser loggedInUser = null;

    boolean mBounded;
    AuthService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        modeSwitch = findViewById(R.id.S_mode);
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsSelectedModeLogin = !mIsSelectedModeLogin;switchView();
            }
        });
        b_authRequest = findViewById(R.id.B_authRequest);
        tv_Email = findViewById(R.id.TV_email);
        tv_Password = findViewById(R.id.TV_password);
        tv_Name = findViewById(R.id.TV_name);
        tv_Address = findViewById(R.id.TV_address);
        tv_City = findViewById(R.id.TV_city);
        tv_Country = findViewById(R.id.TV_country);
        i_Name = findViewById(R.id.I_name);
        i_Address = findViewById(R.id.I_address);
        i_City = findViewById(R.id.I_city);
        i_Country = findViewById(R.id.I_country);
        i_Email = findViewById(R.id.I_email);
        i_Password = findViewById(R.id.I_password);

        b_authRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsSelectedModeLogin){
                    if(!isAuthenticated) {
                        onLogin(i_Email.getText().toString(), i_Password.getText().toString());
                    }else{
                        onLogout();
                    }
                }else{
                    if(!isAuthenticated) {
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("name", i_Name.getText().toString());
                        userData.put("address", i_Address.getText().toString());
                        userData.put("city", i_City.getText().toString());
                        userData.put("country", i_Country.getText().toString());
                        userData.put("email", i_Email.getText().toString());
                        userData.put("password", i_Password.getText().toString());
                        onRegister(userData);
                    }else{
                        onModify(i_Name.getText().toString(), i_Address.getText().toString(),
                                i_City.getText().toString(), i_Country.getText().toString());
                    }
                }
            }
        });

        if(mIsSelectedModeLogin){
            modeSwitch.setText(mStringLogin);
            b_authRequest.setText(mStringLogin);
            tv_Name.setVisibility(View.INVISIBLE);
            tv_Address.setVisibility(View.INVISIBLE);
            tv_City.setVisibility(View.INVISIBLE);
            tv_Country.setVisibility(View.INVISIBLE);
            i_Name.setVisibility(View.INVISIBLE);
            i_Address.setVisibility(View.INVISIBLE);
            i_City.setVisibility(View.INVISIBLE);
            i_Country.setVisibility(View.INVISIBLE);
        }else{
            modeSwitch.setText(mStringRegister);
            b_authRequest.setText(mStringRegister);
            tv_Name.setVisibility(View.VISIBLE);
            tv_Address.setVisibility(View.VISIBLE);
            tv_City.setVisibility(View.VISIBLE);
            tv_Country.setVisibility(View.VISIBLE);
            i_Name.setVisibility(View.VISIBLE);
            i_Address.setVisibility(View.VISIBLE);
            i_City.setVisibility(View.VISIBLE);
            i_Country.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }*/
        Intent mIntent = new Intent(this, AuthService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: AuthService Disconnected");
            mBounded = false;
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: AuthServiceConnected");
            mBounded = true;
            AuthService.LocalBinder mLocalBinder = (AuthService.LocalBinder)service;
            mService = mLocalBinder.getServerInstance();
            mService.setCallbacks(authActivity.this);
            if(mService.getCurrentUser() != null){
                isAuthenticated = true;
            }
            switchView();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            mService.setCallbacks(null);
            unbindService(mConnection);
            mBounded = false;
        }
    };

    public void onLogin(String email, String password){
        mService.logInUser(email,password);
    }

    public void onRegister(HashMap<String, Object> registerInfo){
        mService.registerUser(registerInfo);
    }

    public void onModify(String name, String address, String city, String country){
        mService.modifyUserData(name, address, city, country);
    }

    public void onLogout(){
        mService.signUserOut();
        switchView();
    }

    public void getUserInfoForModify(){
        mService.getCurrentUserData();
    }

    public void switchView() {
        isAuthenticated = mService.getCurrentUser() != null;
        if (mIsSelectedModeLogin) {
            if(!isAuthenticated) {
                modeSwitch.setText(mStringLogin);
                b_authRequest.setText(mStringLogin);
                tv_Email.setVisibility(View.VISIBLE);
                tv_Password.setVisibility(View.VISIBLE);
                tv_Name.setVisibility(View.INVISIBLE);
                tv_Address.setVisibility(View.INVISIBLE);
                tv_City.setVisibility(View.INVISIBLE);
                tv_Country.setVisibility(View.INVISIBLE);
                i_Name.setVisibility(View.INVISIBLE);
                i_Address.setVisibility(View.INVISIBLE);
                i_City.setVisibility(View.INVISIBLE);
                i_Country.setVisibility(View.INVISIBLE);
                i_Password.setVisibility(View.VISIBLE);
                i_Email.setVisibility(View.VISIBLE);
            }else{
                modeSwitch.setText(mStringLogout);
                b_authRequest.setText(mStringLogout);
                tv_Email.setVisibility(View.INVISIBLE);
                tv_Password.setVisibility(View.INVISIBLE);
                tv_Name.setVisibility(View.INVISIBLE);
                tv_Address.setVisibility(View.INVISIBLE);
                tv_City.setVisibility(View.INVISIBLE);
                tv_Country.setVisibility(View.INVISIBLE);
                i_Name.setVisibility(View.INVISIBLE);
                i_Address.setVisibility(View.INVISIBLE);
                i_City.setVisibility(View.INVISIBLE);
                i_Country.setVisibility(View.INVISIBLE);
                i_Password.setVisibility(View.INVISIBLE);
                i_Email.setVisibility(View.INVISIBLE);
                i_Password.setText("");
                i_Email.setText("");
            }
        } else {
            if (isAuthenticated) {
                modeSwitch.setText(mStringModify);
                b_authRequest.setText(mStringModify);
                tv_Password.setVisibility(View.INVISIBLE);
                tv_Email.setVisibility(View.INVISIBLE);
                i_Password.setVisibility(View.INVISIBLE);
                i_Email.setVisibility(View.INVISIBLE);
            } else {
                modeSwitch.setText(mStringRegister);
                b_authRequest.setText(mStringRegister);

                tv_Password.setVisibility(View.VISIBLE);
                tv_Email.setVisibility(View.VISIBLE);
                i_Password.setVisibility(View.VISIBLE);
                i_Email.setVisibility(View.VISIBLE);

                i_Name.setText("");
                i_Address.setText("");
                i_City.setText("");
                i_Country.setText("");
                i_Password.setText("");
                i_Email.setText("");
            }

            tv_Name.setVisibility(View.VISIBLE);
            tv_Address.setVisibility(View.VISIBLE);
            tv_City.setVisibility(View.VISIBLE);
            tv_Country.setVisibility(View.VISIBLE);

            i_Name.setVisibility(View.VISIBLE);
            i_Address.setVisibility(View.VISIBLE);
            i_City.setVisibility(View.VISIBLE);
            i_Country.setVisibility(View.VISIBLE);

            if (isAuthenticated) {
                b_authRequest.setText(mStringModify);
                getUserInfoForModify();
            }
        }
    }

    @Override
    public void retrieveUserCallback(FirebaseUser user) {
        Log.d(TAG, "retrieveUser: FirebaseUser: " + user.getEmail());
        loggedInUser = user;
        isAuthenticated = true;
        switchView();
    }

    @Override
    public void retrieveUserDataCallback(Map<String, Object> values){
        i_Name.setText(values.get("name").toString());
        i_Address.setText(values.get("address").toString());
        i_City.setText(values.get("city").toString());
        i_Country.setText(values.get("country").toString());
    }

    @Override
    public void modifyUserDataCallback(){
        getUserInfoForModify();
    }

    @Override
    public void isAdminCallback(boolean isAdmin) {

    }
}