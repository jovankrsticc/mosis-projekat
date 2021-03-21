package com.example.myapp;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    @Exclude
    public String key;

    public String firstName;
    public String lastName;
    public String userName;
    public String number;
    public String email;
    public String password;
    public String userType;
    public Integer rate;
    public MyLocation myLocation;

    public User()
    {

    }

    public User(String firstName,String lastName, String userName, String number,String email,String password,String userType)
    {
        this.firstName=firstName;
        this.lastName=lastName;
        this.userName=userName;
        this.number=number;
        this.email=email;
        this.password=password;
        this.userType="user";
        this.rate=0;
        this.myLocation= new MyLocation();
    }



}
