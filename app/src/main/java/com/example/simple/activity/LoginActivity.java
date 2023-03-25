package com.example.simple.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simple.R;
import com.example.simple.utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {
    EditText edtTxtUsername, edtTxtPassword;
    Button btnLogin, btnFingerprintLogin;

    TextView txtRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
        String address = sp.getString("ipAddress", "");
        edtTxtUsername = findViewById(R.id.editTextUsernameLogin);
        edtTxtPassword = findViewById(R.id.editTextPasswordLogin);

        txtRegister = findViewById(R.id.registerLogin);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoRegisterActivity();
            }
        });

        btnLogin = findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(view -> {
            String username = edtTxtUsername.getText().toString().trim();
            String password = edtTxtPassword.getText().toString().trim();

            Log.d("asd", "Username: " + username + "Password: " + password );

            if (username.isEmpty()) {
                edtTxtUsername.setError("This field is required");
                edtTxtUsername.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                edtTxtPassword.setError("This field is required");
                edtTxtPassword.requestFocus();
                return;
            }

            RequestParams params = new RequestParams();
            params.add("username", username);
            params.add("password", password);

            HttpUtils.post(address, "/api/users/login", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", "Response: " + response);
                    try {
                        SharedPreferences sp =  getSharedPreferences("simple", MODE_PRIVATE);
                        JSONObject json = new JSONObject(response.toString());
                        String accessToken = json.getString("accessToken");
                        String refreshToken = json.getString("refreshToken");
                        sp.edit().putString("accessToken", accessToken).apply();
                        sp.edit().putString("refreshToken", refreshToken).apply();
                        sp.edit().putString("username", username).apply();
                        sp.edit().putString("password", password).apply();
                        gotoItemActivity();
                        Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (statusCode == 401) {
                        Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        throwable.printStackTrace();
                    }
                }
            });
        });

        btnFingerprintLogin = findViewById(R.id.buttonFingerprintLogin);
        btnFingerprintLogin.setOnClickListener(view -> {
            fingerprintLogin();
        });
    }

    public void gotoItemActivity() {
        Intent intent = new Intent(this, ItemActivity.class);
        startActivity(intent);
        finish();
    }


    public void gotoRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void fingerprintLogin() {
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(LoginActivity.this, "Not working", Toast.LENGTH_LONG).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(LoginActivity.this, "Device Don't have fingerprint", Toast.LENGTH_LONG).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(LoginActivity.this, "No fingerprint assigned", Toast.LENGTH_LONG).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Toast.makeText(LoginActivity.this, "Security update is required for your device", Toast.LENGTH_LONG).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(LoginActivity.this, "This current Android version is not supported", Toast.LENGTH_LONG).show();
                break;

            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Toast.makeText(LoginActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(LoginActivity.this, "Something wrong", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
                String address = sp.getString("ipAddress", "");
                String username = sp.getString("username","");
                String password = sp.getString("password","");

                if (username.isEmpty() || password.isEmpty()) {
                    Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, "You must login by username/password first", Toast.LENGTH_LONG).show();
                } else {
                    RequestParams params = new RequestParams();
                    params.add("username", username);
                    params.add("password", password);
                    HttpUtils.post(address, "/api/users/login", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d("asd", "Response: " + response);
                            try {
                                SharedPreferences sp =  getSharedPreferences("simple", MODE_PRIVATE);
                                JSONObject json = new JSONObject(response.toString());
                                String accessToken = json.getString("accessToken");
                                String refreshToken = json.getString("refreshToken");
                                sp.edit().putString("accessToken", accessToken).apply();
                                sp.edit().putString("refreshToken", refreshToken).apply();
                                gotoItemActivity();
                                Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            if (statusCode == 401) {
                                Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                                throwable.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Simple Shopping")
                .setDescription("Use fingerprint to login")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}