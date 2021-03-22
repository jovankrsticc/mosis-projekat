package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class Profil extends AppCompatActivity {

    private Button izmena;
    private EditText ime,prezime,korisnicko,lozinka,telefon,adresa;
    private FirebaseAuth fAuth;
    private FirebaseDatabase database= FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        ime= findViewById(R.id.profil_ime);
        prezime = findViewById(R.id.profil_prezime);
        korisnicko = findViewById(R.id.profil_korisnicko);
        lozinka = findViewById(R.id.profil_lozinka);
        adresa = findViewById(R.id.profil_adresa);
        telefon= findViewById(R.id.profil_telefon);

        fAuth=FirebaseAuth.getInstance();
        ref=database.getReference("Users");

        if(fAuth.getCurrentUser()!=null)
        {
            id=fAuth.getCurrentUser().getUid();
            ref.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        User u= task.getResult().getValue(User.class);
                        ime.setText(u.firstName);
                        prezime.setText(u.lastName);
                        korisnicko.setText(u.userName);
                        adresa.setText(u.email);
                        telefon.setText(u.number);
                        Log.d("firebase", u.firstName);
                       // Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
        }


    }
}