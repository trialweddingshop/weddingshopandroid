package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity implements OrderCallbackInterface, ServiceCallbacks {

    private boolean oBounded;
    private OrderService oService;
    private boolean aBounded;
    private AuthService aService;
    private boolean isCurrentUserAdmin = false;
    private List<DocumentSnapshot> results;
    private OrderDataModel[] orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
    }
    public void onStart() {
        super.onStart();

        Intent oIntent = new Intent(this, OrderService.class);
        bindService(oIntent, oConnection, BIND_AUTO_CREATE);

        Intent aIntent = new Intent(this, AuthService.class);
        bindService(aIntent, aConnection, BIND_AUTO_CREATE);
    }

    protected void onStop() {
        super.onStop();
        if(aBounded) {
            aService.setCallbacks(null);
            unbindService(aConnection);
            aBounded = false;
        }
        if(oBounded) {
            oService.setCallbacks(null);
            unbindService(oConnection);
            oBounded = false;
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
            aService.setCallbacks(OrdersActivity.this);
            aService.getIsCurrentUserAdmin();
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
            oService.setCallbacks(OrdersActivity.this);
        }
    };

    void getTableData(){
        Log.d(TAG, "getTableData: TableData called");
        if(isCurrentUserAdmin){
            Log.d(TAG, "getTableData: USER IS ADMIN AND ALL ORDERS ARE QUERIED!");
            oService.getAllOrders();
        }else{
            oService.getOrdersFortUser(aService.getCurrentUser().getUid());
        }
    }

    void fillTable(OrderDataModel[] orderObjs){
        try{
            JSONArray jArray = new JSONArray();
            for(OrderDataModel odm : orderObjs) {
                JSONObject obj = new JSONObject();
                obj.put("userName", odm.userName);
                obj.put("itemName", odm.itemName);
                obj.put("cost", odm.cost);
                jArray.put(obj);
            }

            TableLayout tv=(TableLayout) findViewById(R.id.TableLayout);
            tv.removeAllViewsInLayout();
            int flag=1;

            // when i=-1, loop will display heading of each column
            // then usually data will be display from i=0 to jArray.length()
            for(int i=-1;i<jArray.length();i++){

                TableRow tr=new TableRow(OrdersActivity.this);

                tr.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // this will be executed once
                if(flag==1){

                    TextView b3=new TextView(OrdersActivity.this);
                    b3.setText("Name");
                    b3.setTextColor(Color.BLUE);
                    b3.setTextSize(15);
                    tr.addView(b3);

                    TextView b4=new TextView(OrdersActivity.this);
                    b4.setPadding(10, 0, 0, 0);
                    b4.setTextSize(15);
                    b4.setText("Item Name");
                    b4.setTextColor(Color.BLUE);
                    tr.addView(b4);

                    TextView b5=new TextView(OrdersActivity.this);
                    b5.setPadding(10, 0, 0, 0);
                    b5.setText("Cost");
                    b5.setTextColor(Color.BLUE);
                    b5.setTextSize(15);
                    tr.addView(b5);
                    tv.addView(tr);

                    if(isCurrentUserAdmin){
                        TextView b6=new TextView(OrdersActivity.this);
                        b5.setPadding(10, 0, 0, 0);
                        b5.setText("Shipped?");
                        b5.setTextColor(Color.BLUE);
                        b5.setTextSize(15);
                        tr.addView(b6);
                        tv.addView(tr);
                    }

                    final View vline = new View(OrdersActivity.this);
                    vline.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2));
                    vline.setBackgroundColor(Color.BLUE);
                    tv.addView(vline); // add line below heading
                    flag=0;
                } else {
                    JSONObject json_data = jArray.getJSONObject(i);

                    TextView b=new TextView(OrdersActivity.this);
                    String str=json_data.getString("userName");
                    b.setText(str);
                    b.setTextColor(Color.RED);
                    b.setTextSize(15);
                    tr.addView(b);

                    TextView b1=new TextView(OrdersActivity.this);
                    b1.setPadding(10, 0, 0, 0);
                    b1.setTextSize(15);
                    String str1=json_data.getString("itemName");
                    b1.setText(str1);
                    b1.setTextColor(Color.RED);
                    tr.addView(b1);

                    TextView b2=new TextView(OrdersActivity.this);
                    b2.setPadding(10, 0, 0, 0);
                    String str2=json_data.getString("cost") + " $";
                    b2.setText(str2);
                    b2.setTextColor(Color.RED);
                    b2.setTextSize(15);
                    tr.addView(b2);

                    if(isCurrentUserAdmin){
                        Button b_shipped = new Button(OrdersActivity.this);
                        b_shipped.setPadding(10,0,0,0);
                        b_shipped.setText("Shipped");
                        b_shipped.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }

                    tv.addView(tr);
                    final View vline1 = new View(OrdersActivity.this);
                    vline1.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                    vline1.setBackgroundColor(Color.WHITE);
                    tv.addView(vline1);  // add line below each row
                }
            }
        }catch(JSONException e){
            Log.e("log_tag", "Error parsing data " + e.toString());
            Toast.makeText(getApplicationContext(), "JsonArray fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccessfulOrderRegisterCallback() {

    }

    @Override
    public void onSuccessfulSpecificUserOrderFetchCallback(List<DocumentSnapshot> orders) {
        results = orders;
        Log.d(TAG, "onSuccessfulSpecificUserOrderFetchCallback: Get results are at the activity");
        oService.resolveOrderObjects(orders);
    }

    @Override
    public void onSuccessfulOrderFetchCallback(List<DocumentSnapshot> orders) {
        results = orders;
        oService.resolveOrderObjects(orders);
    }

    @Override
    public void onSuccessfulResultObjectificationCallback(OrderDataModel[] orders) {
        this.orders = orders;
        fillTable(orders);
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
        isCurrentUserAdmin = isAdmin;
        getTableData();
    }
}