package com.noveogroup.evgeny.awersomeproject.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.noveogroup.evgeny.awersomeproject.R;
import com.noveogroup.evgeny.awersomeproject.db.model.Task;
import com.noveogroup.evgeny.awersomeproject.util.DateTransformerUtil;
import com.noveogroup.evgeny.awersomeproject.util.LocationUtil;
import com.noveogroup.evgeny.awersomeproject.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskDetailsActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 3;
    private static final String KEY_TASK_ITEM = "TASK_ITEM";
    private static final String USER_MARKER_TAG = "You";


    @BindView(R.id.progress_bar_map)
    ProgressBar progressBarMap;
    @BindView(R.id.map_container)
    FrameLayout mapContainer;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.tags)
    TextView tags;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.rating)
    TextView rating;
    @BindView(R.id.age)
    TextView age;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    Task currentTask;

    String currentPhotoPath;

    private GoogleMap map;
    private Marker userMarker;
    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        logger = LoggerFactory.getLogger(TaskDetailsActivity.class);

        currentTask = (Task) getIntent().getSerializableExtra(KEY_TASK_ITEM);
        title.setText(currentTask.getName());
        tags.setText(StringUtil.getTagsString(currentTask.getTags()));
        author.setText(currentTask.getAuthorName());
        rating.setText(String.valueOf(currentTask.getRating()));
        age.setText(DateTransformerUtil.getAgeOfTask(currentTask.getDate(), getApplicationContext()));

        initializeMap();
        initializeUserLocationListener();
    }

    private void initializeUserLocationListener() {
        LocationUtil locationUtil = LocationUtil.getInstance(this);
        locationUtil.requestLocationUpdates(1000, 0f, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                logger.error("OnLocationChanged: {}", location);
                if (userMarker != null) {
                    updateUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                } else {
                    addUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                progressBarMap.setVisibility(View.GONE);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                logger.error("onStatusChanged: {}", s);
            }

            @Override
            public void onProviderEnabled(String s) {
                logger.error("onProviderEnabled: {}", s);
            }

            @Override
            public void onProviderDisabled(String s) {
                logger.error("onProviderDisabled: {}", s);
            }
        });
    }

    public static Intent getIntent(Context context, Task task) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_TASK_ITEM, task);
        Intent intent = new Intent(context, TaskDetailsActivity.class);
        intent.putExtras(bundle);
        return intent;
    }


    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            map = googleMap;

            LatLng taskPos = new LatLng(currentTask.getLat(), currentTask.getLng());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(taskPos, 16.0f));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            addTaskCircle(taskPos);
            // Add a marker in Sydney, Australia, and move the camera.
        });
    }

    private void addTaskCircle(LatLng taskPos) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(taskPos)
                .fillColor(ContextCompat.getColor(getApplicationContext(), R.color.marker_circle_color))
                .radius(getResources().getInteger(R.integer.marker_circle_radius))
                .strokeWidth(getResources().getInteger(R.integer.marker_circle_stroke_width));
        map.addCircle(circleOptions);
    }

    private void addUserMarker(LatLng userPos) {
        userMarker = map.addMarker(new MarkerOptions().position(userPos).title(getString(R.string.task_marker_sub)));
        userMarker.setTag(USER_MARKER_TAG);
        userMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.user_location));
    }

    private void updateUserMarker(LatLng userPos) {
        userMarker.setPosition(userPos);
    }

    @OnClick(R.id.TMP_BTN)
    void onTMPClick() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("TaskDetailsActivity", "File create err: ", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.noveogroup.evgeny.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //TODO переделай тэги
            startActivity(TaskExecutionActivity.newIntent(this, currentPhotoPath, currentTask.getName(), new ArrayList<String>(currentTask.getTags())));
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
