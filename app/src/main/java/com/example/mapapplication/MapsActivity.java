package com.example.mapapplication;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private EditText startDest;
    private EditText endDest;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startDest = findViewById(R.id.start_dest_edittext);
        endDest = findViewById(R.id.end_dest_edittext);
        search = findViewById(R.id.submit);
        search.setOnClickListener(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private Double[] getLatLongFromAddress(String add) throws IOException {
        Geocoder gc = new Geocoder(this);
        Double[] latLong = new Double[2];
        if(gc.isPresent()){
            List<Address> list = gc.getFromLocationName(add, 1);
            Address address = list.get(0);
            latLong[0] = address.getLatitude();
            latLong[1] = address.getLongitude();
        }
        return latLong;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submit:
                Double[] latLong = new Double[2];
                try {
                    latLong = getLatLongFromAddress(startDest.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(latLong != null){
                    LatLng sydney = new LatLng(latLong[0], latLong[1]);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in "+startDest.getText().toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                }
                break;
        }
    }
}
