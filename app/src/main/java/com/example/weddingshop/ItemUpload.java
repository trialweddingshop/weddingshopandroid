package com.example.weddingshop;
import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.IOException;


public class ItemUpload extends AppCompatActivity implements ItemAddCallBackInterface{

    public final static int PICK_PHOTO_CODE = 1046;
    private EditText i_ItemName;
    private EditText i_ItemDescription;
    private EditText i_Cost;
    private Button b_Submit;
    private ImageView IV_ItemImage;
    private boolean mBounded;
    private ItemUploadService mService;
    private Bitmap currentImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_upload);
        i_ItemName = findViewById(R.id.I_ItemName);
        i_ItemDescription = findViewById(R.id.I_ItemDescription);
        i_Cost = findViewById(R.id.I_Cost);
        b_Submit = findViewById(R.id.B_SubmitItem);
        IV_ItemImage = findViewById(R.id.IV_ItemImage);


        IV_ItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

        b_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.uploadItem(i_ItemName.getText().toString(), i_ItemDescription.getText().toString(),
                       i_Cost.getText().toString(), currentImage);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }*/
        Intent mIntent = new Intent(this, ItemUploadService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ItemUploadService Disconnected");
            mBounded = false;
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ItemUpload Service Connected");
            mBounded = true;
            ItemUploadService.LocalItemBinder mLocalBinder = (ItemUploadService.LocalItemBinder)service;
            mService = mLocalBinder.getServerInstance();
            mService.setCallbacks(ItemUpload.this);
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

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "onPickPhoto: random if is true");
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);
            currentImage = selectedImage;

            // Load the selected image into a preview
            IV_ItemImage.setImageBitmap(selectedImage);
        }
    }

    public void resetFields(){
        i_ItemName.setText("");
        i_ItemDescription.setText("");
        i_Cost.setText("");
        IV_ItemImage.setImageBitmap(null);
    }

    @Override
    public void onItemAddedCallback() {
        Toast.makeText(ItemUpload.this, "Item added successfully", Toast.LENGTH_SHORT).show();
        resetFields();
    }
}