package com.example.houserentalmanagement.houseOwner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.houserentalmanagement.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.HashMap;

public class AddHouse extends AppCompatActivity {

    ImageView houseImage;
    EditText et_houseId, et_houseLocation, et_noOfRoom, et_rentPerRoom, et_houseDescription;
    Button btn_addHouse;

    private static final int STORAGE_PERMISSION_CODE = 1;
    ActivityResultLauncher<Intent> imageResultlauncher;

    StorageReference storageReference;
    public static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String imageString;
    private StorageTask uploadTask;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_house);

        houseImage = findViewById(R.id.iv_houseImage);
        et_houseId = findViewById(R.id.et_houseId);
        et_houseLocation = findViewById(R.id.et_houseLocation);
        et_noOfRoom = findViewById(R.id.et_noOfRoom);
        et_rentPerRoom = findViewById(R.id.et_rentPerRoom);
        et_houseDescription = findViewById(R.id.et_houseDescription);
        btn_addHouse = findViewById(R.id.btn_addHouse);

        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");


        houseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission();
            }
        });

        progressDialog = new ProgressDialog(AddHouse.this);
        registerImageResultLauncher();
        btn_addHouse.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Publishing Your Article");
                progressDialog.setTitle("Adding...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                String houseId = et_houseId.getText().toString();
                String houseLocation = et_houseLocation.getText().toString();
                String noOfRoom = et_noOfRoom.getText().toString();
                String rentPerRoom = et_rentPerRoom.getText().toString();
                String houseDescription = et_houseDescription.getText().toString();
                String image = imageString;


                createFood(houseId, houseLocation, noOfRoom, rentPerRoom, houseDescription, image);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createFood(String houseId, String houseLocation, String noOfRoom, String rentPerRoom, String houseDescription, String image) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(RegisterOwner.HOUSES).child(userId);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("houseId", houseId);
        hashMap.put("noOfRoom", noOfRoom);
        hashMap.put("houseDescription", houseDescription);
        hashMap.put("houseLocation", houseLocation);
        hashMap.put("rentPerRoom", rentPerRoom);
        hashMap.put("houseImage", image);
        hashMap.put("userId", userId);
        reference.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(AddHouse.this, "Article Published Successfully", Toast.LENGTH_SHORT).show();
                    imageString = "";
                } else {
                    Toast.makeText(AddHouse.this, "Article Published Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        imageResultlauncher.launch(intent);
    }
    private void registerImageResultLauncher(){
        imageResultlauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                imageUri = result.getData().getData();
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(AddHouse.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage();
                }
            }
        });
    }

    private void checkStoragePermission(){
        if(ActivityCompat.checkSelfPermission(AddHouse.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //agar permission nhi hai to request karo
            ActivityCompat.requestPermissions(AddHouse.this,new String[]{Manifest.permission.READ_MEDIA_IMAGES},STORAGE_PERMISSION_CODE);

        }else {
            openImage();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = AddHouse.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(AddHouse.this);
        pd.setMessage("Uploading...");
        pd.show();
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        try {
                            Uri downloadingUri = task.getResult();
                            Log.d("TAG", "onComplete: uri completed");
                            String mUri = downloadingUri.toString();
                            imageString = mUri;
                            Glide.with(AddHouse.this).load(imageUri).into(houseImage);
                        } catch (Exception e) {
                            Log.d("TAG1", "error Message: " + e.getMessage());
                            Toast.makeText(AddHouse.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        pd.dismiss();
                    } else {
                        Toast.makeText(AddHouse.this, "Failed here", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddHouse.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(AddHouse.this, "No image Selected", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            if (uploadTask != null && uploadTask.isInProgress()) {
//                Toast.makeText(AddHouse.this, "Upload in progress", Toast.LENGTH_SHORT).show();
//            } else {
//                uploadImage();
//            }
//        }
//    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImage();
                Toast.makeText(AddHouse.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddHouse.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}