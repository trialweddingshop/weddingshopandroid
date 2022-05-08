package com.example.weddingshop;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public interface BrowseCallbackInterface {

    void itemReceivedCallback(Map<String, Object> data, DocumentSnapshot lastVisible);
    void itemDataIndexOutOfBoundsCallbackCallback(boolean isDirectionNext);
}
