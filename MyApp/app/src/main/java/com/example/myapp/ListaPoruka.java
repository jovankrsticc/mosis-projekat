package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListaPoruka extends AppCompatActivity {

    FirebaseRecyclerAdapter<User, ListPorukeViewHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    ImageView profilrangtabpoziv,maptabporukerpoziv;
    DatabaseReference korisniciReference, zahteviZaPrijateljstvoReference, korisnik,listaprijatelja,listaporuka;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    LinearLayout layout1;
    Query query;
    private String userID;
    HashMap<String, String> korisniciporuka;
    String trenutniKorisnik;
    User ulogovaniKorisnik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_poruka);

        maptabporukerpoziv = (ImageView) findViewById(R.id.maptabporuke);
        maptabporukerpoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListaPoruka.this, MapsActivity.class));
            }
        });

        profilrangtabpoziv = (ImageView) findViewById(R.id.mojprofil_volonter3);
        profilrangtabpoziv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListaPoruka.this, RangActivity.class));
            }
        });

        layout1 = (LinearLayout) findViewById(R.id.Layout_listaprimljenihporuka);
        korisniciporuka =  new HashMap<String, String>();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        korisniciReference = FirebaseDatabase.getInstance().getReference().child("Users");
        String put;
        put=(new StringBuilder()).append("Poruke/").append(userID).toString();
        listaporuka = FirebaseDatabase.getInstance().getReference().child(put);

        query = korisniciReference;
        //korisniciReference = (DatabaseReference) korisniciReference.orderByChild("brojTokena");
        //recyclerView = findViewById(R.id.listaporukarcv);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        trenutniKorisnik = mAuth.getCurrentUser().getUid();
        korisnik = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db= korisnik.child("Users");
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

        listaporuka.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        String idposiljaoca=child.getKey();
                        korisniciReference.child(child.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {

                                }
                                else
                                {

                                    User u = task.getResult().getValue(User.class);


                                    addMessageBox(u.firstName+" "+u.lastName, 1,idposiljaoca);
                                }
                            }
                        });
                        Log.d("testjox ",child.getKey());   //Pega o nome de cada tipo de arte

                    }
                }
            }
        });
        //LoadKorisnici();
    }
    public void addMessageBox(String message, int type,String idKorisnika){
        TextView textView = new TextView(ListaPoruka.this);
        textView.setTextSize(16);
        //textView.setTextColor(Color.rgb(255,0,0));
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
        }
        else{
            textView.setBackgroundResource(R.drawable.rounded_corner2);
        }
        Log.d("firebase",message);
        layout1.addView(textView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PorukaZaglavlje.kome=idKorisnika;

                startActivity(new Intent(ListaPoruka.this, Chat.class));
            }
        });
    }

    /*private void LoadKorisnici() {
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, ListPorukeViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull ListPorukeViewHolder holder, int position, @NonNull User model) {
                holder.PorukeUserName.setText(model.firstName);

                holder.PorukePrezime.setText(model.lastName);
            }

            @NonNull
            @Override
            public ListPorukeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sve_poruke, parent, false);
                return new ListPorukeViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }*/
}