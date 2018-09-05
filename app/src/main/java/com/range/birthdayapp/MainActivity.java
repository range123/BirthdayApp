package com.range.birthdayapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    final private static int RC_SIGN_IN = 123;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "SIGN IN SUCCESSFULL", Toast.LENGTH_SHORT).show();
                DatabaseReference df = FirebaseDatabase.getInstance().getReference("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                Users u = new Users(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), FirebaseAuth.getInstance().getCurrentUser().getEmail());
                df.setValue(u);

                Intent i = new Intent(getApplicationContext(), first_Activity.class);
                startActivity(i);
                finish();
            } else
                Toast.makeText(this, "SIGN IN FAILED", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isInternetAvailable() throws InterruptedException,IOException{
        String cmd="ping -c 1 google.com";
        return (Runtime.getRuntime().exec(cmd).waitFor()==0);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            SignInButton sign_in = (SignInButton) findViewById(R.id.sign_in_button);
            sign_in.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            });

        } else {
            //DatabaseReference df= FirebaseDatabase.getInstance().getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
            //Users u=new Users(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),FirebaseAuth.getInstance().getCurrentUser().getUid(),FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),FirebaseAuth.getInstance().getCurrentUser().getEmail());
            //df.setValue(u);
            //CALL INTENT
            try {
                if(isInternetAvailable())
                {
                    Toast.makeText(this, "Internet available", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), first_Activity.class);
                    startActivity(i);
                    finish();
                }
                else
                {
                    Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(getApplicationContext(),offlineact.class));
                    Toast.makeText(this, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), first_Activity.class);
                    startActivity(i);
                    finish();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        }
    }

}
