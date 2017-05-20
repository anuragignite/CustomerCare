package com.example.anurag.customercare.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.anurag.customercare.R;
import com.example.anurag.customercare.application.CustomCareApplication;
import com.example.anurag.customercare.constants.Constants;
import com.example.anurag.customercare.pojos.ActiveConnection;
import com.example.anurag.customercare.pojos.Customer;
import com.example.anurag.customercare.pojos.Executive;
import com.example.anurag.customercare.utils.Utils;
import com.example.anurag.customercare.views.CircleProgressBar;
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

public class CustomerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String      LOG_TAG = "CUSTOMER_ACTIVITY";

    private LinearLayout             mRootView, mCustomerView;

    private TextView                 mCustomerId, mCustomerRating, mCustomerTags, mCall;

    private CircleProgressBar        mCircleProgressBar;

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
        mRootView = (LinearLayout) findViewById(R.id.ll_root);
        mCircleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        mCustomerView = (LinearLayout) findViewById(R.id.ll_customer);
        mCustomerId = (TextView) findViewById(R.id.tv_c_id);
        mCustomerRating = (TextView) findViewById(R.id.tv_c_rating);
        mCustomerTags = (TextView) findViewById(R.id.tv_c_tags);
        mCall = (TextView) findViewById(R.id.tv_call);
    }

    private void bindViews() {
        mCall.setOnClickListener(this);
        Utils.setViewVisibility(View.GONE, mCustomerView, mCircleProgressBar);
        Utils.setViewVisibility(View.VISIBLE, mRootView);
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
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.CUSTOMERS).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mUserId).exists()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(mUserId);
                    customer.setName(CustomCareApplication.getInstance().getName());
                    customer.setRating(3);

                    List<String> tags = new ArrayList<>();
                    tags.add("Recent Connection");
                    tags.add("Short Tempered");
                    tags.add("Tower Problem");
                    customer.setTags(tags);

                    CustomCareApplication.getInstance().getDatabaseReference().child(Constants.CUSTOMERS).child(mUserId).setValue(customer);
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
            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).child(mActiveConnectionId).removeValue();
        }
    }

    private void startListeningActiveThreads() {
        mActiveThreadChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningActiveThreads : onChildAdded : " + s);
                try {
                    if (dataSnapshot.exists()) {
                        ActiveConnection activeConnection = dataSnapshot.getValue(ActiveConnection.class);
                        String customerId = activeConnection.getCustomerId();
                        if (mUserId.equalsIgnoreCase(customerId)) {
                            mActiveConnectionId = activeConnection.getCustomerId() + activeConnection.getExecutiveId();
                            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.EXECUTIVES).child(activeConnection.getExecutiveId()).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Executive executive = dataSnapshot.getValue(Executive.class);
                                        mCustomerId.setText(getString(R.string.id_text, executive.getName()));
                                        mCustomerRating.setText(getString(R.string.rating_text, executive.getRating()));
                                        List<String> tags = executive.getTags();
                                        if (tags != null && !tags.isEmpty()) {
                                            mCustomerTags.setText(getString(R.string.tag_text, TextUtils.join(", ", tags)));
                                        }
                                        Utils.setViewVisibility(View.VISIBLE, mCustomerView, mRootView);
                                        Utils.setViewVisibility(View.GONE, mCircleProgressBar);
                                        // Utils.setViewVisibility(View.GONE,
                                        // mCall);
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
            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).removeEventListener(mActiveThreadChildEventListener);
        }
    }

    private void initiateCustomerCareCall() {
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.PENDING_REQUESTS).child(mUserId).setValue(true);
        Utils.setViewVisibility(View.GONE, mCall, mRootView);
        Utils.setViewVisibility(View.VISIBLE, mCircleProgressBar);
        mCircleProgressBar.setProgressWithAnimation(100);
        Toast.makeText(CustomerActivity.this, "Searching for airtel executives...please waith", Toast.LENGTH_LONG).show();
    }

}
