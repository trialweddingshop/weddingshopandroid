package com.example.weddingshop;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseService extends Service {
    private FirebaseFirestore db;
    private BrowseCallbackInterface serviceCallbacks;
    public BrowseService() {
        db = FirebaseFirestore.getInstance();
    }

    IBinder mBinder = new LocalItemBinder();

    public class LocalItemBinder extends Binder {
        public BrowseService getServerInstance() {
            return BrowseService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallbacks(BrowseCallbackInterface callbacks) {
        serviceCallbacks = callbacks;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void fetchNextItemData(DocumentSnapshot ds){
        new FetchNextItemDataTask().execute(ds);
    }

    public void fetchPrevItemData(DocumentSnapshot ds){
        new FetchPreviousItemDataTask().execute(ds);
    }

    private final class FetchPreviousItemDataTask extends AsyncTask<DocumentSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DocumentSnapshot... params) {
            if(params[0] != null) {
                db.collection("items").orderBy("name", Query.Direction.DESCENDING).startAfter(params[0]).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(serviceCallbacks != null){
                            List<DocumentSnapshot> requestResults = queryDocumentSnapshots.getDocuments();
                            if(requestResults.size() > 0) {
                                serviceCallbacks.itemReceivedCallback(queryDocumentSnapshots.getDocuments().get(0).getData(), queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1));
                            }else{
                                if(serviceCallbacks != null){
                                    serviceCallbacks.itemDataIndexOutOfBoundsCallbackCallback(false);
                                }
                            }
                        }
                    }
                });
            }else{ // getting the first
                db.collection("items").orderBy("name", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(serviceCallbacks != null){
                            serviceCallbacks.itemReceivedCallback(queryDocumentSnapshots.getDocuments().get(0).getData(), queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() -1));
                        }
                    }
                });
            }
            return null;
        }
    }

    private final class FetchNextItemDataTask extends AsyncTask<DocumentSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DocumentSnapshot... params) {
            if(params[0] != null) {
                db.collection("items").orderBy("name", Query.Direction.ASCENDING).startAfter(params[0]).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(serviceCallbacks != null){
                            List<DocumentSnapshot> requestResults = queryDocumentSnapshots.getDocuments();
                            if(requestResults.size() > 0) {
                                serviceCallbacks.itemReceivedCallback(queryDocumentSnapshots.getDocuments().get(0).getData(), queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1));
                            }else{
                                if(serviceCallbacks != null){
                                    serviceCallbacks.itemDataIndexOutOfBoundsCallbackCallback(true);
                                }
                            }
                        }
                    }
                });
            }else{ // getting the first
                db.collection("items").orderBy("name", Query.Direction.ASCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(serviceCallbacks != null){
                            serviceCallbacks.itemReceivedCallback(queryDocumentSnapshots.getDocuments().get(0).getData(), queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() -1));
                        }
                    }
                });
            }
            return null;
        }
    }
}