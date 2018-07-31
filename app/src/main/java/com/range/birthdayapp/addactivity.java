package com.range.birthdayapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class addactivity extends AppCompatActivity {
    Uri currImageURI;
    ImageView pic;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    TextView url;
    String download;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                EditText nameedit=(EditText)findViewById(R.id.name);

                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                currImageURI = data.getData();
                pic.setImageURI(currImageURI);
                firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReferenceFromUrl("gs://birthdayapp-34806.appspot.com");
                final StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "/" + nameedit.getText().toString()+".jpeg");
                final UploadTask uploadTask = ref.putFile(currImageURI);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Toast.makeText(addactivity.this, "Upload Successfull", Toast.LENGTH_SHORT).show();
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    url.setText("URI available");
                                    download = new String(downloadUri.toString());
                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(addactivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addactivity);
        final EditText nameedit = (EditText) findViewById(R.id.name);
        final EditText dobedit = (EditText) findViewById(R.id.dob);
        final EditText phnedit = (EditText) findViewById(R.id.phone);
        url = (TextView) findViewById(R.id.url);
        pic = (ImageView) findViewById(R.id.imgview);
        Button photo = (Button) findViewById(R.id.photobut);
        Button post = (Button) findViewById(R.id.post);

        //firebase starts here

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String downloaduri = download;
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = nameedit.getText().toString();
                final String dob = dobedit.getText().toString();
                final String phone = phnedit.getText().toString();
                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String downloaduri = download;
                if (!url.getText().toString().equals("URI available"))
                    Toast.makeText(addactivity.this, "Wait till picture is uploaded or check your connection and retry", Toast.LENGTH_SHORT).show();
                else {
                    DatabaseReference df = FirebaseDatabase.getInstance().getReference("Birthdays");
                    String pid = df.push().getKey();
                    birthdaypost b = new birthdaypost(name, dob, phone, downloaduri, uid, pid);
                    df.child(pid).setValue(b).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(addactivity.this, "Birthday Added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(addactivity.this, "Task Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }
            }
        });


    }
}