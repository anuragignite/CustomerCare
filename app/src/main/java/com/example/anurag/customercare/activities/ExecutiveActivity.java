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
import com.example.anurag.customercare.views.RippleBackground;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
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

    private static final String      LOG_TAG = "EXECUTIVE_ACTIVITY";

    private LinearLayout             mExecutiveView;

    private TextView                 mExecutiveId, mExecutiveRating, mExecutiveTags, mAnswer;

    private SharedPreferences        mDefaultSharedPreferences;

    private SharedPreferences.Editor mEditor;

    private String                   mUserId;

    private ActiveConnection         activeConnection;

    private ChildEventListener       mActiveThreadChildEventListener, mPendingRequestChildEventListener;

    private String                   mActiveConnectionId;

    private MediaPlayer              mediaPlayer;

    private RippleBackground         mRippleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive);
        setUserId();
        findViews();
        bindViews();

        Toast.makeText(this, "please wait for customer query...your compliance will be rewarded", Toast.LENGTH_LONG).show();

        initValuesForExecutive();
    }

    private void findViews() {
        mExecutiveView = (LinearLayout) findViewById(R.id.ll_executive);
        mExecutiveId = (TextView) findViewById(R.id.tv_e_id);
        mExecutiveRating = (TextView) findViewById(R.id.tv_e_rating);
        mExecutiveTags = (TextView) findViewById(R.id.tv_e_tags);
        mAnswer = (TextView) findViewById(R.id.tv_answer);
        mRippleAnimation = (RippleBackground) findViewById(R.id.ripple);
    }

    private void bindViews() {
        mAnswer.setOnClickListener(this);
        Utils.setViewVisibility(View.GONE, mExecutiveView, mAnswer, mRippleAnimation);
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
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.EXECUTIVES).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mUserId).exists()) {
                    Executive executive = new Executive();
                    executive.setExecutiveId(mUserId);
                    executive.setName("Anurag");
                    executive.setRating(4.1);

                    List<String> tags = new ArrayList<>();
                    tags.add("Champion Executive");
                    tags.add("Polite");
                    tags.add("Tower expert");
                    executive.setTags(tags);

                    CustomCareApplication.getInstance().getDatabaseReference().child(Constants.EXECUTIVES).child(mUserId).setValue(executive);
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
        if (mRippleAnimation != null) {
            mRippleAnimation.stopRippleAnimation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterAsAirtelExecutive();
        stopListeningIncomingCustomerCall();
        stopListeningActiveThreads();
        if (!TextUtils.isEmpty(mActiveConnectionId)) {
            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).child(mActiveConnectionId).removeValue();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startListeningIncomingCustomerCall() {
        mPendingRequestChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(Constants.PENDING_REQUESTS).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "startListeningIncomingCustomerCall:onChildAdded " + s);
                if (!TextUtils.isEmpty(dataSnapshot.getKey())) {
                    activeConnection = new ActiveConnection();
                    activeConnection.setCustomerId(dataSnapshot.getKey());

                    CustomCareApplication.getInstance().getDatabaseReference().child(Constants.CUSTOMERS).child(activeConnection.getCustomerId()).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Customer customer = dataSnapshot.getValue(Customer.class);
                                mExecutiveId.setText(getString(R.string.id_text, customer.getName()));
                                mExecutiveRating.setText(getString(R.string.rating_text, customer.getRating()));
                                List<String> tags = customer.getTags();
                                if (tags != null && !tags.isEmpty()) {
                                    mExecutiveTags.setText(getString(R.string.tag_text, TextUtils.join(", ", tags)));
                                }
                                Utils.setViewVisibility(View.VISIBLE, mExecutiveView, mAnswer, mRippleAnimation);
                                mRippleAnimation.stopRippleAnimation();
                                mRippleAnimation.startRippleAnimation();
                                mediaPlayer = MediaPlayer.create(ExecutiveActivity.this, R.raw.sonar);
                                mediaPlayer.setLooping(true);
                                mediaPlayer.start();
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
            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.PENDING_REQUESTS).removeEventListener(mPendingRequestChildEventListener);
        }
    }

    private void startListeningActiveThreads() {
        mActiveThreadChildEventListener = CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).addChildEventListener(new ChildEventListener() {

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
            CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).removeEventListener(mActiveThreadChildEventListener);
        }
    }

    private void initiateResposeCall() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        activeConnection.setActiveStatus(true);
        activeConnection.setExecutiveId(mUserId);
        mActiveConnectionId = activeConnection.getCustomerId() + activeConnection.getExecutiveId();
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_THREADS).child(activeConnection.getCustomerId() + activeConnection.getExecutiveId()).setValue(activeConnection);
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.PENDING_REQUESTS).child(activeConnection.getCustomerId()).removeValue();

        mRippleAnimation.stopRippleAnimation();
        Utils.setViewVisibility(View.GONE, mAnswer, mRippleAnimation);
    }

    private void registerAsAirtelExecutive() {
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_EXECUTIVES).child(mUserId).setValue(true);
    }

    private void unRegisterAsAirtelExecutive() {
        CustomCareApplication.getInstance().getDatabaseReference().child(Constants.ACTIVE_EXECUTIVES).child(mUserId).removeValue();
    }

}
