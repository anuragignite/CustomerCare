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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String      LOG_TAG          = "CUSTOMER_ACTIVITY";

    private static final String      ACTIVE_THREADS   = "activethreads";

    private static final String      PENDING_REQUESTS = "pendingrequests";

    private static final String      CUSTOMERS        = "customers";

    private static final String      EXECUTIVES       = "executives";

    private TextView                 mCustomerId, mCustomerRating, mCustomerTags, mCall;

    private SharedPreferences        mDefaultSharedPreferences;

    private SharedPreferences.Editor mEditor;

    private String                   mUserId;

    private ActiveConnection         activeConnection;

    private ChildEventListener       mActiveThreadChildEventListener;

    private String                   mActiveConnectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        setUserId();
        findViews();
        bindViews();

        initValuesForCustomer();
    }

    private void findViews() {
        mCustomerId = (TextView) findViewById(R.id.tv_c_id);
        mCustomerRating = (TextView) findViewById(R.id.tv_c_rating);
        mCustomerTags = (TextView) findViewById(R.id.tv_c_tags);
        mCall = (TextView) findViewById(R.id.tv_call);
    }

    private void bindViews() {
        mCall.setOnClickListener(this);
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

    private void initValuesForCustomer() {
        CustomCareApplication.getInstance().getDatabaseReference().child(CUSTOMERS).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mUserId).exists()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(mUserId);
                    customer.setRating(4.5);
                    CustomCareApplication.getInstance().getDatabaseReference().child(CUSTOMERS).child(mUserId).setValue(customer);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "initValuesForCustomer : " + databaseError);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.tv_call:
            initiateCustomerCareCall();
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningActiveThreads();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopListeningActiveThreads();
        if (!TextUtils.isEmpty(mActiveConnectionId)) {
            CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).child(mActiveConnectionId).removeValue();
        }
    }

    private void startListeningActiveThreads() {
        mActiveThreadChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildAdded : " + s);
                try {
                    if (dataSnapshot.exists()) {
                        ActiveConnection activeConnection = dataSnapshot.getValue(ActiveConnection.class);
                        String customerId = activeConnection.getCustomerId();
                        if (mUserId.equalsIgnoreCase(customerId)) {
                            mActiveConnectionId = activeConnection.getCustomerId() + activeConnection.getExecutiveId();
                            CustomCareApplication.getInstance().getDatabaseReference().child(EXECUTIVES).child(activeConnection.getExecutiveId()).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Executive executive = dataSnapshot.getValue(Executive.class);
                                        mCustomerId.setText(getString(R.string.id_text, executive.getExecutiveId()));
                                        mCustomerRating.setText(getString(R.string.rating_text, executive.getRating()));
                                        // setViewVisibility(View.GONE, mCall);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(LOG_TAG, "initValuesForCustomer : " + databaseError);
                                }
                            });
                        }
                    }
                } catch (Throwable throwable) {

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildChanged : " + s);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildRemoved : ");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildMoved: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onCancelled : ");
            }
        });
    }

    private void stopListeningActiveThreads() {
        if (mActiveThreadChildEventListener != null) {
            CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).removeEventListener(mActiveThreadChildEventListener);
        }
    }

    private void initiateCustomerCareCall() {
        CustomCareApplication.getInstance().getDatabaseReference().child(PENDING_REQUESTS).child(mUserId).setValue(true);
        setViewVisibility(View.GONE, mCall);
    }

    private void setViewVisibility(int visibility, View... views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }
}
