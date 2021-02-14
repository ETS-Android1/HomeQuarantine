package com.unitbv.quarantineapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {

    // declare objects
    private Intent intent = null;
    private Bundle bundle = null;

    private FirebaseFirestore db = null;

    private Users currentUser = null;

    private ImageView profilePic = null;
    private TextView userName = null;
    private TextView userStatus = null;
    private TextView userEmail = null;
    private TextView userPhone = null;
    private TextView userAddress = null;
    private TextView userSSN = null;
    private Button userEditBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_user_profile);

        // wire objects with widgets
        profilePic = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userStatus = findViewById(R.id.userStatus);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);
        userAddress = findViewById(R.id.userAddress);
        userSSN = findViewById(R.id.userSSN);
        userEditBtn = findViewById(R.id.userEditBtn);

        // get data from bundle
        intent = getIntent();
        bundle = intent.getExtras();
        currentUser = (Users) bundle.getSerializable("currentUser");

        // populate widgets
        if(!currentUser.getProfilePic().equals(""))
            Picasso.get().load(currentUser.getProfilePic()).into(profilePic);
        else
            Picasso.get().load(R.drawable.avatar).into(profilePic);
        userName.setText(currentUser.getFullName());
        userStatus.setText("Status: " + currentUser.getStatus());
        userEmail.setText(currentUser.getEmail());
        userPhone.setText(currentUser.getPhoneNumber());
        userAddress.setText(currentUser.getStreet() + "\n" +  currentUser.getCity() + ", " +  currentUser.getCountry());
        userSSN.setText(currentUser.getSerieBuletin() + " | " + currentUser.getCnp());

        // TODO: !!! SET EDIT USER INFO BUTTON LISTENER !!!
    }
}