package com.unitbv.quarantineapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

public class CompleteRegister extends AppCompatActivity {

    // declaring objects
    private static final int CAMERA_PIC_REQUEST = 22;

    private ImageView cameraImageView = null;
    private ImageView selfieImageView = null;

    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_complete_register);

        // wiring objects with widgets
        cameraImageView = findViewById(R.id.cameraImageView);
        selfieImageView = findViewById(R.id.selfie);

        cameraImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // text.setText("[MESSAGE]: The button is working!");
                try {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Couldn't load photo", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case CAMERA_PIC_REQUEST:
                    if (resultCode == RESULT_OK) {
                        try {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");

                            selfieImageView.setImageBitmap(photo);

                        } catch (Exception e) {
                            Toast.makeText(this, "Couldn't load photo", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
    }
}