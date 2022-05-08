package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderService extends Service {
    private FirebaseFirestore db;
    private OrderCallbackInterface serviceCallbacks;
    public OrderService() {
        db = FirebaseFirestore.getInstance();
    }

    IBinder mBinder = new LocalItemBinder();

    public class LocalItemBinder extends Binder {
        public OrderService getServerInstance() {
            return OrderService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallbacks(OrderCallbackInterface callbacks) {
        serviceCallbacks = callbacks;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void sendOrder(String userId, String itemId){
        new SendOrderTask().execute(userId, itemId);
    }

    public void getAllOrders(){
        new FetchOrdersTask().execute();
    }

    public void getOrdersFortUser(String userId){
        Log.d(TAG, "getOrdersFortUser: Order service getOrders for user called");
        new FetchOrdersForUserTask().execute(userId);
    }

    public void resolveOrderObjects(List<DocumentSnapshot> orders){
        new ResolveOrderItemsTask().execute(orders);
    }

    private final class ResolveOrderItemsTask extends AsyncTask<List<DocumentSnapshot>, Void, Void>{

        @Override
        protected Void doInBackground(List<DocumentSnapshot>... lists) {
            Log.d(TAG, "doInBackground: Resolve objects called");
            ArrayList<OrderDataModel> orderModels = new ArrayList<>();
            final Integer[] successCounter = {0};
            for (DocumentSnapshot ds : lists[0]) {
                Log.d(TAG, "doInBackground: Item Resolve Request sent");
                OrderDataModel tmp = new OrderDataModel();
                db.collection("users").document(ds.get("userId").toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        tmp.userName = documentSnapshot.get("name").toString();
                        Log.d(TAG, "onSuccess: Item Resolve Username found");
                        db.collection("items").document(ds.get("itemId").toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                tmp.itemName = documentSnapshot.get("name").toString();
                                Log.d(TAG, "onSuccess: ITEM NAME IS: " + tmp.itemName);
                                tmp.cost = documentSnapshot.get("cost").toString();
                                orderModels.add(tmp);
                                successCounter[0]++;
                                Log.d(TAG, "onSuccess: Item resolve Item details are found");
                                if(successCounter[0] == lists[0].size()){
                                    if(serviceCallbacks != null){
                                        Log.d(TAG, "onSuccess: Item resolve results are sent to the Activity");
                                        serviceCallbacks.onSuccessfulResultObjectificationCallback(orderModels.toArray(new OrderDataModel[0]));
                                    }
                                }

                            }
                        });
                    }
                });
            }
            return null;
        }
    }

    private final class SendOrderTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, Object> orderData = new HashMap<>();
            orderData.put("userId", params[0]);
            orderData.put("itemId", params[1]);
            db.collection("orders").document().set(orderData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if(serviceCallbacks != null){
                        serviceCallbacks.onSuccessfulOrderRegisterCallback();
                    }
                }
            });
            return null;
        }
    }

    private final class FetchOrdersForUserTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Log.d(TAG, "doInBackground: FetchOrdersForUserCalled");
            db.collection("orders").whereEqualTo("userId", params[0]).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        if(serviceCallbacks != null){
                            Log.d(TAG, "onSuccess: Fetch Order For User Success");
                            serviceCallbacks.onSuccessfulSpecificUserOrderFetchCallback(queryDocumentSnapshots.getDocuments());
                        }
                    }
                }
            });
            return null;
        }
    }
    private final class FetchOrdersTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            db.collection("orders").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        if(serviceCallbacks != null){
                            serviceCallbacks.onSuccessfulOrderFetchCallback(queryDocumentSnapshots.getDocuments());
                        }
                    }
                }
            });
            return null;
        }
    }
}