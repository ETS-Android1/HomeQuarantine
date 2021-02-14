package com.unitbv.quarantineapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.nio.BufferUnderflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class AdminPanel extends AppCompatActivity {

    // declare objects
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Intent intent = null;
    private Bundle bundle = null;

    private String currentUser = null;

    private TextView welcomeTV = null;
    private ImageButton addUserBtn = null;
    private ImageButton logoutBtn = null;
    private ListView usersList = null;

    private UserAdapter adapter = null;

    private ArrayList<Users> data = new ArrayList<Users>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_admin_panel);

        // wire objects with widgets
        welcomeTV = findViewById(R.id.welcomeUser);
        addUserBtn = findViewById(R.id.addUser);
        logoutBtn = findViewById(R.id.logout);
        usersList = findViewById(R.id.usersList);

        // get intent & bundle
        intent = getIntent();
        bundle = intent.getExtras();

        // getting current user
        currentUser = (String) bundle.getSerializable("currUser");

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

                                if(!admin) {
                                    Users user = new Users(uid, fullname, admin, email, birthday, phoneNumber, country, city, street, cnp, serieBuletin, idPic, profilePic, status);
                                    data.add(user);
                                }

                                welcomeTV.setText("Welcome " + currentUser + "!");
                            }

                            // setting custom adapter for listView
                            adapter = new UserAdapter(
                                    AdminPanel.this,
                                    R.layout.list_item,
                                    data
                            );

                            usersList.setAdapter(adapter);

                        } else {
                            // error getting documents
                        }
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
                intent = new Intent(AdminPanel.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // adding new users
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uniqueId = generateUID();

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

                // show random generated uid in disabled editText
                uid.setText(uniqueId);

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

                // Add New User Button functionality
                addUserBuilder.setPositiveButton("Add New User", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // storing new user's data in newUser map
                        Map<String, Object> newUser = new HashMap<>();

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
                        newUser.put("admin", false);
                        newUser.put("status", "Pending...");

                        // creating a new document in users collection in firestore for the new user
                        db.collection("users").document(uniqueId).set(newUser);
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

    // generates random unique id
    private String generateUID() {
        final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random RANDOM = new Random();

        StringBuilder sb = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }

}