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
import android.widget.Toast;

public class ExecutiveActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String      LOG_TAG           = "EXECUTIVE_ACTIVITY";

    private static final String      ACTIVE_EXECUTIVES = "activeexecutive";

    private static final String      ACTIVE_THREADS    = "activethreads";

    private static final String      PENDING_REQUESTS  = "pendingrequests";

    private static final String      CUSTOMERS         = "customers";

    private static final String      EXECUTIVES        = "executives";

    private TextView                 mExecutiveId, mExecutiveRating, mExecutiveTags, mAnswer;

    private SharedPreferences        mDefaultSharedPreferences;

    private SharedPreferences.Editor mEditor;

    private String                   mUserId;

    private ActiveConnection         activeConnection;

    private ChildEventListener       mActiveThreadChildEventListener, mPendingRequestChildEventListener;

    private String                   mActiveConnectionId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive);
        setUserId();
        findViews();
        bindViews();

        initValuesForExecutive();
    }

    private void findViews() {
        mExecutiveId = (TextView) findViewById(R.id.tv_e_id);
        mExecutiveRating = (TextView) findViewById(R.id.tv_e_rating);
        mExecutiveTags = (TextView) findViewById(R.id.tv_e_tags);
        mAnswer = (TextView) findViewById(R.id.tv_answer);
    }

    private void bindViews() {
        mAnswer.setOnClickListener(this);
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

    private void initValuesForExecutive() {
        CustomCareApplication.getInstance().getDatabaseReference().child(EXECUTIVES).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mUserId).exists()) {
                    Executive executive = new Executive();
                    executive.setExecutiveId(mUserId);
                    executive.setRating(4.1);
                    CustomCareApplication.getInstance().getDatabaseReference().child(EXECUTIVES).child(mUserId).setValue(executive);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, "initValuesForExecutive : " + databaseError);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.tv_answer:
            initiateResposeCall();
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerAsAirtelExecutive();
        startListeningIncomingCustomerCall();
        startListeningActiveThreads();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterAsAirtelExecutive();
        stopListeningIncomingCustomerCall();
        stopListeningActiveThreads();
        if (!TextUtils.isEmpty(mActiveConnectionId)) {
            CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).child(mActiveConnectionId).removeValue();
        }
    }

    private void startListeningIncomingCustomerCall() {
        mPendingRequestChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(PENDING_REQUESTS).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall:onChildAdded " + s);
                if (!TextUtils.isEmpty(dataSnapshot.getKey())) {
                    activeConnection = new ActiveConnection();
                    activeConnection.setCustomerId(dataSnapshot.getKey());

                    CustomCareApplication.getInstance().getDatabaseReference().child(CUSTOMERS).child(activeConnection.getCustomerId()).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Customer customer = dataSnapshot.getValue(Customer.class);
                                mExecutiveId.setText(getString(R.string.id_text, customer.getCustomerId()));
                                mExecutiveRating.setText(getString(R.string.rating_text, customer.getRating()));
//                                setViewVisibility(View.GONE, mAnswer);
                                Toast.makeText(ExecutiveActivity.this, "Customer request coming....please answer", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(LOG_TAG, "initValuesForCustomer : " + databaseError);
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall: onChildChanged " + s);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall : onChildRemoved ");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall : onChildMoved ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall : onCancelled ");
            }
        });
    }

    private void stopListeningIncomingCustomerCall() {
        if (mPendingRequestChildEventListener != null) {
            CustomCareApplication.getInstance().getDatabaseReference().child(PENDING_REQUESTS).removeEventListener(mPendingRequestChildEventListener);
        }
    }

    private void startListeningActiveThreads() {
        mActiveThreadChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildAdded : " + s);
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

    private void initiateResposeCall() {
        activeConnection.setActiveStatus(true);
        activeConnection.setExecutiveId(mUserId);
        mActiveConnectionId = activeConnection.getCustomerId() + activeConnection.getExecutiveId();
        CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_THREADS).child(activeConnection.getCustomerId() + activeConnection.getExecutiveId()).setValue(activeConnection);
        CustomCareApplication.getInstance().getDatabaseReference().child(PENDING_REQUESTS).child(activeConnection.getCustomerId()).removeValue();
        setViewVisibility(View.GONE, mAnswer);
    }

    private void registerAsAirtelExecutive() {
        CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_EXECUTIVES).child(mUserId).setValue(true);
    }

    private void unRegisterAsAirtelExecutive() {
        CustomCareApplication.getInstance().getDatabaseReference().child(ACTIVE_EXECUTIVES).child(mUserId).removeValue();
    }

    private void setViewVisibility(int visibility, View... views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }
}
