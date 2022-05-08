package com.example.weddingshop;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface OrderCallbackInterface {
    void onSuccessfulOrderRegisterCallback();
    void onSuccessfulSpecificUserOrderFetchCallback(List<DocumentSnapshot> orders);
    void onSuccessfulOrderFetchCallback(List<DocumentSnapshot> orders);
    void onSuccessfulResultObjectificationCallback(OrderDataModel[] orders);
}
