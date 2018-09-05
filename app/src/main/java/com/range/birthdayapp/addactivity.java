package com.range.birthdayapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addactivity extends AppCompatActivity {
    Uri currImageURI;
    ImageView pic;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    TextView url;
    String download;
    int SELECT_PHONE_NUMBER = 5;
    EditText phnedit = null;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PHONE_NUMBER) {
                Uri contacturi = data.getData();
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = getApplicationContext().getContentResolver().query(contacturi, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int numberindex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(numberindex);
                    phnedit.setText(number.replaceAll("\\s+", ""));
                }
            }

            if (requestCode == 1) {
                EditText nameedit = (EditText) findViewById(R.id.name);

                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                currImageURI = data.getData();
                pic.setImageURI(currImageURI);
                firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReferenceFromUrl("gs://birthdayapp-34806.appspot.com");
                final StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + "/" + nameedit.getText().toString() + ".jpeg");
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

    Calendar c = null;
    EditText dobedit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addactivity);
        final EditText nameedit = (EditText) findViewById(R.id.name);
        dobedit = (EditText) findViewById(R.id.dob);
        final Button contact = (Button) findViewById(R.id.contactpick);
        url = (TextView) findViewById(R.id.url);
        pic = (ImageView) findViewById(R.id.imgview);
        phnedit = (EditText) findViewById(R.id.phone);
        Button photo = (Button) findViewById(R.id.photobut);
        Button post = (Button) findViewById(R.id.post);
        Button datepick = (Button) findViewById(R.id.bdaypick);
        final Intent i=getIntent();

        c = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                c.set(Calendar.YEAR, i);
                c.set(Calendar.MONTH, i1);
                c.set(Calendar.DAY_OF_MONTH, i2);
                updateLabel();

            }
        };
        datepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(addactivity.this, date, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        //contact picker
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(i, SELECT_PHONE_NUMBER);
            }
        });
        //photo picker
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });

        final String downloaduri = download;
        final String r=i.getStringExtra("class");


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = nameedit.getText().toString();
                final String dob = dobedit.getText().toString();
                final String phone = phnedit.getText().toString();
                final String downloaduri = download;
                String uid = null;
                if(!r.equals("default"))
                    uid=r;
                else
                    uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!url.getText().toString().equals("URI available")) {
                    String def="https://firebasestorage.googleapis.com/v0/b/birthdayapp-34806.appspot.com/o/default%2Fdefaultpic.png?alt=media&token=cf644d60-48e8-43bb-b90a-e79cd2021499";
                    DatabaseReference df = FirebaseDatabase.getInstance().getReference("Birthdays");
                    String pid = df.push().getKey();
                    birthdaypost b = new birthdaypost(name, dob, phone, def, uid, pid);
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

    private void updateLabel() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat sd = new SimpleDateFormat(format, Locale.ENGLISH);
        dobedit.setText(sd.format(c.getTime()));

    }
}
