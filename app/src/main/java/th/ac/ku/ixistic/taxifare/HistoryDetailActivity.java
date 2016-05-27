package th.ac.ku.ixistic.taxifare;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Map;

public class HistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Firebase ref;
    private String id;
    private String key;
    private History hist;
    private GoogleMap map;
    private UiSettings mUiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("idFacebook");
            key = extras.getString("key");
        }

        ref = new Firebase("https://taxifare.firebaseio.com/users/"+id+"/history/"+key);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hist = snapshot.getValue(History.class);
                updateData(hist);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("The read failed", firebaseError.getMessage());
            }
        });

        Button button = (Button) findViewById(R.id.saveButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText comment = (EditText) findViewById(R.id.comment);
                Firebase fire = new Firebase("https://taxifare.firebaseio.com/users/"+id+"/history/"+key);
                fire.child("comment").setValue(comment.getText().toString());
                Toast.makeText(getApplicationContext(), "Update success", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        params.height = (int) pixels;
        mapFragment.getView().setLayoutParams(params);

    }

    public void updateData(History hist){
        if(hist != null) {

            TextView date = (TextView) findViewById(R.id.date);
            date.setText(hist.getDate());

            TextView price = (TextView) findViewById(R.id.price_value_label);
            price.setText(String.format("%d Baht", Math.round(hist.getPrice())));

            TextView time = (TextView) findViewById(R.id.traffic_value_label);
            int h = (int) (hist.getTrafficTime() / 3600000);
            int m = (int) (hist.getTrafficTime() - h * 3600000) / 60000;
            int s = (int) (hist.getTrafficTime() - h * 3600000 - m * 60000) / 1000;
            String hh = h < 10 ? "0" + h : h + "";
            String mm = m < 10 ? "0" + m : m + "";
            String ss = s < 10 ? "0" + s : s + "";
            time.setText(hh + ":" + mm + ":" + ss);

            TextView distance = (TextView) findViewById(R.id.distance_value_label);
            distance.setText(String.format("%.2f km", (hist.getDistance() / 1000.0)));

            EditText comment = (EditText) findViewById(R.id.comment);
            comment.setText(hist.getComment());

            if(hist.getTrackLocation() != null) {
                String[] trackLocation = (hist.getTrackLocation()).split(",");
            }

            if (hist != null && map != null) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(hist.getLatStart() * 10000.0 / 10000, hist.getLonStart() * 10000.0 / 10000))
                        .title("start"));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(hist.getLatStop() * 10000.0 / 10000, hist.getLonStop() * 10000.0 / 10000))
                        .title("stop"));
                LatLng position = new LatLng(hist.getLatStop() * 10000.0 / 10000, hist.getLonStop() * 10000.0 / 10000);

//                Polyline line = map.addPolyline(new PolylineOptions()
//                        .add(new LatLng(hist.getLatStart() * 10000.0 / 10000, hist.getLonStart() * 10000.0 / 10000), new LatLng(hist.getLatStop() * 10000.0 / 10000, hist.getLonStop() * 10000.0 / 10000))
//                        .width(5)
//                        .color(Color.RED));

                if(hist.getTrackLocation() != null) {
                    String[] trackLocation = (hist.getTrackLocation()).split(",");
                    PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                    for(int i=0;i<trackLocation.length-1;i+=2) {

                        double lat = (Double.parseDouble(trackLocation[i])) * 10000.0 / 10000;
                        double lon = (Double.parseDouble(trackLocation[i+1])) * 10000.0 / 10000;
                        LatLng point = new LatLng(lat,lon);
                        options.add(point);
                    }
                    map.addPolyline(options);
                }

                this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        mUiSettings = this.map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
