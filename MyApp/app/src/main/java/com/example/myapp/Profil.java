package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profil extends AppCompatActivity {

    private Button izmena,odjavljivanje;
    private ImageView myImageView,maptab,poruketab,rangtab;
    private EditText ime,prezime,korisnicko,lozinka,telefon,adresa;
    private FirebaseAuth fAuth;
    private FirebaseDatabase database= FirebaseDatabase.getInstance();
    private RadioButton notiukl,notiisklj;
    private DatabaseReference ref;
    private StorageReference sReference;
    private String id;
    private  boolean izmene;
    public static final String CHANNEL_ID = "volonterServiceChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        createNotificationChannel();
        myImageView = findViewById(R.id.imageView_registracija);
        ime= findViewById(R.id.profil_ime);
        prezime = findViewById(R.id.profil_prezime);
        korisnicko = findViewById(R.id.profil_korisnicko);
        lozinka = findViewById(R.id.profil_lozinka);
        adresa = findViewById(R.id.profil_adresa);
        telefon= findViewById(R.id.profil_telefon);
        notiukl=findViewById(R.id.rbUkljuciNotifikacije);
        notiisklj=findViewById(R.id.rbIsljuci_notifikacije);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (VolonterService.class.getName().equals(service.service.getClassName())) {
                notiukl.setChecked(true);
                notiisklj.setChecked(false);
            }
        }

        maptab= findViewById(R.id.mapprofiltab);
        maptab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profil.this, MapsActivity.class));
            }
        });
        poruketab = findViewById(R.id.porukeprofiltab);
        poruketab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profil.this, ListaPoruka.class));
            }
        });

        rangtab = findViewById(R.id.profilrangtab);
        rangtab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profil.this, RangActivity.class));
            }
        });

        odjavljivanje =  findViewById(R.id.odjavaprofil);
        odjavljivanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        fAuth=FirebaseAuth.getInstance();
        ref=database.getReference("Users");
        sReference = FirebaseStorage.getInstance().getReference();

        if(fAuth.getCurrentUser()!=null) {
            id = fAuth.getCurrentUser().getUid();
            ref.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        User u = task.getResult().getValue(User.class);
                        ime.setText(u.firstName);
                        prezime.setText(u.lastName);
                        korisnicko.setText(u.userName);
                        //adresa.setText(u.email);
                        telefon.setText(u.number);
                        Log.d("firebase", u.firstName);
                        // Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
        }

       ucitajsliku();

        izmene=false;
        izmena= findViewById(R.id.btnIzmenaProfila);
        izmena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(izmene)
                {
                    ime.setEnabled(false);
                    prezime.setEnabled(false);
                    korisnicko.setEnabled(false);
                    lozinka.setEnabled(false);
                    //adresa.setEnabled(false);
                    telefon.setEnabled(false);
                    izmena.setText("Izmeni");
                    izmene=false;
                    ref.child(id).child("firstName").setValue(ime.getText().toString());
                    ref.child(id).child("lastName").setValue(prezime.getText().toString());
                    ref.child(id).child("number").setValue(telefon.getText().toString());
                    ref.child(id).child("password").setValue(lozinka.getText().toString());
                    ref.child(id).child("userName").setValue(korisnicko.getText().toString());


                }else
                {
                    ime.setEnabled(true);
                    prezime.setEnabled(true);
                    korisnicko.setEnabled(true);
                    lozinka.setEnabled(true);
                    adresa.setEnabled(true);
                    telefon.setEnabled(true);
                    izmena.setText("Sacuvaj");
                    izmene=true;
                }
            }
        });

    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.rbIsljuci_notifikacije:
                if (checked)
                    stopService();
                    break;
            case R.id.rbUkljuciNotifikacije:
                if (checked)
                    startService();
                    break;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Volonter Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void startService() {
        String input = "Ukljucen je servis!!!";
        Intent serviceIntent = new Intent(this, VolonterService.class);
        serviceIntent.putExtra("inputExtra", input);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, VolonterService.class);
        stopService(serviceIntent);
    }

    public void ucitajsliku()
    {
        sReference.child("profile_images").child(id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide
                        .with(Profil.this)
                        .load(uri)
                        .centerCrop()
                        .into(myImageView);
            }
        });
    }
}