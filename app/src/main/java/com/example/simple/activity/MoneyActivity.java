package com.example.simple.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simple.R;
import com.example.simple.utils.HttpUtils;
import com.example.simple.utils.Login;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MoneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);



        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);

        String accessToken = sp.getString("accessToken", "");
        String address = sp.getString("ipAddress", "");
        if (accessToken.isEmpty()) {
            gotoLoginActivity();
        } else {
            headerButtons();

            EditText edtCreditCard = findViewById(R.id.creditCardMoney);
            EditText edtMoney = findViewById(R.id.valueMoney);
            Button btnAddMoney = findViewById(R.id.btnAddMoney);

            HttpUtils.get(address, "/api/users/me", accessToken, null, new JsonHttpResponseHandler() {
                TextView userMoney;

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", "Respond: " + response);
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject data = json.getJSONObject("data");

                        userMoney = findViewById(R.id.userMoneyMoney);

                        userMoney.setText("Money: " + String.valueOf(data.getDouble("money")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("error", errorResponse.toString());
                    try {
                        JSONObject json = new JSONObject(errorResponse.toString());
                        String message = json.getString("message");
                        if (message.compareTo("jwt expired") == 0) {
                            Login.refreshToken(sp, MoneyActivity.this);
                        } else {
                            Toast.makeText(MoneyActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            btnAddMoney.setOnClickListener(view -> {
                String creditCard = edtCreditCard.getText().toString().trim();
                String money = edtMoney.getText().toString().trim();

                if (creditCard.length() != 16) {
                    edtCreditCard.setError("This field is not a valid credit card");
                    edtCreditCard.requestFocus();
                    return;
                }
                if (!isNumeric(money)) {
                    edtMoney.setError("This field is required");
                    edtMoney.requestFocus();
                    return;
                }

                RequestParams params = new RequestParams();
                params.add("money", money);
                params.add("creditCard", creditCard);

                HttpUtils.patch(address, "/api/users/add-money", accessToken, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("asd", response.toString());
                        finish();
                        startActivity(getIntent());
                        Toast.makeText(MoneyActivity.this, "Add money successful", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("error", errorResponse.toString());
                        if (statusCode == 400) {
                            Toast.makeText(MoneyActivity.this, "Not enough money", Toast.LENGTH_LONG).show();
                        } else {
                            try {
                                JSONObject json = new JSONObject(errorResponse.toString());
                                String message = json.getString("message");
                                if (message.compareTo("jwt expired") == 0) {
                                    Login.refreshToken(sp, MoneyActivity.this);
                                } else {
                                    Toast.makeText(MoneyActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            });


        }
    }

    public void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public void headerButtons() {
        ImageButton btnProfile = findViewById(R.id.imgBtnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoneyActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnItem = findViewById(R.id.imgBtnItem);
        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoneyActivity.this, ItemActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnMoney = findViewById(R.id.imgBtnAddMoney);
        btnMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        ImageButton btnSetting = findViewById(R.id.imgBtnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoneyActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }
}