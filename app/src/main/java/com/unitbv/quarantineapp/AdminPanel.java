package com.unitbv.quarantineapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.unitbv.quarantineapp.rtcgenerator.RtcTokenBuilderSample;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class AdminPanel extends AppCompatActivity {

    // declare objects
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFunctions func = FirebaseFunctions.getInstance();

    private Intent intent = null;
    private Bundle bundle = null;

    private String currentUser = null;

    private ImageButton addUserBtn = null;
    private ImageButton logoutBtn = null;
    private SwipeMenuListView usersList = null;
    private Button allBtn = null;
    private Button pendingBtn = null;
    private Button verifiedBtn = null;

    private UserAdapter adapter = null;

    private ArrayList<Users> data = new ArrayList<Users>();

    private String selectedFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_admin_panel);

        if(auth.getCurrentUser() == null) {
            startActivity(new Intent(AdminPanel.this, MainActivity.class));
            finish();
        }

        final SwipeRefreshLayout pullRefreshLayout = findViewById(R.id.pullToRefresh);
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();
                pullRefreshLayout.setRefreshing(false);
            }
        });

        // wire objects with widgets
        addUserBtn = findViewById(R.id.addUser);
        logoutBtn = findViewById(R.id.logout);
        usersList = findViewById(R.id.usersList);

        allBtn = findViewById(R.id.allFilter);
        verifiedBtn = findViewById(R.id.verifiedFilter);
        pendingBtn = findViewById(R.id.pendingFilter);

        currentUser = auth.getCurrentUser().getEmail();



        // showing all users from the firestore database
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                String uid = document.getId();
                                String fullname = document.getString("fullname");
                                String email = document.getString("email");
                                String birthday = document.getString("birthday");
                                String country = document.getString("country");
                                String city = document.getString("city");
                                String street = document.getString("Street");
                                String cnp = document.getString("CNP");
                                String phoneNumber = document.getString("telefon");
                                String serieBuletin = document.getString("serieBuletin");
                                String idPic = document.getString("IDPic");
                                String profilePic = document.getString("profilePic");
                                boolean admin = (boolean) document.get("admin");
                                String status = document.getString("status");
                                String FCM_Token = document.getString("FCM Token");
                                String latitude = document.getString("latitude");
                                String longitude = document.getString("longitude");
                                String lastCheck = document.getString("lastChecked");

                                if(!admin) {
                                    Users user = new Users(uid, fullname, admin, email, birthday, phoneNumber, country, city, street, cnp, serieBuletin, idPic, profilePic, status, FCM_Token, latitude, longitude, lastCheck);
                                    data.add(user);
                                }
                            }

                            // setting custom adapter for listView
                            adapter = new UserAdapter(
                                    AdminPanel.this,
                                    R.layout.list_item,
                                    data
                            );

                            usersList.setAdapter(adapter);
                            setFilters();

                        } else {
                            // error getting documents
                        }
                    }
                });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(300);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        usersList.setMenuCreator(creator);

        usersList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {

                    case 0:
                        db.collection("users").document(data.get(position).getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AdminPanel.this, "User Deleted!", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        // Open user info activity
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // make intent and bundle
                Intent intent = new Intent(AdminPanel.this, UserProfile.class);
                Bundle bundle = new Bundle();

                bundle.putSerializable("currentUser", data.get(position));
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        // logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(AdminPanel.this, MainActivity.class));
                finish();
            }
        });

        // adding new users
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password = generatePassword();

                auth = FirebaseAuth.getInstance();

                AlertDialog.Builder addUserBuilder = new AlertDialog.Builder(AdminPanel.this);

                View addUserView = getLayoutInflater().inflate(R.layout.add_user_dialog, null);

                addUserBuilder.setTitle("Add new user");

                final Calendar calendar = Calendar.getInstance();

                final EditText fullname = addUserView.findViewById(R.id.editTextFullName);
                final EditText email = addUserView.findViewById(R.id.editTextEmail);
                final EditText birthday = addUserView.findViewById(R.id.editTextBirthday);
                final EditText phoneNumber = addUserView.findViewById(R.id.editTextTelefon);
                final EditText country = addUserView.findViewById(R.id.editTextCountry);
                final EditText city = addUserView.findViewById(R.id.editTextCity);
                final EditText street = addUserView.findViewById(R.id.editTextStreet);
                final EditText serieBuletin = addUserView.findViewById(R.id.editTextSerieBuletin);
                final EditText cnp = addUserView.findViewById(R.id.editTextCNP);
                final EditText uid = addUserView.findViewById(R.id.editTextUID);

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
                        new DatePickerDialog(AdminPanel.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                uid.setText(password);

                // Add New User Button functionality
                addUserBuilder.setPositiveButton("Add New User", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // storing new user's data in newUser map
                        final Map<String, Object> newUser = new HashMap<>();

                        newUser.put("fullname", fullname.getText().toString());
                        newUser.put("email", email.getText().toString());
                        newUser.put("birthday", birthday.getText().toString());
                        newUser.put("telefon", phoneNumber.getText().toString());
                        newUser.put("country", country.getText().toString());
                        newUser.put("city", city.getText().toString());
                        newUser.put("Street", street.getText().toString());
                        newUser.put("serieBuletin", serieBuletin.getText().toString());
                        newUser.put("CNP", cnp.getText().toString());
                        newUser.put("IDPic", "");
                        newUser.put("profilePic", "");
                        newUser.put("latitude", "");
                        newUser.put("longitude", "");
                        newUser.put("quarantineStarts", Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                        newUser.put("quarantineEnds", Date.from(LocalDateTime.now().plusDays(14).atZone(ZoneId.systemDefault()).toInstant()));
                        newUser.put("admin", false);
                        newUser.put("status", "Pending...");
                        newUser.put("checks", null);

                        auth.createUserWithEmailAndPassword(email.getText().toString(), password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                FirebaseUser user = auth.getCurrentUser();
                                Toast.makeText(AdminPanel.this, "Account created!", Toast.LENGTH_LONG).show();
                                db.collection("users").document(user.getUid()).set(newUser);

                                sendPasswordEmail(user.getEmail(), password);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AdminPanel.this, "Failed to create account!", Toast.LENGTH_LONG).show();
                            }
                        });

                        recreate();
                        dialog.dismiss();
                    }
                });

                // Cancel button functionality
                addUserBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // creating the dialog box
                addUserBuilder.setView(addUserView);
                AlertDialog dialog = addUserBuilder.create();
                dialog.show();
            }
        });
    }


    private void filterList(String status) {
        selectedFilter = status;
        ArrayList<Users> filteredUsers = new ArrayList<>();
        for(Users user : data) {
            if(user.getStatus().equals(status)){
                filteredUsers.add(user);
            }
        }

        UserAdapter adapter = new UserAdapter(AdminPanel.this, R.layout.list_item, filteredUsers);
        usersList.setAdapter(adapter);
    }

    private void setFilters() {
        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAdapter adapter = new UserAdapter(AdminPanel.this, R.layout.list_item, data);
                usersList.setAdapter(adapter);
            }
        });

        pendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterList("Pending...");
            }
        });

        verifiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterList("Verified");
            }
        });
    }

    private void sendPasswordEmail(String sendTo, String password) {
        Map<String, Object> docData = new HashMap<>();
        Map<String, Object> msgData = new HashMap<>();

        msgData.put("subject", "Home Quarantine Password");
        msgData.put("html", "Welcome to Home Quarantine App.<br>This is your password:<b> " + password + "</b><br>To enhance security of your account please reset your password!");
        docData.put("to", Arrays.asList(sendTo));
        docData.put("message", msgData);

        db.collection("mail").add(docData);
    }

    // generates random password
    private String generatePassword() {
        final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random RANDOM = new Random();

        StringBuilder sb = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }


}