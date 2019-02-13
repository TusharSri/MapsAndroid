package com.example.mapapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PolyDecodeDemoActivity extends BaseDemoActivity implements View.OnClickListener {

    private EditText startDest;
    private EditText endDest;
    private Button search;
    private ProgressDialog dialog;

    @Override
    protected void initViews() {
        startDest = findViewById(R.id.start_dest_edittext);
        endDest = findViewById(R.id.end_dest_edittext);
        search = findViewById(R.id.submit);
        search.setOnClickListener(PolyDecodeDemoActivity.this);
    }

    @Override
    protected void startDemo() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(current, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            getMap().addMarker(new MarkerOptions().position(current));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                List<LatLng> path = new ArrayList();
                if (!startDest.getText().toString().trim().equals("")) {
                    Double[] latLong = new Double[2];
                    try {
                        latLong = getLatLongFromAddress(startDest.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    LatLng start = new LatLng(latLong[0], latLong[1]);
                    path.add(start);
                    getMap().addMarker(new MarkerOptions().position(start).title(startDest.getText().toString()));
                    getMap().moveCamera(CameraUpdateFactory.newLatLng(start));
                }

                if (!endDest.getText().toString().trim().equals("")) {
                    Double[] latLong = new Double[2];
                    try {
                        latLong = getLatLongFromAddress(endDest.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    LatLng end = new LatLng(latLong[0], latLong[1]);
                    path.add(end);
                    getMap().addMarker(new MarkerOptions().position(end).title(endDest.getText().toString()));
                }

                if(!TextUtils.isEmpty(startDest.getText().toString()) && !TextUtils.isEmpty(endDest.getText().toString())){
                    new GetDirection(startDest.getText().toString(),endDest.getText().toString()).execute();
                }
                break;
        }
    }

    private Double[] getLatLongFromAddress(String add) throws IOException {
        Geocoder gc = new Geocoder(this);
        Double[] latLong = new Double[2];
        if (gc.isPresent()) {
            List<Address> list = gc.getFromLocationName(add, 1);
            Address address = list.get(0);
            latLong[0] = address.getLatitude();
            latLong[1] = address.getLongitude();
        }
        return latLong;
    }


    class GetDirection extends AsyncTask<String, String, String> {

        private final String origin;
        private final String dest;
        private List<LatLng> pontos;

        GetDirection(String origin, String dest) {
            this.origin = origin;
            this.dest = dest;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(PolyDecodeDemoActivity.this);
            dialog.setMessage("Drawing the route, please wait!");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        protected String doInBackground(String... args) {
            String stringUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin+ "&destination=" + dest+ "&sensor=false&key=AIzaSyCT_UViYC4cKOUp5vsWOFn34O0jqWGvjE8";
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url
                        .openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(httpconn.getInputStream()),
                            8192);
                    String strLine = null;

                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }

                String jsonOutput = response.toString();

                JSONObject jsonObject = new JSONObject(jsonOutput);

                // routesArray contains ALL routes
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // Grab the first route
                JSONObject route = routesArray.getJSONObject(0);

                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                pontos = decodePoly(polyline);

            } catch (Exception e) {
                dialog.dismiss();
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            searchedRoute();
        }

        private void searchedRoute() {
            if (pontos != null) {
                for (int i = 0; i < pontos.size() - 1; i++) {
                    LatLng src = pontos.get(i);
                    LatLng dest = pontos.get(i + 1);
                    try {
                        //here is where it will draw the polyline in your map
                        Polyline line = getMap().addPolyline(new PolylineOptions()
                                .add(new LatLng(src.latitude, src.longitude),
                                        new LatLng(dest.latitude, dest.longitude))
                                .width(10).color(Color.BLUE).geodesic(true));

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(src);
                        builder.include(dest);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                        getMap().animateCamera(cu);
                    } catch (NullPointerException e) {
                        Log.e("Error", "NullPointerException onPostExecute: " + e.toString());
                    } catch (Exception e2) {
                        Log.e("Error", "Exception onPostExecute: " + e2.toString());
                    }

                }
                dialog.dismiss();

            }
        }
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}