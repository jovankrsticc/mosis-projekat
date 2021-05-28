package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class AddObjectActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener  {

    DatabaseReference currentUser, newObject;
    FirebaseAuth fAuth;
    String userID;
    EditText userLong, userLat, objectName;
    Button btnName;

    public Date vremeistekaakcije;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);
        userLong = findViewById(R.id.editTextUserLong);
        userLat = findViewById(R.id.editTextUserLat);
        objectName = findViewById(R.id.objName);
        btnName = findViewById(R.id.addObj);
        newObject = FirebaseDatabase.getInstance().getReference().child("Objects");
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        currentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLong.setText(snapshot.child("myLocation").child("longitude").getValue().toString());
                userLat.setText(snapshot.child("myLocation").child("latitude").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String objectNameS = objectName.getText().toString();
                String userLongS = userLong.getText().toString();
                String userLatS = userLat.getText().toString();
                if(objectNameS.isEmpty())
                {
                    Toast.makeText(AddObjectActivity.this, "Object name is required!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String key = newObject.push().getKey();
                    Timestamp t=new Timestamp(vremeistekaakcije);
                    Log.d("testjox","Vreme"+vremeistekaakcije);
                    MyObject myObject=new MyObject(objectNameS,Double.valueOf(userLatS),Double.valueOf(userLongS),t.getSeconds());
                    newObject.child(key).setValue(myObject);
                    Intent intent=new Intent(AddObjectActivity.this, MapsActivity.class);
                    Toast.makeText(AddObjectActivity.this, "Object added.", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
            }
        });


        TextView dateSpinner = (TextView)findViewById(R.id.vremetrajanajakcijeinput);

        View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showDatePickerDialog(v);
                }
                return true;
            }
        };



        dateSpinner.setOnTouchListener(Spinner_OnTouch);

    }


    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "Date");

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());

        vremeistekaakcije=c.getTime();

        TextView vreme=(TextView) findViewById(R.id.vremetrajanajakcijeinput);
        vreme.setText(currentDateString);
    }
}