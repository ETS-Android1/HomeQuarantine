package com.unitbv.quarantineapp;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CheckIfSignedin extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if(user != null) {
            checkUserAccessLevel(user.getUid());
        }
    }

    private void checkUserAccessLevel(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getBoolean("admin")) {
                    Intent intent = new Intent(CheckIfSignedin.this, AdminPanel.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CheckIfSignedin.this, IntroActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                }
            }
        });
    }
}
