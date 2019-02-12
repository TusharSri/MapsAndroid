package com.example.mapapplication;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolyDecodeDemoActivity extends BaseDemoActivity implements View.OnClickListener {

    private EditText startDest;
    private EditText endDest;
    private Button search;
    private final static String LINE = "rvumEis{y[}DUaBGu@EqESyCMyAGGZGdEEhBAb@DZBXCPGP]Xg@LSBy@E{@SiBi@wAYa@AQGcAY]I]KeBm@_Bw@cBu@ICKB}KiGsEkCeEmBqJcFkFuCsFuCgB_AkAi@cA[qAWuAKeB?uALgB\\eDx@oBb@eAVeAd@cEdAaCp@s@PO@MBuEpA{@R{@NaAHwADuBAqAGE?qCS[@gAO{Fg@qIcAsCg@u@SeBk@aA_@uCsAkBcAsAy@AMGIw@e@_Bq@eA[eCi@QOAK@O@YF}CA_@Ga@c@cAg@eACW@YVgDD]Nq@j@}AR{@rBcHvBwHvAuFJk@B_@AgAGk@UkAkBcH{@qCuAiEa@gAa@w@c@o@mA{Ae@s@[m@_AaCy@uB_@kAq@_Be@}@c@m@{AwAkDuDyC_De@w@{@kB_A}BQo@UsBGy@AaA@cLBkCHsBNoD@c@E]q@eAiBcDwDoGYY_@QWEwE_@i@E}@@{BNaA@s@EyB_@c@?a@F}B\\iCv@uDjAa@Ds@Bs@EyAWo@Sm@a@YSu@c@g@Mi@GqBUi@MUMMMq@}@SWWM]C[DUJONg@hAW\\QHo@BYIOKcG{FqCsBgByAaAa@gA]c@I{@Gi@@cALcEv@_G|@gAJwAAUGUAk@C{Ga@gACu@A[Em@Sg@Y_AmA[u@Oo@qAmGeAeEs@sCgAqDg@{@[_@m@e@y@a@YIKCuAYuAQyAUuAWUaA_@wBiBgJaAoFyCwNy@cFIm@Bg@?a@t@yIVuDx@qKfA}N^aE@yE@qAIeDYaFBW\\eBFkANkANWd@gALc@PwAZiBb@qCFgCDcCGkCKoC`@gExBaVViDH}@kAOwAWe@Cg@BUDBU`@sERcCJ{BzFeB";

    @Override
    protected void initViews() {
        startDest = findViewById(R.id.start_dest_edittext);
        endDest = findViewById(R.id.end_dest_edittext);
        search = findViewById(R.id.submit);
        search.setOnClickListener(PolyDecodeDemoActivity.this);
    }

    @Override
    protected void startDemo() {
        List<LatLng> decodedPath = PolyUtil.decode(LINE);

        getMap().addPolyline(new PolylineOptions().addAll(decodedPath));

        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 12));
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

                    String url = getMap().
                    //PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
                    //getMap().addPolyline(opts);
                    //List<LatLng> decodedPath = PolyUtil.encode(PolyUtil.decode(path));
                    //getMap().addPolyline(new PolylineOptions().addAll(decodedPath));
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
}