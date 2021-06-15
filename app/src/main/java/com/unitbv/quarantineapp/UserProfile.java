package com.unitbv.quarantineapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

        userEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder editUserBuilder = new AlertDialog.Builder(UserProfile.this);

                View addUserView = getLayoutInflater().inflate(R.layout.edit_user_dialog, null);

                editUserBuilder.setTitle("Edit current user");

                final Calendar calendar = Calendar.getInstance();

                final EditText fullname = addUserView.findViewById(R.id.editTextFullNameEdit);
                final EditText email = addUserView.findViewById(R.id.editTextEmailEdit);
                final EditText birthday = addUserView.findViewById(R.id.editTextBirthdayEdit);
                final EditText phoneNumber = addUserView.findViewById(R.id.editTextTelefonEdit);
                final EditText country = addUserView.findViewById(R.id.editTextCountryEdit);
                final EditText city = addUserView.findViewById(R.id.editTextCityEdit);
                final EditText street = addUserView.findViewById(R.id.editTextStreetEdit);
                final EditText serieBuletin = addUserView.findViewById(R.id.editTextSerieBuletinEdit);
                final EditText cnp = addUserView.findViewById(R.id.editTextCNPEdit);

                // open date picker dialog for birthday select
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }

                    private void updateLabel() {
                        String format = "dd/MM/yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

                        birthday.setText(sdf.format(calendar.getTime()));
                    }
                };

                birthday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(UserProfile.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });


                // Update Button functionality
                editUserBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // storing new user's data in newUser map
                        final Map<String, Object> newUser = new HashMap<>();

                        if(!fullname.getText().toString().equals(""))
                            newUser.put("fullname", fullname.getText().toString());
                        if(!email.getText().toString().equals(""))
                            newUser.put("email", email.getText().toString());
                        if(!birthday.getText().toString().equals(""))
                            newUser.put("birthday", birthday.getText().toString());
                        if(!phoneNumber.getText().toString().equals(""))
                            newUser.put("telefon", phoneNumber.getText().toString());
                        if(!country.getText().toString().equals(""))
                            newUser.put("country", country.getText().toString());
                        if(!city.getText().toString().equals(""))
                            newUser.put("city", city.getText().toString());
                        if(!street.getText().toString().equals(""))
                            newUser.put("Street", street.getText().toString());
                        if(!serieBuletin.getText().toString().equals(""))
                            newUser.put("serieBuletin", serieBuletin.getText().toString());
                        if(!cnp.getText().toString().equals(""))
                            newUser.put("CNP", cnp.getText().toString());

                        db.collection("users").document(currentUser.getUid()).set(newUser, SetOptions.merge());

                        recreate();
                        dialog.dismiss();
                    }
                });

                // Cancel button functionality
                editUserBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // creating the dialog box
                editUserBuilder.setView(addUserView);
                AlertDialog dialog = editUserBuilder.create();
                dialog.show();
            }
        });

    }
}