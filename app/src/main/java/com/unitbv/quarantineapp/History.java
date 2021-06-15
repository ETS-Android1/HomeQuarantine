package com.unitbv.quarantineapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class History extends AppCompatActivity {
    private TableLayout tableLayout;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.history_table);
        tableLayout = findViewById(R.id.tableLayout);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                List<Timestamp> checks = (List<Timestamp>) document.get("checks");
                for (int i = 0; i < checks.size(); i++) {
                    View tableRow = LayoutInflater.from(getApplicationContext()).inflate(R.layout.history_table_item, null, false);
                    TextView history_display_no = (TextView) tableRow.findViewById(R.id.history_display_no);
                    TextView history_display_date = (TextView) tableRow.findViewById(R.id.history_display_date);
                    TextView history_display_status = (TextView) tableRow.findViewById(R.id.history_display_status);

                    history_display_no.setText("" + (i + 1));

                    Timestamp t = checks.get(i);
                    Date d = t.toDate();
                    history_display_date.setText(d.toString());

                    history_display_status.setText("OK!");
                    tableLayout.addView(tableRow);
                }
            }
        });

    }

}