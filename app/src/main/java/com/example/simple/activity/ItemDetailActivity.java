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

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Button btnBuy;
        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
        String address = sp.getString("ipAddress", "");
        String token = sp.getString("accessToken","");
        Log.d("asd", "token: " + token);
        if (token.isEmpty()) {
            gotoLoginActivity();
        } else {
            headerButtons();

            Bundle b = getIntent().getExtras();
            String _id = b.getString("_id");
            String url = "/api/items/" + _id;


            HttpUtils.get(address, url, token,null, new JsonHttpResponseHandler() {
                TextView itemName, itemDes, itemAmount, itemPrice;
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", "Respond: " + response);
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject data = json.getJSONObject("data");

                        itemName = findViewById(R.id.itemNameDetail);
                        itemPrice = findViewById(R.id.itemPriceDetail);
                        itemDes = findViewById(R.id.itemDescriptionDetail);
                        itemAmount = findViewById(R.id.itemAmountDetail);

                        itemName.setText(data.getString("name"));
                        itemPrice.setText(String.valueOf(data.getDouble("price")));
                        itemDes.setText(data.getString("description"));
                        itemAmount.setText("Available: " + String.valueOf(data.getInt("amount")) + " items");

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
                            Login.refreshToken(sp, ItemDetailActivity.this);
                        } else {
                            Toast.makeText(ItemDetailActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            HttpUtils.get(address, "/api/users/me", token, null, new JsonHttpResponseHandler() {
                TextView userMoney;

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("asd", "Respond: " + response);
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject data = json.getJSONObject("data");

                        userMoney = findViewById(R.id.userMoney);

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
                            Login.refreshToken(sp, ItemDetailActivity.this);
                        } else {
                            Toast.makeText(ItemDetailActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            btnBuy = findViewById(R.id.btnBuyItem);

            btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText edtItemQuantity = findViewById(R.id.edtTxtItemQuantity);
                    String itemQuantity = edtItemQuantity.getText().toString().trim();
                    RequestParams params = new RequestParams();
                    params.add("id", _id);
                    params.add("quantity", itemQuantity);

                    HttpUtils.post(address, "/api/users/item", token, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Intent intent = new Intent(ItemDetailActivity.this, ItemActivity.class);
                            startActivity(intent);
                            Toast.makeText(ItemDetailActivity.this, "Buy item successful", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("error", errorResponse.toString());
                            if (statusCode == 400) {
                                Toast.makeText(ItemDetailActivity.this, "Not enough money or not enough item", Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    JSONObject json = new JSONObject(errorResponse.toString());
                                    String message = json.getString("message");
                                    if (message.compareTo("jwt expired") == 0) {
                                        Login.refreshToken(sp, ItemDetailActivity.this);
                                    } else {
                                        Toast.makeText(ItemDetailActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        }

    }
    public void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void headerButtons() {
        ImageButton btnProfile = findViewById(R.id.imgBtnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnItem = findViewById(R.id.imgBtnItem);
        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, ItemActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnMoney = findViewById(R.id.imgBtnAddMoney);
        btnMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, MoneyActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnSetting = findViewById(R.id.imgBtnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }
}