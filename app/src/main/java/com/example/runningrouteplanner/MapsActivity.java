package com.example.runningrouteplanner;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.example.runningrouteplanner.DirectionFinder;
import com.example.runningrouteplanner.DirectionFinderListener;
import com.example.runningrouteplanner.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private int myRequestCode = 1;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private double dis;
    private boolean loop;
    private int direction;
    private LocationManager locationManager;
    private Location location;
    private TextView type;
    private int check;
    private int planned = 0;
    private String origin="42.350,-71.108";
    private String destination="42.450,-71.208";
    private Date startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i = getIntent();
        Bundle a =i.getExtras();
        check = a.getInt("source");
        if (check==1) {
            dis = a.getDouble("distance", 2);
            loop = a.getBoolean("loop", true);
            direction = a.getInt("direction", 3);
            String d = direction + "";
            Log.d("direction", d);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = getCurrentLocation();
            type = findViewById(R.id.type);
            String t = "Loop Type: ";
            if (loop) t = t + "return ";
            else t = t + "single";
            type.setText(t);
        }else{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = getCurrentLocation();
            origin = a.getString("start");
            destination = a.getString("end");
            sendRequest();
        }
    }

    public void onStartClick(View view) {
        planned = 1;
        sendRequest();
    }

    @SuppressLint("MissingPermission")
    public void onStoreClick(View view) {
        if(planned == 0) return;
        startTime = Calendar.getInstance().getTime();
        storeLog(startTime, dis, origin, destination);
        Context context = getApplicationContext();
        CharSequence text = "Planning data stored!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    // store the log into database using MyProvider
    public void storeLog(Date date, double myDistance, String origin, String destination){
        // format time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] s = origin.split(",");
        String[] e = destination.split(",");
        String str2 = String.format("%.4f,%.4f", Double.parseDouble(s[0]), Double.parseDouble(s[1]));
        String str3 = String.format("%.4f,%.4f", Double.parseDouble(e[0]), Double.parseDouble(e[1]));
        ContentValues newValues = new ContentValues();
        newValues.put(MyProviderContract.DATE, dateFormat.format(date));
        newValues.put(MyProviderContract.STARTPOINT, str2);
        newValues.put(MyProviderContract.ENDPOINT, str3);
        newValues.put(MyProviderContract.DISTANCE, myDistance);

        // insert value to running tracker table
        getContentResolver().insert(MyProviderContract.TRACKER_URI, newValues);
    }

    public void onHistoryClick(View view){
        Intent intent = new Intent(MapsActivity.this, History.class);
        Bundle a = new Bundle();
        a.putString("origin",origin);
        a.putString("destination",destination);
        intent.putExtras(a);
        startActivity(intent);
    }

    public static String locationStringFromLocation(final Location location) {
        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + "," + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation () {
        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        return location;
    }

    private void sendRequest() {
        if (check==1) {
            origin = locationStringFromLocation(location);
            String originX = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);
            String originY = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
//        double ox = Double.parseDouble(originX);
//        double oy = Double.parseDouble(originY);
            double x_go;
            double y_go;
            double factor;
            switch (direction) {
                case 1:
                    x_go = 1.0;
                    y_go = -1.0;
                    break;
                case 2:
                    x_go = 1.0;
                    y_go = 1.0;
                    break;
                case 3:
                    x_go = -1.0;
                    y_go = -1.0;
                    break;
                case 4:
                    x_go = -1.0;
                    y_go = 1.0;
                    break;
                default:
                    x_go = 0;
                    y_go = 0;
                    break;
            }
            if (loop) factor = 0.5;
            else factor = 1.0;
            Log.d("x_go", x_go + "");
            Log.d("y_go", y_go + "");
            double x = Double.parseDouble(originX) + x_go * 2 * dis / (100 * 6) * factor;
            double y = Double.parseDouble(originY) + y_go * 2.5 * dis / (100 * 6) * factor;
            destination = x + "," + y;
        }
        if (destination.isEmpty()){
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        // check and ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, myRequestCode);
            return;
        }
        // enable my current location
        enableMyLocation();
    }

    // enable location
    private void enableMyLocation(){
        // if permission denied, show the toast alert
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Context context = getApplicationContext();
            CharSequence text = "Location permission denied!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            // set up zoom setting
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Add a marker in Current Location and move the camera
            // LatLng Boston = new LatLng(42.350, -71.106);
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Boston"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        }
    }
    // handle the permission request with specific request code, and close the activity if permission denied
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == myRequestCode){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == grantResults[0]) {
                enableMyLocation();
            }else{
                Context context = getApplicationContext();
                CharSequence text = "Location permission denied!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                onDestroy();
            }
        }
    }
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }
    }
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 15));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.googleg_disabled_color_18))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarker.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.googleg_standard_color_18))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(Color.RED)
                    .width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }

            polyLinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
