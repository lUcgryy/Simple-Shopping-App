package com.example.simple.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simple.R;
import com.example.simple.utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity {
    EditText edtTxtEmail, edtTxtUsername, edtTxtPassword, edtTxtConfirmPassword;
    Button btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
        String address = sp.getString("ipAddress", "");

        edtTxtEmail = findViewById(R.id.editTextEmailRegister);
        edtTxtUsername = findViewById(R.id.editTextUsernameRegister);
        edtTxtPassword = findViewById(R.id.editTextPasswordRegister);
        edtTxtConfirmPassword = findViewById(R.id.editTextConfirmPasswordRegister);

        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(view -> {
            String email = edtTxtEmail.getText().toString().trim();
            String username = edtTxtUsername.getText().toString().trim();
            String password = edtTxtPassword.getText().toString().trim();
            String confirmPassword = edtTxtConfirmPassword.getText().toString().trim();
            if (email.isEmpty()) {
                edtTxtEmail.setError("This field is required");
                edtTxtEmail.requestFocus();
                return;
            }
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
            if (confirmPassword.isEmpty()) {
                edtTxtConfirmPassword.setError("This field is required");
                edtTxtConfirmPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edtTxtPassword.setError("The password must be at least 6 characters");
                edtTxtPassword.requestFocus();
                return;
            }
            if (confirmPassword.compareTo(password) != 0) {
                edtTxtConfirmPassword.setError("The passwords are not match");
                edtTxtConfirmPassword.requestFocus();
                return;
            }
            RequestParams params = new RequestParams();
            params.add("email", email);
            params.add("username", username);
            params.add("password", password);
            params.add("passwordConfirm", confirmPassword);

            HttpUtils.post(address, "/api/users/register", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", "Response: " + response);
//                    try {
//                        JSONObject json = new JSONObject(response.toString());
//                        JSONObject data = json.getJSONObject("data");
                    LinearLayout linearLayout = findViewById(R.id.registerLayout);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT);
                    Button btnLogin = new Button(RegisterActivity.this);
                    btnLogin.setLayoutParams(params);
                    btnLogin.setText("Login");
                    btnLogin.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, R.color.green));
                    btnLogin.setTextColor(ContextCompat.getColorStateList(RegisterActivity.this, R.color.white));

                    btnLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });

                    linearLayout.addView(btnLogin);

                    Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_LONG).show();

//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(RegisterActivity.this, "Invalid information", Toast.LENGTH_LONG).show();
                    Log.d("error", errorResponse.toString());
                }
            });
        });
    }


}