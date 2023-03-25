package com.example.simple.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.InetAddresses;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.simple.R;

public class SettingActivity extends AppCompatActivity {

    SharedPreferences sp;
    Button btnSaveConf;
    EditText edtAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        headerButtons();

        sp = getSharedPreferences("simple", MODE_PRIVATE);
        String oldAddress = sp.getString("ipAddress", "");
        btnSaveConf = findViewById(R.id.btnSaveConf);
        edtAddress = findViewById(R.id.ipAddress);
        edtAddress.setText(oldAddress);

        btnSaveConf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String newAddress = edtAddress.getText().toString().trim();
                if (!isIpValid(newAddress)) {
                    edtAddress.setError("Not a valid address");
                    edtAddress.requestFocus();
                    return;
                } else {
                    Log.d("Address", newAddress);
                    sp.edit().putString("ipAddress", newAddress).apply();
                    Toast.makeText(SettingActivity.this, "Save success", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private boolean isIpValid(String address) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return InetAddresses.isNumericAddress(address);
        } else {
            return Patterns.IP_ADDRESS.matcher(address).matches();
        }
    }

    public void headerButtons() {
        ImageButton btnProfile = findViewById(R.id.imgBtnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnItem = findViewById(R.id.imgBtnItem);
        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, ItemActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnMoney = findViewById(R.id.imgBtnAddMoney);
        btnMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, MoneyActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnSetting = findViewById(R.id.imgBtnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });
    }
}