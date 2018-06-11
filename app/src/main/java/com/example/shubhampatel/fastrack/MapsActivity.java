package com.example.shubhampatel.fastrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    FirebaseDatabase mFireBaseDatabase;
    Query databaseproduct;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    Button list;
    ArrayList<LatLng> MarkerPoints = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference refdb;
    ArrayList<String> longitude = new ArrayList<>();
    ArrayList<String> latitude = new ArrayList<>();
    Double lo, la;
    String[] itemarray;
    Map<Double, Integer> manhattan = new TreeMap<>();
    private ArrayList<String> dmlist;
    int i, ii;
    public String customerlist,listname;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        list = findViewById(R.id.mylist);
        dmlist = getIntent().getExtras().getStringArrayList("dmlist");
        listname = getIntent().getExtras().getString("listName");
        customerlist = getIntent().getExtras().getString("listofcustomer");


        database = FirebaseDatabase.getInstance();
        refdb = database.getReference();

        Log.e("customerlist", customerlist);
        itemarray = customerlist.split(":");


        mFireBaseDatabase = FirebaseDatabase.getInstance();
        databaseproduct = FirebaseDatabase.getInstance().getReference("product");
        databaseproduct.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (i = 0; i < itemarray.length; i++) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("pname").getValue(String.class).equals(itemarray[i])) {
                            String productLati = snapshot.child("lati").getValue(String.class);
                            String productLong = snapshot.child("longi").getValue(String.class);
                            if (productLong != null && productLati != null) {
                                longitude.add(productLong);
                                latitude.add(productLati);
                                Log.e("longiiiiiii", latitude.get(i) + "" + longitude.get(i) + " " + itemarray[i]);
                            }
                        }
                    }
                }
                //getUrl();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent(MapsActivity.this, MyShopList.class);
                i.putExtra("dmlist", dmlist);
                i.putExtra("listName", listname);
                startActivity(i);

            }
        });
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

            }
        });
    }

    private void loadmappath()
    {
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }


    private void placemarker() {
        int i = 0;
        for (int j : manhattan.values()) {
            la = Double.parseDouble(latitude.get(j));
            lo = Double.parseDouble(longitude.get(j));
            LatLng latLng = new LatLng(la, lo);

//            LatLng latLng = new LatLng(lo, la);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            if (i == 0) {
                markerOptions.title("START : " + itemarray[j].toUpperCase());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (i == itemarray.length - 1) {
                markerOptions.title("END : " + itemarray[j].toUpperCase());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                markerOptions.title(i + " " +itemarray[j]);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            i++;
        }
    }

    private String getUrl() {
        int i = 0;
        int start = -1;
        int end = -1;
        String waypoints = "waypoints=";
        for (Double md : manhattan.keySet()) {
            Integer index = manhattan.get(md);
            if (i == 0) {
                start = index;
            } else if (i == manhattan.size() - 1) {
                end = index;
            } else {
                waypoints += latitude.get(index) + "," + longitude.get(index) + "|";
            }
            i++;
        }
        String combinefirst = "origin=" + latitude.get(start) + "," + longitude.get(start);
        String combinelast = "destination=" + latitude.get(end) + "," + longitude.get(end);
//        String combinefirst = "origin=" + longitude.get(start) + "," + latitude.get(start);
//        String combinelast = "destination=" + longitude.get(end) + "," + latitude.get(end);
        String sensor = "sensor=false";
        String parameters = combinefirst + "&" + combinelast + "&" + waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.e("url", url.toString());
        return url;
    }

//        String combinefirst = "origin=" + latitude.get(start) + "," + longitude.get(start);
//        String combinefirst = "origin=" + longitude.get(start) + "," + latitude.get(start);
//        String combinelast = "destination=" + longitude.get(end) + "," + latitude.get(end);




    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(8);


                lineOptions.color(Color.RED);
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.toString());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(19));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
//          end
//        double lat = 37.351194;
//        double lng = -121.934822;

//        current location of yrs.
        double lat = latLng.latitude;
        double lng = latLng.longitude;


//        start
//        double lat = 37.350758;
//        double lng = -121.933852;

//        double lat = 37.350725;
//        double lng = -121.933829;

//        middle
//           double lat = 37.350940;
//           double lng = -121.934197;


        Log.e("lat", String.valueOf(lat));
        Log.e("lng", String.valueOf(lng));


        for (int k = 0; k < itemarray.length; k++) {


            double lo = Double.parseDouble(longitude.get(k));
            double la = Double.parseDouble(latitude.get(k));


//            double lo = Double.parseDouble(latitude.get(k));
//            double la = Double.parseDouble(longitude.get(k));


            double manhattanDistance = Math.abs((lat - la) + (lng - lo));
            manhattan.put(manhattanDistance, k);
            Log.e("manhatten", "our location to  :  " + itemarray[k] + " " + manhattan);
        }
        placemarker();
        loadmappath();
  //      loadmappath1(latLng);

    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void onBackPressed() {
        Intent startMain = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(startMain);
    }

}
