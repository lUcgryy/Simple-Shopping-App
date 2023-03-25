package com.example.simple.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.simple.R;
import com.example.simple.model.Item;
import com.example.simple.utils.HttpUtils;
import com.example.simple.utils.ItemAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
        String address = sp.getString("ipAddress", "");
        headerButtons();

        HttpUtils.get(address, "/api/items", null, new JsonHttpResponseHandler() {
            ArrayList<Item> items = new ArrayList<>();
            RecyclerView itemsRecyclerView;

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("asd", "Response: " + response);
                try {
                    itemsRecyclerView = findViewById(R.id.itemList);
                    JSONObject res_json = new JSONObject(response.toString());
                    JSONArray data = res_json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject itemJson = data.getJSONObject(i);
                        String _id = itemJson.getString("_id");
                        String name = itemJson.getString("name");
                        String description = itemJson.getString("description");
                        double price = itemJson.getDouble("price");
                        int amount = itemJson.getInt("amount");
                        Item item = new Item(_id, name, description, price, amount);
                        items.add(item);
                    }
                    ItemAdapter adapter = new ItemAdapter();
                    adapter.setItems(items);

                    itemsRecyclerView.setAdapter(adapter);
                    itemsRecyclerView.setLayoutManager(new LinearLayoutManager(ItemActivity.this));

                } catch (JSONException  e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(ItemActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }
        });
    }
    public void headerButtons() {
        ImageButton btnProfile = findViewById(R.id.imgBtnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnItem = findViewById(R.id.imgBtnItem);
        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        ImageButton btnMoney = findViewById(R.id.imgBtnAddMoney);
        btnMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemActivity.this, MoneyActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnSetting = findViewById(R.id.imgBtnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }
}