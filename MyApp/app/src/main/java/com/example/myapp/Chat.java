package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {

    LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    User korisnik,Drugikorisnik;
    FirebaseDatabase database;
    DatabaseReference ref,ref1,ref2,dalismoprijatelji,prijateljstva;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        layout = (LinearLayout) findViewById(R.id.layout_mesages);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.scrollView_mesages);

        database = FirebaseDatabase.getInstance();
        fAuth = FirebaseAuth.getInstance();
        ref1 = database.getReference("Users");
        prijateljstva = FirebaseDatabase.getInstance().getReference().child("Friendships");

        String id;

        if (fAuth.getCurrentUser() != null) {
            id = fAuth.getCurrentUser().getUid();
            ref1.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        User u = task.getResult().getValue(User.class);
                        korisnik =  u;
                        PorukaZaglavlje.korisnik=id;
                        //Log.d("firebase", u);
                        // Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });

            ref1.child(PorukaZaglavlje.kome).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        User u = task.getResult().getValue(User.class);
                        Drugikorisnik =  u;
                        TextView t= (TextView) findViewById(R.id.ImeIPrezimeUchatu);
                        t.setText(u.firstName+" "+u.lastName);
                        //Log.d("firebase", u);
                        // Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
            Button but = (Button) findViewById(R.id.PruziusluguKorisnikuChat);

            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String punanziv=(new StringBuilder()).append("Friendships/").append(id).toString();
                    dalismoprijatelji=database.getReference(punanziv);
                    dalismoprijatelji.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(PorukaZaglavlje.kome)) {
                                // run some code
                                Log.d("firebase","Prijatelji velikai!!");
                            }
                            else
                            {

                                Log.d("firebase","Nismo jos prijatelji!!");
                                prijateljstva.child(id).child(PorukaZaglavlje.kome).setValue(Drugikorisnik);
                                prijateljstva.child(PorukaZaglavlje.kome).child(id).setValue(korisnik);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            });

            //database = FirebaseDatabase.getInstance();
            String test = PorukaZaglavlje.kome;
            String put;
            put=(new StringBuilder()).append("Poruke/").append(id).append("/").append(PorukaZaglavlje.kome).toString();
            Log.d("firebase", put);
            ref = database.getReference(put);
            put=(new StringBuilder()).append("Poruke/").append(PorukaZaglavlje.kome).append("/").append(id).toString();
            ref2=database.getReference(put);

            //reference2 = FirebaseDatabase.getInstance();//new Firebase("https://android-chat-app-e711d.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String messageText = messageArea.getText().toString();
                    messageArea.setText("");
                    if (!messageText.equals("")) {
                        Poruka p = new Poruka(PorukaZaglavlje.korisnik,PorukaZaglavlje.kome,messageText);

                        ref.push().setValue(p);
                        ref2.push().setValue(p);

                        //reference2.push().setValue(map);
                    }
                }
            });
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    layout.removeAllViews();
                    for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                        Poruka p  = singleSnapshot.getValue(Poruka.class);
                        if(p!=null){
                            //Log.d("firebase", p.kome);
                            String message = p.telo;
                            String userName = p.korisnik;

                            if (userName.equals(PorukaZaglavlje.korisnik.toString())) {
                                addMessageBox("JA: " + message, 1);
                            } else {
                                addMessageBox(  Drugikorisnik.firstName+" "+Drugikorisnik.lastName+": "+message  , 2);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            /*ref.addChildEventListener( new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Poruka p  = snapshot.getValue(Poruka.class);
                    if(p!=null){
                        //Log.d("firebase", p.kome);
                        String message = p.telo;
                        String userName = p.korisnik;
                        Log.d("firebase",userName);
                        if (userName.equals(PorukaZaglavlje.korisnik.toString())) {
                            addMessageBox("You:-\n" + message, 1);
                        } else {
                            addMessageBox(PorukaZaglavlje.kome + ":-\n" + message, 2);
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
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w( "loadPost:onCancelled", databaseError.toException());
                }
            });*/

        }
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
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
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

}