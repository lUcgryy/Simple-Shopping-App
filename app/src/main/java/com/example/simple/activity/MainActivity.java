package com.example.simple.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.InetAddresses;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.simple.R;

public class MainActivity extends AppCompatActivity {
    Button btnLogin, btnRegister;
    TextView txtAddressConf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = getSharedPreferences("simple", MODE_PRIVATE);
        String address = sp.getString("ipAddress", "");

        if (address.isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter IP address (format: x.x.x.x)");
            builder.setCancelable(false);

            EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sp.edit().putString("ipAddress", input.getText().toString().trim()).apply();
                    if (!sp.getString("accessToken","").isEmpty()) {
                        gotoItemActivity();
                    }
                }
            });

            AlertDialog alertDialog = builder.show();
            Button btnDialog = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnDialog.setEnabled(false);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String address = input.getText().toString().trim();
                    if (isIpValid(address)) {
                        btnDialog.setEnabled(true);
                    } else {
                        btnDialog.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


        } else {
            if (!sp.getString("accessToken","").isEmpty()) {
                gotoItemActivity();
            }
        }

        txtAddressConf = findViewById(R.id.addressSetting);
        txtAddressConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSettingActivity();
            }
        });
        btnLogin = findViewById(R.id.buttonLoginMain);
        btnRegister = findViewById(R.id.buttonRegisterMain);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }
    public void gotoItemActivity() {
        Intent intent = new Intent(this, ItemActivity.class);
        startActivity(intent);
        finish();
    }

    public void gotoSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private boolean isIpValid(String address) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return InetAddresses.isNumericAddress(address);
        } else {
            return Patterns.IP_ADDRESS.matcher(address).matches();
        }
    }

}