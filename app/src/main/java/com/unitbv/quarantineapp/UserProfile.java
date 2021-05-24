package com.unitbv.quarantineapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserProfile extends AppCompatActivity {

    // declare objects
    private Intent intent = null;
    private Bundle bundle = null;

    private FirebaseFirestore db = null;
    private FirebaseAuth auth = null;

    private Users currentUser = null;

    private ImageView profilePic = null;
    private TextView userName = null;
    private TextView userStatus = null;
    private TextView userEmail = null;
    private TextView userPhone = null;
    private TextView userAddress = null;
    private TextView userSSN = null;
    private Button userEditBtn = null;

    private TextView notify = null;
    private TextView checkID = null;

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
        notify = findViewById(R.id.notify);
        checkID = findViewById(R.id.checkID);

        // get data from bundle
        intent = getIntent();
        bundle = intent.getExtras();
        currentUser = (Users) bundle.getSerializable("currentUser");

        // populate widgets
        if (!currentUser.getProfilePic().equals(""))
            Picasso.get().load(currentUser.getProfilePic()).into(profilePic);
        else
            Picasso.get().load(R.drawable.avatar).into(profilePic);
        userName.setText(currentUser.getFullName());
        userStatus.setText("Status: " + currentUser.getStatus());
        userEmail.setText(currentUser.getEmail());
        userPhone.setText(currentUser.getPhoneNumber());
        userAddress.setText(currentUser.getStreet() + "\n" + currentUser.getCity() + ", " + currentUser.getCountry());
        userSSN.setText(currentUser.getSerieBuletin() + " | " + currentUser.getCnp());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final Api api = retrofit.create(Api.class);

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendNotification sendNotification = new SendNotification("Please take the call in order to check you in!", "Quarantine Check", "OPEN_VIDEOCALL");
                RequestNotification requestNotification = new RequestNotification();
                requestNotification.setSendNotification(sendNotification);

                requestNotification.setToken(currentUser.getFCM_Token());

                retrofit2.Call<ResponseBody> responseBodyCall = api.sendNotification(requestNotification);

                responseBodyCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                    }
                });

                intent = getIntent();
                bundle = intent.getExtras();
                currentUser = (Users) bundle.getSerializable("currentUser");

                intent = new Intent(UserProfile.this, VideoChatViewActivity.class);

                bundle.putSerializable("user", currentUser);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        checkID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog builder = new Dialog(UserProfile.this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });

                ImageView imageView = new ImageView(UserProfile.this);
                if(!currentUser.getIdPic().equals("")) {
                    Picasso.get().load(currentUser.getIdPic()).into(imageView);
                    builder.addContentView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    builder.show();
                } else {
                    Toast.makeText(UserProfile.this, "The user hasn't uploaded his Id yet!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}