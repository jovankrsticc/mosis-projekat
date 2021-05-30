package com.example.myapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static java.lang.Math.abs;

public class ListVievAdapter extends ArrayAdapter<User> {

    private AppCompatActivity activity;
    private List<User> userList;

    public ListVievAdapter(AppCompatActivity context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.userList = objects;
    }

    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AddFriendViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.all_friends, parent, false);
            holder = new AddFriendViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (AddFriendViewHolder) convertView.getTag();
            //holder.ratingBar.getTag(position);
        }

        holder.oceneprikaz.setOnRatingBarChangeListener(onRatingChangedListener(holder, position));


        /*holder.oceneprikaz.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                Log.d("Adapter", "star: ");
                return true;
            }
        });*/
        /*holder.oceneprikaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float v=holder.oceneprikaz.getRating();
                User item = getItem(position);
                item.rate=(int)(item.rate - v)/2 ;
                DatabaseReference ocenjivanje= FirebaseDatabase.getInstance().getReference().child("Users");
                ocenjivanje.child(item.key).child("rate").setValue(item.rate);

                Log.d("testjox", "star99: " + item.key);
            }
        });*/


        holder.oceneprikaz.setTag(position);
        float ocena=getItem(position).rate;
        Log.d("testjox","Ocena=="+ocena);
        holder.oceneprikaz.setRating(abs(ocena));
        holder.mUserName.setText(getItem(position).firstName);
        holder.mPrezime.setText(getItem(position).lastName);
        return convertView;
    }

    private RatingBar.OnRatingBarChangeListener onRatingChangedListener(final AddFriendViewHolder holder, final int position) {
        return new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                User item = getItem(position);
                item.rate=(int)(item.rate - v)/2 ;
                DatabaseReference ocenjivanje= FirebaseDatabase.getInstance().getReference().child("Users");
                ocenjivanje.child(item.key).child("rate").setValue(item.rate);

                Log.d("testjox", "star: " + item.key);
            }
        };
    }


}