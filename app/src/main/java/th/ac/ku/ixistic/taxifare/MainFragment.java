package th.ac.ku.ixistic.taxifare;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ixistic on 5/21/16 AD.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    public Boolean isStart = false;

    private GoogleMap map;
    private UiSettings mUiSettings;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private Chronometer clock;
    private Button startButton;
    private double distance;
    private TextView distanceText;
    private double latOld;
    private double lonOld;
    private double latStart;
    private double lonStart;
    private TextView priceText;
    private double price;

    private Bundle bundle;
    private Firebase ref;
    private List locationLat;
    private List locationLon;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_section_main, null, false);

        // map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
        params.height = (int) pixels;
        mapFragment.getView().setLayoutParams(params);

        // button
        startButton = (Button) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(this);

        // clock
        clock = (Chronometer) view.findViewById(R.id.traffic_value_label);
        clock.setText("00:00:00");
        clock.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);
                updatePrice();
            }
        });

        distanceText = (TextView) view.findViewById(R.id.distance_value_label);
        distance = 0;
        latOld = 0;
        lonOld = 0;

        priceText = (TextView) view.findViewById(R.id.price_value_label);
        price = 0;

        ref = new Firebase("https://taxifare.firebaseio.com");

        locationLat = new ArrayList();
        locationLon = new ArrayList();

        return view;
    }

    public void updateData(Bundle bundle){
        this.bundle = bundle;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        mUiSettings = this.map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
//        this.map.addMarker(new MarkerOptions()
//                .position(new LatLng(13.7522, 100.4939))
//                .title("Marker"));
//        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.7522, 100.4939), 15));
        this.map.setOnMyLocationButtonClickListener(this);
        this.map.setOnMyLocationChangeListener(myLocationChangeListener);
        enableMyLocation();

    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//            map.addMarker(new MarkerOptions().position(loc));
            if (map != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            }
            if(isStart) {
                clock.stop();
                if(latOld == 0 && lonOld == 0) {
                    latOld = location.getLatitude();
                    latStart = latOld;
                    lonOld = location.getLongitude();
                    lonStart = lonOld;
                }else{
                    distance += haversine(latOld, lonOld, location.getLatitude(), location.getLongitude());
                    latOld = location.getLatitude();
                    lonOld = location.getLongitude();
                }
                locationLat.add(latOld);
                locationLon.add(lonOld);

                distanceText.setText(String.format("%.2f km", distance / 1000.0));
                updatePrice();
                clock.start();
            }
        }
    };

    private void updatePrice() {
        if(distance > 1000 && distance <= 10000){
            price = 35 + ((int)(distance/1000) * 5.50);
        }
        else if(distance > 10000 && distance <= 20000){
            price = 35 + ((10000/1000) * 5.50) + ((int)((distance-10000)/1000) * 6.50);
        }
        else if(distance > 20000 && distance <= 40000){
            price = 35 + ((10000/1000) * 5.50) + (((20000-10000)/1000) * 6.50) + ((int)((distance-20000)/1000) * 7.50);
        }
        else if(distance > 40000 && distance <= 60000){
            price = 35 + ((10000/1000) * 5.50) + (((20000-10000)/1000) * 6.50) + (((40000-20000)/1000) * 7.50) + ((int)((distance-40000)/1000) * 8);
        }
        else if(distance > 60000 && distance <= 80000){
            price = 35 + ((10000/1000) * 5.50) + (((20000-10000)/1000) * 6.50) + (((40000-20000)/1000) * 7.50) + (((60000-40000)/1000) * 8) + ((int)((distance-60000)/1000) * 9);
        }
        else if(distance > 80000){
            price = 35 + ((10000/1000) * 5.50) + (((20000-10000)/1000) * 6.50) + (((40000-20000)/1000) * 7.50) + (((60000-40000)/1000) * 8) + (((80000-60000)/1000) * 9) + ((int)((distance-80000)/1000) * 10.50);
        }
        long time = SystemClock.elapsedRealtime() - clock.getBase();
        int m = (int) (time / 3600000 * 60);
        price += 2 * m;
        priceText.setText(String.format( "%d Baht", Math.round(price) ));
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000 * c;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((MainActivity)getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    protected void onResumeFragments() {
        this.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_button:
                startMeter();
                break;
        }
    }

    public void startMeter(){

        Log.w("myApp", "start");
        if(!isStart){
            distance = 0;
            latOld = 0;
            lonOld = 0;
            latStart = 0;
            lonStart = 0;
            isStart = true;
            startButton.setText("Stop");
            price = 35;
            locationLat = new ArrayList();
            locationLon = new ArrayList();
            clock.setBase(SystemClock.elapsedRealtime());
        }else{
            isStart = false;
            startButton.setText("Start");
            if(bundle != null){
                Firebase usersRef = ref.child("users");
                Firebase user = usersRef.child(bundle.getString("idFacebook"));
                Firebase history = user.child("history");
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("hh:mm a, EEEE, MMMM F, yyyy");
                String formattedDate = df.format(c.getTime());
                long time = SystemClock.elapsedRealtime() - clock.getBase();
                String track = "";
                for (int i = 0; i < locationLat.size()-1; i++) {
                    track += (double)locationLat.get(i)+",";
                    track += (double)locationLon.get(i)+",";
                }
                track += (double)locationLat.get(locationLat.size()-1)+",";
                track += (double)locationLon.get(locationLon.size()-1);
                History hist = new History(formattedDate,latStart,lonStart,latOld,lonOld,price,time,distance,track);
                history.push().setValue(hist);
            }
            clock.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getActivity().getSharedPreferences("Main", Context.MODE_PRIVATE);
        if(sp.getBoolean("status", false)) {
            loadPref();
        }
    }

    public void loadPref(){
        SharedPreferences sp = getActivity().getSharedPreferences("Main", Context.MODE_PRIVATE);
        bundle = new Bundle();
        bundle.putString("idFacebook", sp.getString("id", ""));
        bundle.putString("first_name", sp.getString("first_name", ""));
        bundle.putString("last_name", sp.getString("last_name", ""));
        bundle.putString("email", sp.getString("email", ""));
        bundle.putString("gender", sp.getString("gender", ""));
        bundle.putString("birthday", sp.getString("birthday", ""));
        bundle.putString("location", sp.getString("location", ""));
        bundle.putString("profile_pic", sp.getString("profile_pic", ""));
        updateData(bundle);
    }
}
