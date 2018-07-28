package com.example.tracking.solarcalculator_android.activity;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tracking.solarcalculator_android.R;
import com.example.tracking.solarcalculator_android.reciever.MyBroadcastReciever;
import com.example.tracking.solarcalculator_android.util.Location;
import com.example.tracking.solarcalculator_android.util.LocationTracker;
import com.example.tracking.solarcalculator_android.util.ServiceListener;
import com.example.tracking.solarcalculator_android.util.SunriseSunsetCalculator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private Button btnPriviouse,btnNext,btnReset;
    private TextView tvSunRise,tvSunSet,tvDate,tvCurrentLocation;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private LocationTracker locationTrack;
    private double latitude;
    private double longitude;
    private GoogleMap mGoogleMap;
    private SupportMapFragment supportMapFragment;
    private PlaceAutocompleteFragment autocompleteFragment;
    private String selectedtimezoneidentifier;
    private Date reqDate;
    private String officialSunrise;
    private String officialSunset;
    private String astronomicalSunrise;
    private String astronomicalSunset;
    private String civilSunrise;
    private String civilSunset;
    private String nauticalSunrise;
    private String nauticalSunset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_main);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        getLatLong();
        renderForm();
    }

    private void renderForm(){
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        supportMapFragment.getMapAsync(this);
        tvSunRise = (TextView)findViewById(R.id.tvSunRise);
        tvSunSet = (TextView)findViewById(R.id.tvSunSet);
        tvDate = (TextView)findViewById(R.id.tvDate);
        tvCurrentLocation = (TextView)findViewById(R.id.tvCurrentLocation);
        btnPriviouse = (Button) findViewById(R.id.btnPreviouse);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnReset = (Button)findViewById(R.id.btnReset);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             incrementDateByOne();
            }
        });

        btnPriviouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementDateByOne();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetCurrentDate();
            }
        });

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // example : way to access view from PlaceAutoCompleteFragment
                        // ((EditText) autocompleteFragment.getView()
                        // .findViewById(R.id.place_autocomplete_search_input)).setText("");
                        autocompleteFragment.setText("");
                        //view.setVisibility(View.GONE);
                        mGoogleMap.clear();
                        getCurrentLocation();
                    }
                });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mGoogleMap.clear();
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                drawMap(latitude, longitude);
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(MainActivity.this,"Clear text", Toast.LENGTH_LONG).show();
            }
        });

        tvCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                getCurrentLocation();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if(latitude >0 && longitude >0){
            drawMap(latitude,longitude);
        }
    }


    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    private void getLatLong(){
        locationTrack = new LocationTracker(MainActivity.this);
        locationTrack.setRequestProcessedListener(new ServiceListener<LatLng>() {
            @Override
            public void result(LatLng result) {

                if (result!=null) {

                    longitude = locationTrack.getLongitude();
                    latitude = locationTrack.getLatitude();
                    mGoogleMap.clear();
                    drawMap(latitude,longitude);

                    //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                } else {

                    locationTrack.showSettingsAlert();
                }
            }
        });

        if (locationTrack.canGetLocation()) {


            longitude = locationTrack.getLongitude();
            latitude = locationTrack.getLatitude();
            // Start Service.
            startService(this);
        } else {

            locationTrack.showSettingsAlert();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void drawMap(double latitude, double longitude){
        LatLng latLng = new LatLng(latitude, longitude);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Marker"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mGoogleMap.setMyLocationEnabled(true);
        } else {

            mGoogleMap.setMyLocationEnabled(true);
        }

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        reqDate = cal.getTime();
        tvDate.setText(formatDate(reqDate));
        selectedtimezoneidentifier = TimeZone.getDefault().getID();
        updateDataAndCalculateSunriseSunset(latitude, longitude,cal);
    }
    public void updateDataAndCalculateSunriseSunset(double latitude, double longitude,Calendar cal) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");

        Location location = new Location(latitude, longitude);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
                location, selectedtimezoneidentifier);


        //cal.setTime(reqdDate);

        officialSunrise = calculator.getOfficialSunriseForDate(cal);
        officialSunset = calculator.getOfficialSunsetForDate(cal);

        astronomicalSunrise = calculator.getAstronomicalSunriseForDate(cal);
        astronomicalSunset = calculator.getAstronomicalSunsetForDate(cal);
        civilSunrise = calculator.getCivilSunriseForDate(cal);
        civilSunset = calculator.getCivilSunsetForDate(cal);
        nauticalSunrise = calculator.getNauticalSunriseForDate(cal);
        nauticalSunset = calculator.getNauticalSunsetForDate(cal);
        saveDataInPreference();
        updateUIWithResults();

    }

    private void updateUIWithResults(){
        tvSunRise.setText(formateTime(officialSunrise));
        tvSunSet.setText(formateTime(officialSunset));
    }

    private void loadDataFromPreference() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);


        String latitude = sharedPreferences.getString("latitude", "00.00");
        String longitude = sharedPreferences.getString("longitude", "00.00");
        selectedtimezoneidentifier = sharedPreferences.getString(
                "selectedtimezoneidentifier", TimeZone.getDefault().getID());

    }

    private void saveDataInPreference() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.putString("selectedtimezoneidentifier", selectedtimezoneidentifier);
        editor.putString("sunrise", officialSunrise);
        editor.putString("sunset", officialSunset);
        editor.commit();
    }

    private void getCurrentLocation(){
        locationTrack = new LocationTracker(MainActivity.this);


        if (locationTrack.canGetLocation()) {
             longitude = locationTrack.getLongitude();
             latitude = locationTrack.getLatitude();

            drawMap(latitude,longitude);
        } else {

            locationTrack.showSettingsAlert();
        }
    }

    private void decrementDateByOne() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(reqDate);
        cal.add(Calendar.DATE, -1);
        reqDate = cal.getTime();
        tvDate.setText(formatDate(reqDate));
        updateDataAndCalculateSunriseSunset(latitude, longitude,cal);
    }

    private void incrementDateByOne() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(reqDate);
        cal.add(Calendar.DATE, 1);
        reqDate = cal.getTime();
        tvDate.setText(formatDate(reqDate));
        updateDataAndCalculateSunriseSunset(latitude, longitude,cal);
    }

    private void resetCurrentDate() {

        Calendar cal = Calendar.getInstance();
        reqDate = cal.getTime();
        tvDate.setText(formatDate(reqDate));
        updateDataAndCalculateSunriseSunset(locationTrack.getLatitude(), locationTrack.getLongitude(),cal);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
        return sdf.format(date);
    }


    private String formateTime(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
        Date dt;
        try {
            dt = sdf.parse(time);
            return sdfs.format(dt);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    // Service will start after 30 minutes.

    public void startService(Context context) {
        Calendar cal = Calendar.getInstance();
        long interval = 1000 * 60 * 30; // 30 minutes in milliseconds
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyBroadcastReciever.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                interval, pi);
    }

}
