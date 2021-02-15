package com.unitbv.quarantineapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // declare objects
    private EditText codeET = null;
    private Button registerButton = null;
    private boolean auth = false;
    private String currentUser = null;
    private Intent intent = null;
    private Bundle bundle = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        // wire objects with widgets
        codeET = findViewById(R.id.codeEditText);
        registerButton = findViewById(R.id.registerBtn);

        // deal with button events
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    for(QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                        if(document.getId().equals(codeET.getText().toString())) {
                                            if ((boolean) document.get("admin")) {
                                                auth = true;
                                                currentUser = document.getString("fullname");
                                                bundle = new Bundle();
                                                bundle.putSerializable("currUser", currentUser);
                                                intent = new Intent(MainActivity.this, AdminPanel.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            } else {
                                                auth = true;
                                                currentUser = document.getString("fullname");
                                                bundle = new Bundle();
                                                bundle.putSerializable("currUser", currentUser);
                                                intent = new Intent(MainActivity.this, IntroActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }

                                        }
                                    }

                                    if (!auth) {
                                        Toast.makeText(MainActivity.this, "Code does not exist!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                }
        });


    }
}