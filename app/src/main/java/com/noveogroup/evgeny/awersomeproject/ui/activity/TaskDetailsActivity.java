package com.noveogroup.evgeny.awersomeproject.ui.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.noveogroup.evgeny.awersomeproject.R;
import com.noveogroup.evgeny.awersomeproject.db.model.Task;
import com.noveogroup.evgeny.awersomeproject.util.DateTransformerUtil;
import com.noveogroup.evgeny.awersomeproject.util.StringUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskDetailsActivity extends AppCompatActivity {

    private static final String KEY_TASK_ITEM = "TASK_ITEM";

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

    Task currentTask;

    boolean isMapExpanded = false;

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        ButterKnife.bind(this);


        currentTask = (Task) getIntent().getSerializableExtra(KEY_TASK_ITEM);
        title.setText(currentTask.getName());
        tags.setText(StringUtil.getTagsString(currentTask.getTags()));
        author.setText(currentTask.getAuthorName());
        rating.setText(String.valueOf(currentTask.getRating()));
        age.setText(DateTransformerUtil.getAgeOfTask(currentTask.getDate(), getApplicationContext()));

        initializeMap();

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

            // Add a marker in Sydney, Australia, and move the camera.
            LatLng taskPos = new LatLng(currentTask.getLat(), currentTask.getLng());
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(taskPos)
                    .fillColor(ContextCompat.getColor(getApplicationContext(), R.color.marker_circle_color))
                    .radius(getResources().getInteger(R.integer.marker_circle_radius))
                    .strokeWidth(getResources().getInteger(R.integer.marker_circle_stroke_width));
            map.addMarker(new MarkerOptions().position(taskPos).title(getString(R.string.task_marker_sub)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(taskPos, 16.0f));
            map.addCircle(circleOptions);
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            map.setMyLocationEnabled(true);
        });
    }

//    @OnClick(R.id.fab)
//    void onMapClicked(){
//        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
//        toggle();
//    }
//
//    public void toggle() {
//        ValueAnimator animation;
//        if (isMapExpanded) {
//            animation = ValueAnimator.ofFloat(0f, 1f);
//        }else {
//            animation = ValueAnimator.ofFloat(1.5f, 1f);
//        }
//        animation.setDuration(400);
//        animation.start();
//
//        ObjectAnimator anim = ObjectAnimator.ofFloat(mapContainer, "layoutWeight",300);
//        anim.start();
//    }


}
