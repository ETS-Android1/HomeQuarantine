package com.unitbv.quarantineapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.unitbv.quarantineapp.rtcgenerator.RtcTokenBuilderSample;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class VideoChatViewActivity extends AppCompatActivity {
    private static final String TAG = VideoChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    final ArrayList<Date> dates = new ArrayList<>();

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;

    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;

    private LoggerRecyclerView mLogView;

    private Intent intent = null;
    private Bundle bundle = null;

    Users user = null;


    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         *     The remote user joins the channel and sends the video stream.
         *     The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         *         The remote user leaves channel.
         *         The remote user drops offline.
         *         The remote user calls the muteLocalVideoStream method.
         *         The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid);
                }
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        ViewGroup parent = mRemoteContainer;
        if (parent.indexOfChild(mLocalVideo.view) > -1) {
            parent = mLocalContainer;
        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        if (mRemoteVideo != null) {
            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mLocalContainer);
        parent.addView(view);
        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo);
    }

    private void onRemoteUserLeft(int uid) {
        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {
            removeFromParent(mRemoteVideo);
            // Destroys remote view
            mRemoteVideo = null;
        }
    }

    private String generateToken() {
        String currentUser = auth.getCurrentUser().getUid();
        String channelName = "test-channel";

        RtcTokenBuilderSample rtcTokenBuilderSample = new RtcTokenBuilderSample();

        Resources resources = getResources();

        return rtcTokenBuilderSample.getRtcToken(currentUser, channelName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        initUI();

        intent = getIntent();
        bundle = intent.getExtras();
        user = (Users) bundle.getSerializable("user");

        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);

        mLogView = findViewById(R.id.log_recycler_view);
    }

    private Map<String, String> getCurrentLocation() {
        final Map<String, String> gpsCoords = new HashMap<>();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(VideoChatViewActivity.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(VideoChatViewActivity.this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int latestLocationIndex = locationResult.getLocations().size() - 1;

                                gpsCoords.put("currLongitude", String.valueOf(locationResult.getLocations().get(latestLocationIndex).getLongitude()));
                                gpsCoords.put("currLatitude", String.valueOf(locationResult.getLocations().get(latestLocationIndex).getLatitude()));

                            }
                        }
                    }, Looper.getMainLooper());
        }

        return gpsCoords;
    }

    private void checkUserLocation() {
        db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, String> gps;
                gps = getCurrentLocation();

               if(documentSnapshot.getBoolean("admin")){
                   // db.collection("users").document(user.getUid()).set(gps, SetOptions.merge());
                   Toast.makeText(VideoChatViewActivity.this, gps.get("currLongitude"), Toast.LENGTH_LONG).show();
                   String lat = user.getLatitude();
                   String longit = user.getLongitude();

                   Geocoder geocoder;
                   List<Address> addresses = null;
                   geocoder = new Geocoder(VideoChatViewActivity.this, Locale.getDefault());
                   try {
                       addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(longit), 1);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }

                   String address = addresses.get(0).getAddressLine(0);
                   String city = addresses.get(0).getLocality();
                   String country = addresses.get(0).getCountryName();

                   mLogView.logI("Quarantine Address:");
                   mLogView.logI(address);
               }
            }
        });
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        mLocalContainer.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
        mLocalVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(mLocalVideo);
    }

    private void joinChannel() {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
        // RtcTokenBuilderSample rtc = new RtcTokenBuilderSample();
        // String token = rtc.getRtcToken("1234567", "test-channel");

        String token = getString(R.string.agora_access_token);

        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token,  "test-channel", "Extra Optional Data", 0);
        checkUserLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        evaluateSession();
        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
    }

    private void evaluateSession() {
        final Map<String, Object> data = new HashMap<>();
        final Map<String, Object> lc = new HashMap<>();
        final Map<String, Object> verified = new HashMap<>();
        db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getBoolean("admin")){

                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoChatViewActivity.this);
                    builder.setMessage("Did the person pass the quarantine check?");
                    builder.setTitle("Evaluate Session");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String formatedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                            data.put("checks", FieldValue.arrayUnion(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
                            lc.put("lastChecked", formatedDate);
                            verified.put("status", "Verified");
                            db.collection("users").document(user.getUid()).update(data);
                            db.collection("users").document(user.getUid()).set(lc, SetOptions.merge());
                            db.collection("users").document(user.getUid()).set(verified, SetOptions.merge());

                            Handler handler = new Handler();

                            Runnable runnableCode = new Runnable() {
                                @Override
                                public void run() {
                                    Map<String, Object> statusData = new HashMap<>();
                                    statusData.put("status", "Pending...");
                                    db.collection("users").document(user.getUid()).set(statusData, SetOptions.merge());
                                }
                            };

                            handler.postDelayed(runnableCode, 60 * 1000);

                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: Do something with problematic users
                            dialog.cancel();
                            finish();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }
}
