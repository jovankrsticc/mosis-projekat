package com.example.myapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, DatePickerDialog.OnDateSetListener {

    private View popupInputDialogView;
    private GoogleMap mMap;
    private CheckBox filterkorisnik,filtervolonter;
    private ImageView btnFilterVolonter, addObject,profilpoziv,porukepoziv;
    private TextView cancelDialogButton;
    private DatabaseReference reference,objReference,friendRef;
    private FirebaseAuth fAuth;
    private StorageReference sReference;
    private LocationManager locationManager;
    private final int MIN_TIME=1000;
    private final int MIN_DISTANCE=1;
    private String userID;
    private HashMap<String,Marker> hashMapMarker = new HashMap<>();
    private HashMap<String,Marker> hashMapObjectMarter = new HashMap<>();
    private HashMap<Marker,String> hashMapMarkerID = new HashMap<>();
    private Collection<Marker> markers;
    private Button btn,radiusBtn,dateBtn;

    public boolean prikazikorisnike=true;
    public boolean prikazivolontere=true;

    public Date vremeistekaakcije;

    private Resources mResources;

    public boolean radiusClicked = false;
    public CircleOptions circleOptions;
    public EditText radius,date;
    public String radiusString="";
    public Circle mapCircle;
    public MyLocation currentLocation;
    public SearchView searchView;
    private AlertDialog alertDialog;
    private TextView vreme;

    DatabaseReference geo = FirebaseDatabase.getInstance().getReference().child("Geofence");
    GeoFire geoFire = new GeoFire(geo);
    ArrayList<String> arrayListUserIds = new ArrayList<String>();
    ArrayList<String> Prijatelji = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        vremeistekaakcije= new Date();

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        objReference = FirebaseDatabase.getInstance().getReference().child("Objects");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sReference = FirebaseStorage.getInstance().getReference();



        porukepoziv= (ImageView) findViewById(R.id.poruketab);
        porukepoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, ListaPoruka.class));
            }
        });
        profilpoziv = findViewById(R.id.mojprofil_volonter);
        profilpoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, RangActivity.class));
            }
        });

        ImageView b=(ImageView) findViewById(R.id.Porukebtn_map);
        b.setEnabled(false);
        b.setVisibility(View.INVISIBLE);

        addObject = findViewById(R.id.btnAddObj);
        addObject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startActivity(new Intent(MapsActivity.this, AddObjectActivity.class));

            }
        });

         friendRef= FirebaseDatabase.getInstance().getReference().child("Friendships").child(userID);
         friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childDataSnapshot : snapshot.getChildren()) {
                    Prijatelji.add(childDataSnapshot.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnFilterVolonter = findViewById(R.id.btnFilterOpenVolonter);

        btnFilterVolonter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);
                popupInputDialogView = layoutInflater.inflate(R.layout.dijalogvolonter, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                // Set title, icon, can not cancel properties.
                //alertDialogBuilder.setTitle("User Data Collection Dialog.");
                //alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setView(popupInputDialogView);


                // Set the inflated layout view object to the AlertDialog builder.
                alertDialogBuilder.setView(popupInputDialogView);

                // Create AlertDialog and show.
                alertDialog = alertDialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                lp.dimAmount=0.0f;

                alertDialog.getWindow().setAttributes(lp);
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                alertDialog.show();

                filterkorisnik= alertDialog.findViewById(R.id.ckprikayklijenata);
                filtervolonter=alertDialog.findViewById(R.id.ckprikazvolonter);
                filterkorisnik.setChecked(prikazikorisnike);
                filtervolonter.setChecked(prikazivolontere);

                filterkorisnik.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        prikazikorisnike=b;
                    }
                });

                //filtervolonter=alertDialog.findViewById(R.id.ckprikazvolonter);
                filtervolonter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        prikazivolontere=b;
                    }

                });

                TextView dateSpinner = (TextView) alertDialog.findViewById(R.id.vremetrajanajakcijeinputfilter);

                View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            showDatePickerDialog(v);
                        }
                        return true;
                    }
                };

                vreme=dateSpinner;

                dateSpinner.setOnTouchListener(Spinner_OnTouch);
            }
        });



        searchView = findViewById(R.id.searchView2);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String search = searchView.getQuery().toString();
                if (search != null || !search.equals("")) {
                    markers = hashMapMarker.values();
                    for (Marker m : markers) {
                        if (m.getTitle().equals(search)) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 16));
                            return true;
                        }
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });




        //createNotificationChannel();
    }


    public void zatvoridialogvolontera(View view)
    {
        alertDialog.cancel();
    }

    public void filtriranjevolontera(View view)
    {
        radius =popupInputDialogView.findViewById(R.id.profil_adresa);//findViewById(R.id.editRadius);
        if (mapCircle != null) {
            mapCircle.remove();
        }
        if (!radius.getText().toString().isEmpty()) {
            if (!radius.getText().toString().equals(radiusString)) {
                if (radiusString.equals(""))
                    radiusClicked = !radiusClicked;
                radiusString = radius.getText().toString();
                addCircle(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), Float.valueOf(radiusString));
            } else {
                radiusClicked = !radiusClicked;
                radiusString = "";
            }
        }
        alertDialog.cancel();
        getLocationUpdates();
        showObjectMarker();
        showUsers();

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
        getLocationUpdates();
        showObjectMarker();
        showUsers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 101)
            {
                getLocationUpdates();
            }
        }
        else
        {
            Toast.makeText(MapsActivity.this,"Permission Required",Toast.LENGTH_LONG).show();
        }
    }

    private void getLocationUpdates() {
        if(locationManager!=null)
        {
            if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
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
                    Toast.makeText(MapsActivity.this,"No provider.",Toast.LENGTH_LONG).show();
                }

            }
            else
            {
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            }

        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location!=null)
        {
            MyLocation myLocation= new MyLocation(location.getLatitude(),location.getLongitude());
            saveLocation(myLocation);
        }
        else
        {
            Toast.makeText(MapsActivity.this,"No location",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocation(MyLocation location) {
        reference.child("myLocation").setValue(location);
        currentLocation=location;
        if(radiusClicked)
        {
            if(mapCircle!=null)
                mapCircle.remove();
            addCircle(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), Float.valueOf(radiusString));
        }

        /*geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 2);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!key.equals(userID))
                {
                    arrayListUserIds.add(key);
                    sendNotification();
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
        });*/
    }



    private void showUsers() {

            reference.getParent().addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot.exists())
                    {
                        try {
                            //   Toast.makeText(MapsActivity.this,"Child added"+snapshot.getValue(),Toast.LENGTH_SHORT).show();
                            Log.d("testjox","Ulazi odavde i ne brise!!!!!!");
                            Marker marker = hashMapMarker.get(snapshot.getKey());
                            if(marker!=null)
                            {
                                hashMapMarkerID.remove(marker);
                                marker.remove();
                                hashMapMarker.remove(snapshot.getKey());
                            }
                            if(Prijatelji.contains(snapshot.getKey()))
                            {

                                addMarker(snapshot.getValue(User.class),snapshot.getKey(),"friend");
                            }
                            else
                            {
                                Log.d("testjox","id je:"+snapshot.getKey());
                                addMarker(snapshot.getValue(User.class),snapshot.getKey(),"user");
                            }

                            if(snapshot.getKey().equals(userID))
                            {
                                LatLng l=new LatLng(snapshot.child("myLocation").getValue(MyLocation.class).getLatitude(),snapshot.child("myLocation").getValue(MyLocation.class).getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l,12));
                            }

                        }
                        catch (Exception e)
                        {
                            Toast.makeText(MapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot.exists())
                    {
                        try {
                            //    Toast.makeText(MapsActivity.this,"Child changed"+snapshot.getValue(),Toast.LENGTH_SHORT).show();
                            Marker marker = hashMapMarker.get(snapshot.getKey());
                            if(marker!=null)
                            {
                                hashMapMarkerID.remove(marker);
                                marker.remove();
                                hashMapMarker.remove(snapshot.getKey());
                            }

                            if(Prijatelji.contains(snapshot.getKey()))
                            {
                                addMarker(snapshot.getValue(User.class),snapshot.getKey(),"friend");

                            }
                            else
                            {
                                addMarker(snapshot.getValue(User.class),snapshot.getKey(),"user");
                            }

                            /*if(snapshot.getKey().equals(userID))
                            {
                                LatLng l=new LatLng(snapshot.child("myLocation").getValue(MyLocation.class).getLatitude(),snapshot.child("myLocation").getValue(MyLocation.class).getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l,12));
                            }*/
                        }

                        catch (Exception e)
                        {
                            Toast.makeText(MapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    private void addMarker(User user,String id,String typeMarker) {

        if((user.userType.equals("Volonter") & prikazivolontere==true)||(user.userType.equals("Korisnik") & prikazikorisnike==true))
        {
                LatLng location = new LatLng(user.myLocation.getLatitude(), user.myLocation.getLongitude());
                if (typeMarker.equals("friend")) {
                    sReference.child("profile_images").child(id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(MapsActivity.this)
                                    .asBitmap()
                                    .load(uri.toString())
                                    .listener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, java.lang.Object model, Target<Bitmap> target, boolean isFirstResource) {

                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, java.lang.Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                                            Bitmap smallMarker = Bitmap.createScaledBitmap(resource, 100, 100, false);
                                            //Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(value.getMyLocation().getLatitude(), value.getMyLocation().getLongitude()))
                                            //        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));


                                            int boja=Color.BLUE;
                                            if(user.userType.equals("Volonter"))
                                            {
                                                boja=Color.RED;
                                            }


                                            Marker marker = mMap.addMarker(new MarkerOptions()
                                                    .position(location)
                                                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapCircle(smallMarker,boja)))
                                                    .title(user.userName)
                                                    .snippet("Ime: " + user.firstName + "\n" +
                                                            "Prezime: " + user.lastName + "\n")


                                            );
                                            mMap.setOnMarkerClickListener(MapsActivity.this);
                                            hashMapMarker.put(id, marker);
                                            hashMapMarkerID.put(marker, id);
                                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                                // Use default InfoWindow frame
                                                @Override
                                                public View getInfoWindow(Marker arg0) {
                                                    return null;
                                                }

                                                // Defines the contents of the InfoWindow
                                                @Override
                                                public View getInfoContents(Marker arg0) {

                                                    // Getting view from the layout file infowindowlayout.xml
                                                    View v = getLayoutInflater().inflate(R.layout.infowindowlayout, null);

                                                    LatLng latLng = arg0.getPosition();

                                                    //Button poruka = (Button) v.findViewById(R.id.poruka_btn);
                                                    TextView tv1 = (TextView) v.findViewById(R.id.textView1);
                                                    TextView tv2 = (TextView) v.findViewById(R.id.textView2);
                                                    String title = arg0.getTitle();
                                                    String informations = arg0.getSnippet();

                                                    tv1.setText(title);
                                                    tv2.setText(informations);


                                                    return v;

                                                }
                                            });

                                            return true;

                                        }
                                    })
                                    .centerCrop()
                                    .preload();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapsActivity.this, "ERROR" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    addMarker(user, id, "user");
                                }
                            });

                } else if (typeMarker.equals("user")) {
                    Marker marker1;
                    if(id.equals(userID))
                    {
                        marker1 = mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(user.userName)
                                .icon(BitmapFromVector(getApplicationContext(), R.drawable.mojalokacija))
                                .snippet("Ime: " + user.firstName + "\n" + "Prezime: " + user.lastName)

                        );

                        mMap.setOnMarkerClickListener(MapsActivity.this);
                        hashMapMarker.put(id, marker1);
                        hashMapMarkerID.put(marker1, id);
                    }
                    else {
                        if (user.userType.equals("Volonter")) {
                            marker1 = mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(user.userName)
                                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.beli_persona))
                                    .snippet("Ime: " + user.firstName + "\n" + "Prezime: " + user.lastName)

                            );

                            mMap.setOnMarkerClickListener(MapsActivity.this);
                            hashMapMarker.put(id, marker1);
                            hashMapMarkerID.put(marker1, id);
                        } else if (user.userType.equals("Korisnik")) {
                            marker1 = mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(user.userName)
                                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_person_pin_circle_24))
                                    .snippet("Ime: " + user.firstName + "\n" + "Prezime: " + user.lastName)
                            );

                            mMap.setOnMarkerClickListener(MapsActivity.this);
                            hashMapMarker.put(id, marker1);
                            hashMapMarkerID.put(marker1, id);

                        }
                    }




                } else {
                    Toast.makeText(MapsActivity.this, "Greska", Toast.LENGTH_SHORT).show();
                }
        }

    }





    private BitmapDescriptor BitmapFromVector(Context applicationContext, int id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(applicationContext, id);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);


        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);


        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap BitmapCircle(Bitmap bitmap,int boja)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 100, h + 100, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 50, (h / 2) + 50, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(bitmap, 50, 50, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(boja);
        p.setStrokeWidth(10);
        c.drawCircle((w / 2) + 50, (h / 2) + 50, radius, p);

        return output;
    }

    private void showObjectMarker() {
        com.google.firebase.Timestamp t=new Timestamp(vremeistekaakcije);
        Log.d("testjox","marker vreme"+t.getSeconds());
        for (String key: hashMapObjectMarter.keySet()) {
           hashMapObjectMarter.get(key).remove();
        }
        objReference.orderByChild("end")
                .startAt(t.getSeconds()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    try{

                        Marker marker = hashMapObjectMarter.get(snapshot.getKey());
                        Log.d("testjox","marker"+snapshot.getValue(MyObject.class).name);
                        if(marker!=null)
                        {
                            //hashMapMarkerID.remove(marker);
                            marker.remove();

                            hashMapObjectMarter.remove(snapshot.getKey());
                        }

                        addObjectMarker(snapshot.getValue(MyObject.class),snapshot.getKey());
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    try
                    {
                        Marker marker = hashMapObjectMarter.get(snapshot.getKey());
                        if(marker!=null)
                        {
                            //hashMapMarkerID.remove(marker);
                            marker.remove();

                            hashMapObjectMarter.remove(snapshot.getKey());
                        }
                        addObjectMarker(snapshot.getValue(MyObject.class),snapshot.getKey());

                    }
                    catch (Exception e)
                    {

                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "Date");

    }

    private void addObjectMarker(MyObject myObject,String id) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(myObject.end * 1000);
        Date date = cal.getTime();
        LatLng mylocation = new LatLng(myObject.location.getLatitude(), myObject.location.getLongitude());
        Toast.makeText(MapsActivity.this,"LAT "+mylocation.latitude+"  LONG "+mylocation.longitude,Toast.LENGTH_SHORT).show();
        Marker objMarker = mMap.addMarker(new MarkerOptions()
                .position(mylocation)
                .title(myObject.name+"  "+date.toString())
                .icon(BitmapFromVector(getApplicationContext(),R.drawable.ic_baseline_store_24))

        );

        mMap.setOnMarkerClickListener(MapsActivity.this);
        hashMapObjectMarter.put(id,objMarker);
        //ID.put(objMarker,id);
    }

    private void addCircle(LatLng latlng, float radius) {

        circleOptions = new CircleOptions();
        circleOptions.center(latlng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        circleOptions.visible(true);
        mapCircle = mMap.addCircle(circleOptions);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.showInfoWindow();
        ImageView b=(ImageView) findViewById(R.id.Porukebtn_map);
        if(hashMapMarkerID.get(marker)!=null && hashMapMarkerID.get(marker)!= userID )
        {
            b.setVisibility(View.VISIBLE);
            b.setEnabled(true);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PorukaZaglavlje.kome=hashMapMarkerID.get(marker);
                    Log.d("firebase",PorukaZaglavlje.kome);
                    startActivity(new Intent(MapsActivity.this, Chat.class));
                }
            });
        }
        else
        {
            b.setVisibility(View.INVISIBLE);
            b.setEnabled(false);
        }

        return true;
    }

    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name = "channel";
            String description = "channel for notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifications", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        Intent intent = new Intent(this, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifications")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("USER DETECTED")
                .setContentText("User spotted nearby")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());

        vremeistekaakcije=c.getTime();

        vreme=(TextView)  alertDialog.findViewById(R.id.vremetrajanajakcijeinputfilter);
        vreme.setText(currentDateString);
    }
}