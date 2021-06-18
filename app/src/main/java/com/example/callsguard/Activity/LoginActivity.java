package com.example.callsguard.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.callsguard.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout phoneNumTextField;
    private TextInputLayout passwordTextField;
    private Button sendButton;
    private Button signButton;
    private CountryCodePicker ccp;
    private String phoneNumber;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private String sendVerificationCodeBySystem;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        phoneNumber = loadData();
        if (phoneNumber != "") {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("phoneNum", phoneNumber);
            startActivity(intent);
            finish();
        }

        initUI();
        signButton.setVisibility(View.GONE);
        passwordTextField.setVisibility(View.GONE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = getPhoneNumber();
                sendVerificationCodeToUser(phoneNumber);
                signButton.setVisibility(View.VISIBLE);
                passwordTextField.setVisibility(View.VISIBLE);
                phoneNumTextField.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                ccp.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
            }
        });
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = getPhoneNumber();
                if (phoneNumber.isEmpty() || phoneNumber.length() < 6) {
                    phoneNumTextField.setError("Wrong Number");
                    phoneNumTextField.requestFocus();
                    return;

                }
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(phoneNumber);

            }
        });

    }

    private void initUI() {
        ccp = findViewById(R.id.ccp);
        phoneNumTextField = findViewById(R.id.phoneNumTextField);
        passwordTextField = findViewById(R.id.passwordTextField);
        sendButton = findViewById(R.id.sendButton);
        signButton = findViewById(R.id.signButton);
        progressBar = findViewById(R.id.progress_circular);
    }

    private String getPhoneNumber() {
        return phoneNumber = ccp.getSelectedCountryCodeWithPlus() + phoneNumTextField.getEditText().getText().toString();
    }

    private void sendVerificationCodeToUser(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            sendVerificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ppt", e.getMessage());
            progressBar.setVisibility(View.GONE);
            sendButton.setVisibility(View.VISIBLE);
            ccp.setVisibility(View.VISIBLE);
            phoneNumTextField.setVisibility(View.VISIBLE);
            signButton.setVisibility(View.GONE);
            passwordTextField.setVisibility(View.GONE);

        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(sendVerificationCodeBySystem, codeByUser);
        signInTheUserByCredential(credential);

    }

    private void signInTheUserByCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    saveData(phoneNumber);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("phoneNum", phoneNumber);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("ppt", task.getException().getMessage());
                    sendButton.setVisibility(View.VISIBLE);
                    ccp.setVisibility(View.VISIBLE);
                    phoneNumTextField.setVisibility(View.VISIBLE);
                    signButton.setVisibility(View.GONE);
                    passwordTextField.setVisibility(View.GONE);

                }
            }
        });
    }

    public void saveData(String text) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT, text);
        editor.apply();
    }

    public String loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String text = sharedPreferences.getString(TEXT, "");

        return text;
    }
}