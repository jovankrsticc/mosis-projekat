package com.example.myapp;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListPorukeViewHolder extends RecyclerView.ViewHolder{
    TextView PorukeUserName, PorukeUuidKorisnika,PorukePrezime;
    //Button AddFriend;


    public ListPorukeViewHolder(@NonNull View itemView) {
        super(itemView);
        PorukeUserName = itemView.findViewById(R.id.poruke_ime_rang);
        PorukePrezime = itemView.findViewById(R.id.poruke_prezime_rang);

        //mUuidKorisnika = itemView.findViewById(R.id.uuid_korisnika_poruke);
        //mUuidKorisnika.setVisibility(View.GONE);
        //mAddFriend = itemView.findViewById(R.id.dodajPrijatelja);

    }
}
