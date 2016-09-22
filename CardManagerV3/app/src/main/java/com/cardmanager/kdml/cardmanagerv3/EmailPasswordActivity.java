package com.cardmanager.kdml.cardmanagerv3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmanager.kdml.cardmanagerv3.DTO.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";
    Bundle extra;
    Intent intent;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mNameField;
    private EditText mPhoneField;
    private boolean isLoginFlag = false;
    private DatabaseReference mDatabase;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        TelephonyManager telephonyManager = (TelephonyManager)getBaseContext().getSystemService(getBaseContext().TELEPHONY_SERVICE);
        String phoneNum = telephonyManager.getLine1Number();
        phoneNum = phoneNum.replace("+82","0");
        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mNameField = (EditText) findViewById(R.id.field_name);
        mPhoneField = (EditText) findViewById(R.id.field_phone);

        mPhoneField.setText(phoneNum);
        extra = new Bundle();
        intent = new Intent();
        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        Bundle bnd = this.getIntent().getExtras();
        if(bnd != null && bnd.getInt("loginFlag") == 1)
        {
            isLoginFlag = true;
        }
        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //User appUser = cd.getUser();
                if (user != null) {
                    // User is signed in
                    //Toast.makeText(getBaseContext(),"Login success",Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    extra.putInt("data",1);
                    intent.putExtras(extra);
                    setResult(RESULT_OK,intent);
                    readUsers(user.getUid());


                    if(isLoginFlag)
                        finish();
                } else {
                    // User is signed out
                    //Toast.makeText(getBaseContext(),"Logout",Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    User appUser = new User();
                    appUser.setName("Guest");
                    appUser.setFireBase_ID("");
                    appUser.setEmail("Guest");
                    updateUserInfo_Email_FireBaseID(appUser);
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }
    public void readUsers(String uid) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Log.d(TAG, snapshot.toString());
                    String key = snapshot.getKey();
                    User value = snapshot.getValue(User.class);
                    Log.d(TAG, "key : " + key);
                    Log.d(TAG, value.toString());
                    updateUserInfo_Email_FireBaseID(value);

                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm("ACCOUNT")) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            FirebaseUser user = task.getResult().getUser();
                            User appUser = new User();
                            appUser.setName(mNameField.getText().toString());
                            appUser.setFireBase_ID( user.getUid());
                            appUser.setEmail(user.getEmail());
                            appUser.setPhone(mPhoneField.getText().toString());
                            mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("users").child(user.getUid()).setValue(appUser);
                            mDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User value = dataSnapshot.getValue(User.class);
                                    updateUserInfo_Email_FireBaseID(value);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm("SIGN")) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm(String type) {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if ((type.equals("SIGN") ||type.equals("ACCOUNT"))  && TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if ((type.equals("SIGN") ||type.equals("ACCOUNT"))  && TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String name = mNameField.getText().toString();
        if (type.equals("ACCOUNT") && TextUtils.isEmpty(name)) {
            mNameField.setError("Required.");
            valid = false;
        } else {
            mNameField.setError(null);
        }

        String phone = mPhoneField.getText().toString();
        if (type.equals("ACCOUNT") && TextUtils.isEmpty(phone)) {
            mPhoneField.setError("Required.");
            valid = false;
        } else {
            mPhoneField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields2).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields2).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email_create_account_button:
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.email_sign_in_button:
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    public boolean updateUserInfo_Email_FireBaseID(User user)
    {
        CustomerDatabase cd = CustomerDatabase.getInstance(null);
        String UPDATE_SQL = "update " + cd.TABLE_CUSTOMER_INFO +" set CUSTOMER_EMAIL = '" + user.getEmail() +"', CUSTOMER_NAME = '"+user.getName()  +"', FireBase_ID = '" + user.getFireBase_ID()+"', CUSTOMER_PHONE = '" + user.getPhone()+"'";
        try {
            cd.execSQL(UPDATE_SQL);
        } catch(Exception ex) {
            Log.e(TAG, "Exception in UPDATE_SQL TABLE_CUSTOMER_INFO", ex);
            return false;
        }
        return true;
    }
}
