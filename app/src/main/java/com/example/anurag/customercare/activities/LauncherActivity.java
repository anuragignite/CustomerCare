package com.example.anurag.customercare.activities;

import java.util.UUID;

import com.example.anurag.customercare.R;
import com.example.anurag.customercare.application.CustomCareApplication;
import com.example.anurag.customercare.pojos.ActiveConnection;
import com.example.anurag.customercare.pojos.Customer;
import com.example.anurag.customercare.pojos.Executive;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String      LOG_TAG = "LAUNCHER_ACTIVITY";

    private Button                   mCustomerMode, mExecutiveMode;

    private SharedPreferences        mDefaultSharedPreferences;

    private SharedPreferences.Editor mEditor;

    private String                   mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        setUserId();
        findViews();
        bindViews();
    }

    private void findViews() {
        mCustomerMode = (Button) findViewById(R.id.b_customer_mode);
        mExecutiveMode = (Button) findViewById(R.id.b_executive_mode);
    }

    private void bindViews() {
        mCustomerMode.setOnClickListener(this);
        mExecutiveMode.setOnClickListener(this);
    }

    private void setUserId() {
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mDefaultSharedPreferences.edit();
        mUserId = mDefaultSharedPreferences.getString("USER_ID", "");
        if (TextUtils.isEmpty(mUserId)) {
            mUserId = UUID.randomUUID().toString();
            mEditor.putString("USER_ID", mUserId);
            mEditor.commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.b_customer_mode:
            startActivity(new Intent(LauncherActivity.this, CustomerActivity.class));
            break;
        case R.id.b_executive_mode:
            startActivity(new Intent(LauncherActivity.this, ExecutiveActivity.class));
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
