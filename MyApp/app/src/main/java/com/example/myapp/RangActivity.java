package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RangActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<User, AddFriendViewHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    ImageView poruketabpoziv,maptabporukerpoziv;
    Button prikazmogprofila;
    DatabaseReference korisniciReference, zahteviZaPrijateljstvoReference, korisnik,listaprijatelja,ocenjivanje;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    private String userID;
    Query query;
    String trenutniKorisnik;
    User ulogovaniKorisnik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang);

        prikazmogprofila = (Button) findViewById(R.id.mojprofilprikazbrnrang);
        prikazmogprofila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RangActivity.this, Profil.class));
            }
        });

        maptabporukerpoziv = (ImageView) findViewById(R.id.maptabprofil);
        maptabporukerpoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RangActivity.this, MapsActivity.class));
            }
        });

        poruketabpoziv = (ImageView) findViewById(R.id.poruketabprofil);
        poruketabpoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RangActivity.this, ListaPoruka.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        korisniciReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(userID);
        listaprijatelja = FirebaseDatabase.getInstance().getReference().child("Friendships");
        ocenjivanje=FirebaseDatabase.getInstance().getReference().child("Users");
        zahteviZaPrijateljstvoReference = FirebaseDatabase.getInstance().getReference().child("Friend requests");
        query = korisniciReference.orderByChild("rate");
        //korisniciReference = (DatabaseReference) korisniciReference.orderByChild("brojTokena");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        trenutniKorisnik = mAuth.getCurrentUser().getUid();
        korisnik = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db= korisnik.child("Friendships");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imeKorisnika = snapshot.child(trenutniKorisnik).child("firstName").getValue(String.class);
                String prezimeKorisnika = snapshot.child(trenutniKorisnik).child("lastName").getValue(String.class);
                String korisnickoIme = snapshot.child(trenutniKorisnik).child("userName").getValue(String.class);
                String emailAdresa = snapshot.child(trenutniKorisnik).child("email").getValue(String.class);
                String lozinka = snapshot.child(trenutniKorisnik).child("password").getValue(String.class);
                String brojTelefona = snapshot.child(trenutniKorisnik).child("number").getValue(String.class);
                Integer ocena = snapshot.child(trenutniKorisnik).child("rate").getValue(Integer.class);
                String tipkorisnika = snapshot.child(trenutniKorisnik).child("userType").getValue(String.class);
                ulogovaniKorisnik = new User(imeKorisnika, prezimeKorisnika, emailAdresa, korisnickoIme, brojTelefona, lozinka, ocena,tipkorisnika);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LoadKorisnici();
    }

    private void LoadKorisnici() {
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, AddFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position, @NonNull User model) {
                holder.mUserName.setText(model.firstName);

                holder.mPrezime.setText(model.lastName);
                holder.mUuidKorisnika.setText(getRef(position).getKey().toString());
                String idKorisnika = getRef(position).getKey().toString();

                //
                ocenjivanje.child(idKorisnika).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        float newFloat = (float) task.getResult().child("rate").getValue(Integer.class);
                        Log.d("testjox",String.valueOf(newFloat));
                        holder.oceneprikaz.setRating(newFloat);
                        holder.oceneprikaz.setNumStars(5);
                    }
                });

                //
                holder.oceneprikaz.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        model.rate=(int)(model.rate+v)/2;

                        ocenjivanje.child(idKorisnika).child("rate").setValue(model.rate);
                    }
                });
                /*holder.mAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //DatabaseReference rf = zahteviZaPrijateljstvoReference.child(idKorisnika).child(trenutniKorisnik);
                        PorukaZaglavlje.kome=idKorisnika;

                        startActivity(new Intent(RangActivity.this, Chat.class));
                        //ovo bilo pod komentar zahteviZaPrijateljstvoReference.child(getRef(position).getKey().toString()).child(key).setValue(trenutniKorisnik);
                        //rf.setValue(ulogovaniKorisnik);
                    }
                });*/
            }

            @NonNull
            @Override
            public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_friends, parent, false);
                return new AddFriendViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }


}