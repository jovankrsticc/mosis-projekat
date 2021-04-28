package com.example.myapp;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Poruka {
    @Exclude
    public String key;

    public String korisnik;
    public String kome;
    public String telo;

    public Poruka()
    {

    }

    public Poruka(String ko,String ka,String t)
    {
        kome=ka;
        korisnik = ko;
        telo=t;
    }
}
