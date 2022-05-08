package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class BrowseActivity extends AppCompatActivity implements BrowseCallbackInterface, ServiceCallbacks, OrderCallbackInterface {

    private TextView tv_Name;
    private TextView tv_Description;
    private TextView tv_Cost;
    private ImageView nextButton;
    private ImageView prevButton;
    private ImageView showImage;
    private Button b_Order;
    private int pageCounter;
    private DocumentSnapshot lastDS;
    private boolean mBounded;
    private BrowseService mService;

    private boolean aBounded;
    private AuthService aService;

    private boolean oBounded;
    private OrderService oService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        tv_Cost = findViewById(R.id.TV_BrowseCost);
        tv_Description = findViewById(R.id.TV_BrowseItemDescription);
        tv_Name = findViewById(R.id.TV_BrowseItemName);
        nextButton = findViewById(R.id.IV_BrowseNext);
        prevButton = findViewById(R.id.IV_Previous);
        showImage = findViewById(R.id.Iv_BrowseImageDisplay);
        b_Order = findViewById(R.id.B_Order);
        pageCounter = 0;
        lastDS = null;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prev();
            }
        });

        b_Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, BrowseService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        Intent aIntent = new Intent(this, AuthService.class);
        bindService(aIntent, aConnection, BIND_AUTO_CREATE);

        Intent oIntent = new Intent(this, OrderService.class);
        bindService(oIntent, oConnection, BIND_AUTO_CREATE);
    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: Browse Service Disconnected");
            mBounded = false;
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Browse Service Connected");
            mBounded = true;
            BrowseService.LocalItemBinder mLocalBinder = (BrowseService.LocalItemBinder)service;
            mService = mLocalBinder.getServerInstance();
            mService.setCallbacks(BrowseActivity.this);
            next();
        }
    };

    ServiceConnection aConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: Browse Auth Service Disconnected");
            aBounded = false;
            aService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Browse Auth Service Connected");
            aBounded = true;
            AuthService.LocalBinder mLocalBinder = (AuthService.LocalBinder)service;
            aService = mLocalBinder.getServerInstance();
            aService.setCallbacks(BrowseActivity.this);

            if(aService.getCurrentUser() != null){
                b_Order.setVisibility(View.VISIBLE);
            }else{
                b_Order.setVisibility(View.INVISIBLE);
            }
        }
    };

    ServiceConnection oConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: Order Service Disconnected");
            oBounded = false;
            oService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Order Service Connected");
            oBounded = true;
            OrderService.LocalItemBinder mLocalBinder = (OrderService.LocalItemBinder)service;
            oService = mLocalBinder.getServerInstance();
            oService.setCallbacks(BrowseActivity.this);
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

        if(aBounded){
            aService.setCallbacks(null);
            unbindService(aConnection);
            aBounded = false;
        }

        if(oBounded){
            oService.setCallbacks(null);
            unbindService(oConnection);
            oBounded = false;
        }
    };

    void next(){
        mService.fetchNextItemData(lastDS);
    }

    void prev(){
        mService.fetchPrevItemData(lastDS);
    }

    void order(){
        oService.sendOrder(aService.getCurrentUser().getUid(),lastDS.getId());
    }

    @Override
    public void itemReceivedCallback(Map<String, Object> data, DocumentSnapshot ds) {
        if(!data.isEmpty()){
            tv_Name.setText(data.get("name").toString());
            tv_Description.setText((data.get("description").toString()));
            tv_Cost.setText(data.get("cost").toString() + " $");
            byte[] decodedString = Base64.decode(data.get("image").toString(), Base64.DEFAULT);
            Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString,0, decodedString.length);
            showImage.setImageBitmap(decodedImg);
            lastDS = ds;
        }
    }

    @Override
    public void itemDataIndexOutOfBoundsCallbackCallback(boolean isDirectionNext) {
        lastDS = null;
        if(isDirectionNext){
            next();
        }else{
            prev();
        }

    }

    @Override
    public void retrieveUserCallback(FirebaseUser user) {

    }

    @Override
    public void retrieveUserDataCallback(Map<String, Object> values) {

    }

    @Override
    public void modifyUserDataCallback() {

    }

    @Override
    public void isAdminCallback(boolean isAdmin) {

    }

    @Override
    public void onSuccessfulOrderRegisterCallback() {
        Toast.makeText(BrowseActivity.this, "Item order successful!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessfulSpecificUserOrderFetchCallback(List<DocumentSnapshot> orders) {

    }

    @Override
    public void onSuccessfulOrderFetchCallback(List<DocumentSnapshot> orders) {

    }

    @Override
    public void onSuccessfulResultObjectificationCallback(OrderDataModel[] orders) {

    }
}