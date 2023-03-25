package com.example.simple.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.simple.R;
import com.example.simple.utils.HttpUtils;
import com.example.simple.utils.Login;
import com.example.simple.utils.RealPathUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity {
    private final int READ_GALLERY_CODE = 10;
    private final int CAMERA_CODE = 11;
    private ImageButton imgBtnAvatar;
    private Uri mUri;

    private ProgressDialog progressDialog;

    private SharedPreferences sp;
    private String token, address;


    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("Profile Activity", "On Activity Result");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imgBtnAvatar.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
    );

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            ContentResolver resolver = getContentResolver();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, mUri);
                                imgBtnAvatar.setImageBitmap(bitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        Button btnUpdate, btnLogout, btnUploadAvatar;

        imgBtnAvatar = findViewById(R.id.imgAvatar);
        btnUploadAvatar = findViewById(R.id.btnUploadAvatar);
        sp = getSharedPreferences("simple", MODE_PRIVATE);
        token = sp.getString("accessToken","");
        address = sp.getString("ipAddress", "");

        if (token.isEmpty()) {
            gotoLoginActivity();
        } else {
            headerButtons();
            // Set up avatar
            imgBtnAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String[] options = {
                            "Open Gallery", "Use Camera", "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Choose an option");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String option = options[i];
                            switch (option) {
                                case "Open Gallery":
                                    onClickRequestGalleryPermission();
                                    break;
                                case "Use Camera":
                                    onClickRequestCameraPermission();
                                    break;
                                case "Cancel":
                                    dialogInterface.cancel();
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + option);
                            }
                        }
                    });
                    builder.show();
                }
            });

            btnUploadAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mUri != null) {
                        uploadImage();
                    } else {
                        Toast.makeText(ProfileActivity.this, "You haven't choose the different image", Toast.LENGTH_LONG).show();

                    }
                }
            });

            final EditText[] edtName = new EditText[1];
            final EditText[] edtPhone = new EditText[1];
            HttpUtils.get(address, "/api/users/me", token, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    TextView txtUsername, txtEmail, txtResetToken;
                    Log.d("asd", "Respond " + response);
                    try {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject data = json.getJSONObject("data");

                        edtName[0] = findViewById(R.id.nameProfile);
                        txtUsername = findViewById(R.id.usernameProfile);
                        txtEmail = findViewById(R.id.emailProfile);
                        edtPhone[0] = findViewById(R.id.phoneProfile);

                        String name = data.getString("name");
                        String username = data.getString("username");
                        String email = data.getString("email");
                        String phone = data.getString("phone");
                        String avatar = data.getString("avatar");
                        String avatarUrl = HttpUtils.getAbsoluteUrl(address, avatar);
                        Glide.with(ProfileActivity.this).load(avatarUrl).into(imgBtnAvatar);
                        if (name.isEmpty()) {
                            edtName[0].setText("");
                        } else {
                            edtName[0].setText(name);
                        }
                        txtUsername.setText(username);
                        txtEmail.setText(email);
                        edtPhone[0].setText(phone);
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
                            Login.refreshToken(sp, ProfileActivity.this);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            btnUpdate = findViewById(R.id.btnUpdateProfile);
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    edtName[0] = findViewById(R.id.nameProfile);
                    edtPhone[0] = findViewById(R.id.phoneProfile);

                    RequestParams params = new RequestParams();

                    String name = edtName[0].getText().toString().trim();
                    String phone = edtPhone[0].getText().toString().trim();

                    params.add("name", name);
                    params.add("phone", phone);

                    HttpUtils.patch(address, "/api/users/me", token, params, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            Toast.makeText(ProfileActivity.this, "Update Success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d("error", errorResponse.toString());
                            try {
                                JSONObject json = new JSONObject(errorResponse.toString());
                                String message = json.getString("message");
                                if (message.compareTo("jwt expired") == 0) {
                                    Login.refreshToken(sp, ProfileActivity.this);
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            btnLogout = findViewById(R.id.btnLogoutProfile);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sp.edit().putString("accessToken", "").apply();
                    sp.edit().putString("refreshToken", "").apply();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void headerButtons() {
        ImageButton btnProfile = findViewById(R.id.imgBtnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        ImageButton btnItem = findViewById(R.id.imgBtnItem);
        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ItemActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnMoney = findViewById(R.id.imgBtnAddMoney);
        btnMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MoneyActivity.class);
                startActivity(intent);
            }
        });

        ImageButton btnSetting = findViewById(R.id.imgBtnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

    }

    private void onClickRequestGalleryPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission, READ_GALLERY_CODE);
        }
    }

    private void onClickRequestCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openCamera();
            return;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, CAMERA_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_GALLERY_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
        if (requestCode == CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryActivityResultLauncher.launch(Intent.createChooser(intent, "Select avatar"));
    }

    private void uploadImage() {
        progressDialog.show();
        Log.d("Uri", ""+mUri);
        String realFilePath = RealPathUtil.getRealPath(this, mUri);

        Log.d("Image", realFilePath);
        File file = new File(realFilePath);

        RequestParams params = new RequestParams();
        try {
            params.put("avatar", file);
            HttpUtils.patch(address, "/api/users/upload-avatar", token, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressDialog.dismiss();
                    Log.d("avatar", response.toString());
                    Toast.makeText(ProfileActivity.this, "Upload Success", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progressDialog.dismiss();
                    Log.d("error", errorResponse.toString());
                    try {
                        JSONObject json = new JSONObject(errorResponse.toString());
                        String message = json.getString("message");
                        if (message.compareTo("jwt expired") == 0) {
                            Login.refreshToken(sp, ProfileActivity.this);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Something wrong with the API", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String filename = "cam_avatar.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        mUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        cameraActivityResultLauncher.launch(intent);
    }

}