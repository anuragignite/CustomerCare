package com.example.anurag.customercare.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

/**
 * Created by anurag on 19/05/17.
 */

public class CustomCareApplication extends Application {

    private static CustomCareApplication sINSTANCE;

    private DatabaseReference            mDatabaseReference;

    private String[]                     mNames = { "Anurag Kumar", "Komal Singh", "Bhartendu Tanwar", "Aastha Priya", "Prashant Kumar", "Sudipta Banergee" };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // https://developer.android.com/reference/android/support/multidex/MultiDexApplication.html
        // http://stackoverflow.com/questions/32880802/twitter-fabric-multidex-causes-noclassdeffounderror
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sINSTANCE = this;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static CustomCareApplication getInstance() {
        return sINSTANCE;
    }

    public DatabaseReference getDatabaseReference() {
        return mDatabaseReference;
    }

    public String getName() {
        return mNames[new Random().nextInt(mNames.length)];
    }
}
