package com.example.myapp;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class VolonterService extends Service implements LocationListener {
    public static final String CHANNEL_ID = "volonterServiceChannel";

    DatabaseReference geo = FirebaseDatabase.getInstance().getReference().child("Geofence");
    GeoFire geoFire = new GeoFire(geo);

    public MyLocation currentLocation;
    private DatabaseReference reference;
    private FirebaseAuth fAuth;
    private String userID;
    private LocationManager locationManager;
    private final int MIN_TIME=1000;
    private final int MIN_DISTANCE=1;
    ArrayList<String> arrayListUserIds = new ArrayList<String>();

    public VolonterService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        getLocationUpdates();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Volonter Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.beli_persona)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void getLocationUpdates() {
        if(locationManager!=null)
        {
            if(ActivityCompat.checkSelfPermission(VolonterService.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(VolonterService.this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                }
                else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                }
                else
                {

                }

            }
            else
            {
                //ActivityCompat.requestPermissions(VolonterService.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            }

        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("testjox","Lokacija uso!");
        if(location!=null)
        {
            MyLocation myLocation= new MyLocation(location.getLatitude(),location.getLongitude());
            saveLocation(myLocation);
        }
    }

    private void saveLocation(MyLocation location) {
        reference.child("myLocation").setValue(location);
        currentLocation=location;
        Log.d("testjox","Cuva!");
        geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 2);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!key.equals(userID))
                {
                    //arrayListUserIds.add(key);
                    sendNotification();
                    Log.d("testjox","Salje!!!");
                }
            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }
            @Override
            public void onGeoQueryReady() {
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    public void sendNotification() {
        String input = "Novi klijent u blizizni";
        Intent serviceIntent = new Intent(this, VolonterService.class);
        serviceIntent.putExtra("inputExtra", input);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}