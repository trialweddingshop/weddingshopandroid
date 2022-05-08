package com.example.weddingshop;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class ItemUploadService extends Service {

    private FirebaseFirestore db;
    private ItemAddCallBackInterface serviceCallbacks;
    public ItemUploadService() {
        db = FirebaseFirestore.getInstance();
    }

    IBinder mBinder = new LocalItemBinder();

    public class LocalItemBinder extends Binder {
        public ItemUploadService getServerInstance() {
            return ItemUploadService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallbacks(ItemAddCallBackInterface callbacks) {
        serviceCallbacks = callbacks;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void uploadItem(String name, String description, String cost, Bitmap image){
        Log.d(TAG, "uploadItem: ");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] currentImageInB64Bytes = baos.toByteArray();
        Log.d(TAG, "uploadItem: Conversion to byte happened");
        String b64Image = Base64.encodeToString(currentImageInB64Bytes, Base64.DEFAULT);
        Log.d(TAG, "uploadItem: Conversion to string: " + b64Image);
        new UploadItemDataTask().execute(name, description, cost, b64Image);
    }

    private final class UploadItemDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, Object> itemData = new HashMap<>();
            itemData.put("name", params[0]);
            itemData.put("description", params[1]);
            itemData.put("cost", params[2]);
            itemData.put("image", params[3]);
            db.collection("items").document().set(itemData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        if(serviceCallbacks != null){
                            Log.d(TAG, "onComplete: Successfully Uploaded Image");
                            serviceCallbacks.onItemAddedCallback();
                        }
                    }
                }
            });
            return null;
        }
    }
}